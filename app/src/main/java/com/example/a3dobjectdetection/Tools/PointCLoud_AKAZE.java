package com.example.a3dobjectdetection.Tools;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;

public class PointCLoud_AKAZE {
    private final int type= CvType.CV_32FC1;
    private final int cols=64;
    private int rows;
    private Mat descriptions;
    private MatOfPoint3f points;

    public PointCLoud_AKAZE(int rows){
        this.rows=rows;
        descriptions=new Mat(0,0,type);
        points=new MatOfPoint3f();
    }

    public Mat getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Mat descriptions) {
        this.descriptions = descriptions;
    }

    public MatOfPoint3f getPoints() {
        return points;
    }

    public void setPoints(MatOfPoint3f points) {
        this.points = points;
    }

    public void addPoint(Point3 point){
        MatOfPoint3f mat=new MatOfPoint3f(point);
        points.push_back(mat);
    }

    public void addDescription(float[] floats){
        Mat mat=new Mat(1,cols,type);
        mat.put(0,0,floats);
        descriptions.push_back(mat);
    }

    @Override
    public String toString() {
        return "PointCLoud_AKAZE{" +
                "descriptions=" + descriptions +
                ", points=" + points +
                '}';
    }
}
