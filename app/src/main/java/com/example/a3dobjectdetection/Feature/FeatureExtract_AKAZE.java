package com.example.a3dobjectdetection.Feature;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.AKAZE;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.FlannBasedMatcher;
import org.opencv.features2d.ORB;

import java.util.LinkedList;
import java.util.List;


public class FeatureExtract_AKAZE {
    //https://docs.opencv.org/3.4/javadoc/org/opencv/features2d/package-summary.html
    AKAZE akaze;
    //ORB orb;

    public FeatureExtract_AKAZE(){
        akaze=AKAZE.create(AKAZE.DESCRIPTOR_KAZE,4,4,0.001f);
        //akaze=AKAZE.create(AKAZE.DESCRIPTOR_MLDB);
        //orb=ORB.create();
    }

    public void extractFeatureandDescription(Mat image, Mat mask ,MatOfKeyPoint keypoints,Mat descriptions){
        long startTime = System.currentTimeMillis(); // 获取开始时间
        akaze.detectAndCompute(image,mask,keypoints,descriptions);
        long endTime = System.currentTimeMillis(); // 获取结束时间
        Log.e("extractFeature","AKAZE提取所需时间"+(endTime - startTime) + "ms");
        //orb.detectAndCompute(image,mask,keypoints,descriptions);
    }




}
