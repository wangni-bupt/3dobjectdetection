package com.example.a3dobjectdetection.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.example.a3dobjectdetection.Feature.FeatureExtract_AKAZE;
import com.example.a3dobjectdetection.Feature.FeatureExtract_ORB;
import com.example.a3dobjectdetection.Feature.Matcher;
import com.example.a3dobjectdetection.Feature.SolverPnP;
import com.example.a3dobjectdetection.Helper.FileHelper;
import com.example.a3dobjectdetection.Tools.MatrixTool;
import com.example.a3dobjectdetection.Tools.PointCLoud_AKAZE;
import com.example.a3dobjectdetection.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
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
import org.opencv.core.Point3;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class OpenCVTestActivity extends AppCompatActivity {

    private BaseLoaderCallback mLoaderCallback;
    private final static String TAG= OpenCVTestActivity.class.getSimpleName();
    private FeatureExtract_AKAZE featureExtract_akaze;
    private FeatureExtract_ORB featureExtract_orb;
    private Matcher matcher;
    private FileHelper fileHelper=new FileHelper();

    public static final String FILE_ENCODING = "UTF-8";
    private MatOfPoint3f pointcoordinates;
    private Mat descriptions ;
    private List<Point3> list1;
    private List<Float> list2;

    private AssetManager assetManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_c_v_test2);
        assetManager = getResources().getAssets();

        mLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                // TODO Auto-generated method stub
                switch (status){
                    case BaseLoaderCallback.SUCCESS:
                        Log.i(TAG, "成功加载");
                        break;
                    default:
                        super.onManagerConnected(status);
                        Log.i(TAG, "加载失败");
                        break;
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this,mLoaderCallback);
        }else{
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }


//        list1=new LinkedList<>();
//        list2=new LinkedList<>();
//        if (fileHelper.getTxtFromAssetsToMat(assetManager,"3D-0.01.txt",list1,list2)) {
//            Log.i(TAG, "文件读取成功");
//            //Log.i(TAG, String.valueOf(pointcoordinates.size()));
//            //Log.i(TAG, String.valueOf(descriptions.size()));
//        }
//        descriptions = new Mat(list1.size(), 64, CvType.CV_32FC1);
//        pointcoordinates = new MatOfPoint3f();
//        fileHelper.VectorToMat(list1,list2,pointcoordinates,descriptions);

        PointCLoud_AKAZE pointCLoud_akaze=fileHelper.
                getTxtFromAssetsToMatChange(assetManager,"3D-0.01-change.txt");
        if(pointCLoud_akaze==null){
            Log.e(TAG,"文件失败");
            return;
        }else{
            Log.e(TAG,pointCLoud_akaze.toString());
        }

        descriptions=pointCLoud_akaze.getDescriptions();
        pointcoordinates=pointCLoud_akaze.getPoints();
//        Log.e(TAG,descriptions.toString());
//        Log.e(TAG,pointcoordinates.toString());


        featureExtract_akaze=new FeatureExtract_AKAZE();
        featureExtract_orb=new FeatureExtract_ORB();
        matcher=new Matcher();
        try {
            InputStream in1=getAssets().open("image/3.jpg");
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
            //featureExtract_orb.extractFeatureandDescription(src1,mask1,keypoint1,description1);
            Log.i(TAG,description1.toString());
            Log.i(TAG,keypoint1.toString());
            //featureExtract_akaze.extractFeatureandDescription(src2,mask2,keypoint2,description2);
            //featureExtract_orb.extractFeatureandDescription(src2,mask2,keypoint2,description2);


            List<MatOfDMatch> dMatches1=new LinkedList<>();
            //List<MatOfDMatch> dMatches2=new LinkedList<>();
            //matcher.Bruteforcematcher(Core.NORM_L2,description1,description2,dMatches1,2);
            matcher.Bruteforcematcher(Core.NORM_L2,description1,descriptions,dMatches1,2);
            //matcher.Bruteforcematcher(Core.NORM_HAMMING,description1,description2,dMatches1,2);
            //matcher.Flannmatcher(Core.NORM_L2,description1,description2,dMatches2,2);
           // matcher.Flannmatcher(Core.NORM_HAMMING,description1,description2,dMatches2,2);
//            TimerHelper.Test(FeatureExtract_AKAZE.class,FeatureExtract_AKAZE.class.getMethod("extractFeatureandDescription",
//                    Mat.class,Mat.class,List.class,Mat.class),TAG,
//                    "特征提取所耗时间： ",src1,mask1,keypoint1,description1);
            //List<MatOfDMatch> define1=new LinkedList<>();
            //List<MatOfDMatch> define2=new LinkedList<>();
            //matcher.MaxandMindistance(dMatches1,define1);
            //matcher.MaxandMindistance(dMatches2,define2);
            List<DMatch> good1=new LinkedList<>();
            //List<DMatch> good2=new LinkedList<>();
            matcher.NNdistanceRatio(dMatches1,1.5f,good1);
            Log.e(TAG, String.valueOf(good1.size()));
            //matcher.NNdistanceRatio(define2,0.8f,good2);
            MatOfPoint3f threepoint=new MatOfPoint3f();
            MatOfPoint2f imagepoint=new MatOfPoint2f();
            SolverPnP.FindKeypointMatch(threepoint,imagepoint,good1,pointcoordinates,keypoint1);
            Mat K=new Mat(3,3,CvType.CV_32FC1);
            float[] a=new float[]{4637,0,2883,0,4637,1288,0,0,1};
            K.put(0,0,a);
            Mat revec=new Mat();
            Mat tvec=new Mat();
            SolverPnP.Solver(threepoint,imagepoint,K, new MatOfDouble(),revec,tvec);
            Log.e(TAG,"旋转向量："+revec);
            Log.e(TAG,"平移  "+tvec);
            double[] r=MatrixTool.matTodouble(revec);
            double[] t=MatrixTool.matTodouble(tvec);
            tvec.get(0,0,t);
            for(int i=0;i<r.length;i++){
                Log.e(TAG,"旋转向量："+r[i]);
            }
            for(int i=0;i<t.length;i++){
                Log.e(TAG,"平移："+t[i]);
            }

            Mat mat=new Mat();
            Calib3d.Rodrigues(revec,mat);
            Log.e(TAG,"旋转矩阵："+mat);
            double[] r_change=MatrixTool.matTodouble(mat);
            for(int i=0;i<r_change.length;i++){
                Log.e(TAG,"旋转矩阵："+r_change[i]);
            }

            Mat mat2=new Mat();
            Mat mattranspose=mat.t();

            double[][] transpose=MatrixTool.matTodoubletwo(mattranspose);
            double[][] td=MatrixTool.matTodoubletwo(tvec);

            double[][] result= MatrixTool.BruteForce(transpose,td);
            for(int i=0;i<result.length;i++){
                for(int j=0;j<result[0].length;j++){
                    Log.e(TAG,"相机位置："+result[i][j]);
                }
            }
        } catch (IOException  e) {
            e.printStackTrace();
        }

    }
}