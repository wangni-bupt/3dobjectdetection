package com.example.a3dobjectdetection.Feature;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.AKAZE;
import org.opencv.features2d.ORB;

public class FeatureExtract_ORB {

    ORB orb;

    public FeatureExtract_ORB(){
        orb=ORB.create();
    }

    public void extractFeatureandDescription(Mat image, Mat mask , MatOfKeyPoint keypoints, Mat descriptions){
        long startTime = System.currentTimeMillis(); // 获取开始时间
        orb.detectAndCompute(image,mask,keypoints,descriptions);
        long endTime = System.currentTimeMillis(); // 获取结束时间
        Log.e("extractFeature","ORB提取所需时间"+(endTime - startTime) + "ms");

    }
}
