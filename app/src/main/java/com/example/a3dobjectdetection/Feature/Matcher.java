package com.example.a3dobjectdetection.Feature;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.FlannBasedMatcher;

import java.util.LinkedList;
import java.util.List;

public class Matcher {
    private final static String TAG=Matcher.class.getSimpleName();

    //Brute-force descriptor matcher.
    public void Bruteforcematcher(int normType,Mat querydescriptions, Mat traindescriptions,
                                  List<MatOfDMatch> dMatches, int number){
        BFMatcher bfMatcher=BFMatcher.create(normType);
        long startTime = System.currentTimeMillis(); // 获取开始时间
        bfMatcher.knnMatch(querydescriptions,traindescriptions,dMatches,2);
        long endTime = System.currentTimeMillis(); // 获取结束时间
        Log.e("Bruteforcematcher","暴力破解所需时间"+ (endTime - startTime) + "ms");
    }
    //Flann-based descriptor matcher. ORB不能使用不知道为什么，好像FLANN默认是L2Distance = cvflann::L2<float>
    public void Flannmatcher(int normType,Mat querydescriptions, Mat traindescriptions,
                             List<MatOfDMatch> dMatches,int number){
        FlannBasedMatcher flannBasedMatcher= FlannBasedMatcher.create();
        long startTime = System.currentTimeMillis(); // 获取开始时间
        flannBasedMatcher.knnMatch(querydescriptions,traindescriptions,dMatches,2);
        long endTime = System.currentTimeMillis(); // 获取结束时间
        Log.e("Flannmatcher","FLANN所需时间"+ (endTime - startTime) + "ms");
    }

    public void ListMatofDMatchToListDMatch(List<MatOfDMatch> list1,List<DMatch> list2){
        for(int i=0;i<list1.size();i++){
            List<DMatch> matOfDMatch=list1.get(i).toList();
            for(DMatch dMatch:matOfDMatch) {
                list2.add(dMatch);
            }
        }
    }

    //Nearest neighbor distance ratio filtering ( a < fratio * b)  设定每2个做一次比较
    public void NNdistanceRatio(List<MatOfDMatch> dMatches,float minRatio,List<DMatch> matches){
        long startTime = System.currentTimeMillis(); // 获取开始时间
        //Log.e(TAG, String.valueOf(dMatches.size()));
        for(int i=0;i<dMatches.size();i++){
            MatOfDMatch matOfDMatch=dMatches.get(i);
            //Log.e(TAG,matOfDMatch.toString());
            DMatch[] dMatch=matOfDMatch.toArray();
            //Log.e(TAG, String.valueOf(dMatch.length));
            //if(dMatch.length<=1) continue;
            float distanceRatio = dMatch[0].distance/dMatch[1].distance;
            //Log.e(TAG, String.valueOf(distanceRatio));
            // Pass only matches where distance ratio between
            // nearest matches is greater than 1.5
            // (distinct criteria)
            if (distanceRatio < minRatio) {
                matches.add(dMatch[0]);
            }
        }
        long endTime = System.currentTimeMillis(); // 获取结束时间
        Log.e("NNdistanceRatio","ratio滤波"+ (endTime - startTime) + "ms");
    }

    public void MaxandMindistance(List<MatOfDMatch> dMatches,List<MatOfDMatch> goodMatch){
        long startTime = System.currentTimeMillis(); // 获取开始时间
        double maxDist = Double.MIN_VALUE;
        double minDist = Double.MAX_VALUE;
        for(int j=0;j<dMatches.size();j++){
            DMatch[] mats=dMatches.get(j).toArray();
            for (int i = 0; i < mats.length; i++) {
                double dist = mats[i].distance;
                if (dist < minDist) {
                    minDist = dist;
                }
                if (dist > maxDist) {
                    maxDist = dist;
                }
            }
        }
        System.out.println("Min Distance:" + minDist);
        System.out.println("Max Distance:" + maxDist);

        for (int i = 0; i < dMatches.size(); i++) {
            DMatch[] mats=dMatches.get(i).toArray();
            MatOfDMatch matOfDMatch=new MatOfDMatch();
            List<DMatch> list=new LinkedList<>();
            for (int j = 0; j < mats.length; j++) {
                double dist = mats[j].distance;
                if (dist < 2 * minDist) {
                    list.add(mats[j]);
                }
            }
            if(list.size()!=0){
                matOfDMatch.fromList(list);
                //Log.e(TAG,matOfDMatch.toString());
                goodMatch.add(matOfDMatch);
            }
        }
        Log.e(TAG, "除去最大最小"+String.valueOf(goodMatch.size()));
        long endTime = System.currentTimeMillis(); // 获取结束时间
        Log.e("MaxandMindistance","除去最大最小"+ (endTime - startTime) + "ms");
    }
}
