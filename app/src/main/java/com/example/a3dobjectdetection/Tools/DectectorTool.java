package com.example.a3dobjectdetection.Tools;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import com.example.a3dobjectdetection.Feature.FeatureExtract_AKAZE;
import com.example.a3dobjectdetection.Feature.FeatureExtract_ORB;
import com.example.a3dobjectdetection.Feature.Matcher;
import com.example.a3dobjectdetection.Feature.SolverPnP;
import com.example.a3dobjectdetection.Helper.FileHelper;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class DectectorTool extends Thread {
    private static final String TAG=DectectorTool.class.getSimpleName();


    private PointCLoud_AKAZE pointCLoud_akaze;
    private MatOfPoint3f pointcoordinates;
    private Mat descriptions ;

    private FeatureExtract_AKAZE featureExtract_akaze;
    private FeatureExtract_ORB featureExtract_orb;
    private Matcher matcher;

    private Session session;
    private AssetManager assetManager;

    private boolean reco=false;//是否识别
    private boolean alreadyreco=false;//是否已经识别过了

    private boolean isPause=false;
    private boolean isClose=false;

    private float[] viewMatrix = new float[16];
    private float[] RT = new float[16];
    private float[] pose=new float[16];


    public DectectorTool(FileHelper fileHelper, AssetManager assetManager,String filename,Session session){
        pointCLoud_akaze=fileHelper.
                getTxtFromAssetsToMatChange(assetManager,filename);
        if(pointCLoud_akaze==null){
            Log.e(TAG,"文件失败");
            return;
        }else{
            Log.e(TAG,pointCLoud_akaze.toString());
        }
        descriptions=pointCLoud_akaze.getDescriptions();
        pointcoordinates=pointCLoud_akaze.getPoints();
        featureExtract_akaze=new FeatureExtract_AKAZE();
        featureExtract_orb=new FeatureExtract_ORB();
        matcher=new Matcher();
        this.session=session;
        this.assetManager=assetManager;
    }

    public synchronized void TrakerFail(){
        reco=false;
        this.notify();
    }

    public synchronized void onThreadPause() {
        isPause = true;
    }

    private void onThreadWait() {
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public synchronized void onThreadResume() {
        isPause = false;
        this.notify();
    }

    public boolean isClose() {
        return isClose;
    }

    public void setClose(boolean isClose) {
        this.isClose = isClose;
    }

    public synchronized void closeThread() {
        try {
            notify();
            setClose(true);
            interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        super.run();
        while (!isClose && !isInterrupted() && !isPause){
          detect();
        }
    }

    private void detect(){
        try {
            Frame frame;
            //frame=session.update();
            InputStream in1=assetManager.open("image/3.jpg");
            //InputStream in2=getAssets().open("image/2.jpg");

            Mat src1 = new Mat();
            //Mat src2 = new Mat();
            Bitmap bitmap1 = BitmapFactory.decodeStream(in1);
            //Bitmap bitmap2 = BitmapFactory.decodeStream(in2);
            Utils.bitmapToMat(bitmap1,src1);
            //Utils.bitmapToMat(bitmap2,src2);
            MatOfKeyPoint keypoint1=new MatOfKeyPoint();
            //MatOfKeyPoint keypoint2=new MatOfKeyPoint();
            Mat description1=new Mat();
            // Mat description2=new Mat();
            Mat mask1=new Mat();
            //Mat mask2=new Mat();
            featureExtract_akaze.extractFeatureandDescription(src1,mask1,keypoint1,description1);

            Log.i(TAG,description1.toString());
            Log.i(TAG,keypoint1.toString());


            List<MatOfDMatch> dMatches1=new LinkedList<>();
            matcher.Bruteforcematcher(Core.NORM_L2,description1,descriptions,dMatches1,2);
            List<DMatch> good1=new LinkedList<>();
            //List<DMatch> good2=new LinkedList<>();
            matcher.NNdistanceRatio(dMatches1,1.5f,good1);
            Log.e(TAG, String.valueOf(good1.size()));
            //matcher.NNdistanceRatio(define2,0.8f,good2);
            MatOfPoint3f threepoint=new MatOfPoint3f();
            MatOfPoint2f imagepoint=new MatOfPoint2f();
            SolverPnP.FindKeypointMatch(threepoint,imagepoint,good1,pointcoordinates,keypoint1);
            Mat K=new Mat(3,3, CvType.CV_32FC1);
            float[] a=new float[]{4637,0,2883,0,4637,1288,0,0,1};
            K.put(0,0,a);
            Mat revec=new Mat();
            Mat tvec=new Mat();
            synchronized (this){
                reco=SolverPnP.Solver(threepoint,imagepoint,K, new MatOfDouble(),revec,tvec);
                if(reco) {
                    Log.e(TAG, "旋转向量：" + revec);
                    Log.e(TAG, "平移  " + tvec);
                    double[] r = MatrixTool.matTodouble(revec);
                    double[] t = MatrixTool.matTodouble(tvec);
                    tvec.get(0, 0, t);
                    for (int i = 0; i < r.length; i++) {
                        Log.e(TAG, "旋转向量：" + r[i]);
                    }
                    for (int i = 0; i < t.length; i++) {
                        Log.e(TAG, "平移：" + t[i]);
                    }

                    Mat mat = new Mat();
                    Calib3d.Rodrigues(revec, mat);
                    Log.e(TAG, "旋转矩阵：" + mat);
                    //double[] R = MatrixTool.matTodouble(mat);
                    double[] R = MatrixTool.matTodouble(mat);
                    //MatrixTool.doubletofloat(R,t,viewMatrix);

                    double[] r_change=MatrixTool.matTodouble(mat);
                    for(int i=0;i<r_change.length;i++){
                        Log.e(TAG,"旋转矩阵："+r_change[i]);
                    }

                    Mat mat2=new Mat();
                    Mat mattranspose=mat.t();

                    double[][] transpose=MatrixTool.matTodoubletwo(mattranspose);
                    double[][] td=MatrixTool.matTodoubletwo(tvec);

                    double[][] result= MatrixTool.BruteForce(transpose,td);
                    double[] t_=new double[3];
                    for(int i=0;i<result.length;i++){
                        for(int j=0;j<result[0].length;j++){
                            t_[i]=-result[i][j];
                            Log.e(TAG,"相机位置："+t_[i]);
                        }
                    }
                    MatrixTool.doubletofloat(R,t_,pose);
                    MatrixTool.doubletofloat(R,t,RT);
                    onThreadPause();
                }else {
                    Log.e(TAG,"求解失败");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean isReco() {
        synchronized (this) {
            return reco;
        }
    }

    public float[] getViewMatrix() {
        synchronized (this) {
            return viewMatrix;
        }
    }
    public float[] getPose() {
        synchronized (this) {
            return pose;
        }
    }

    public boolean isAlreadyreco() {
        return alreadyreco;
    }

    public void setAlreadyreco(boolean alreadyreco) {
        this.alreadyreco = alreadyreco;
    }

    public float[] getRT() {
        return RT;
    }
}
