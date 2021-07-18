package com.example.a3dobjectdetection.Feature;


import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;

import java.util.LinkedList;
import java.util.List;

public class SolverPnP {
    private final static  String TAG=SolverPnP.class.getSimpleName();

    //2d是搜寻的
    public static void FindKeypointMatch(MatOfPoint3f matOfPoint3f, MatOfPoint2f imagePoints, List<DMatch> list,
                                         MatOfPoint3f threepoints, MatOfKeyPoint matOfKeyPoint){
        List<Point> save=new LinkedList<>();
        KeyPoint[] keyPoint=matOfKeyPoint.toArray();
        for(DMatch d:list){
            //Log.e(TAG, String.valueOf(d.trainIdx)+" "+String.valueOf(d.queryIdx));
            matOfPoint3f.push_back(threepoints.row(d.trainIdx));

            Mat mat=new Mat(1,1, CvType.CV_32FC2);
            save.add(keyPoint[d.queryIdx].pt);
        }
        imagePoints.fromList(save);
        Log.e(TAG,imagePoints.toString());
        Log.e(TAG,matOfPoint3f.toString());
    }


    public static void Solver(MatOfPoint3f objectPoints, MatOfPoint2f imagePoints, Mat cameraMatrix, MatOfDouble distCoeffs, Mat rvec, Mat tvec){
        long startTime = System.currentTimeMillis(); // 获取开始时间
        Calib3d.solvePnPRansac(objectPoints,imagePoints,cameraMatrix,distCoeffs,rvec,tvec);
        long endTime = System.currentTimeMillis(); // 获取结束时间
        Log.e("Solver","pnp求解时间"+ (endTime - startTime) + "ms");
    }
}
