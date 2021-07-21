package com.example.a3dobjectdetection.Helper;

import android.content.res.AssetManager;
import android.util.Log;

import com.example.a3dobjectdetection.Tools.PointCLoud_AKAZE;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class FileHelper {
    private static final String TAG = FileHelper.class.getSimpleName();

    public boolean getTxtFromAssets(AssetManager assetManager, String fileName,
                             Vector<Vector<Double>> pointcoordinates, Vector<Double> descriptions) {
        try {
            InputStream inputStream =assetManager.open(fileName);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            while ((line = bufReader.readLine()) != null) {
                Vector<Double> point = new Vector<>();
                String[] tokens = line.split(" ");
                point.add(Double.parseDouble(tokens[0]));
                point.add(Double.parseDouble(tokens[1]));
                point.add(Double.parseDouble(tokens[2]));
                pointcoordinates.add(point);
                line = bufReader.readLine();
                descriptions.add(Double.parseDouble(line));
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "读取txt文件出错");
            e.printStackTrace();
        }
        return false;
    }

    public FloatBuffer getTxtFromAssets(AssetManager assetManager, String fileName) {
        FloatBuffer floatBuffer= ByteBuffer.allocateDirect(96).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        try {
            InputStream inputStream =assetManager.open(fileName);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            while ((line = bufReader.readLine()) != null) {
                String[] tokens = line.split(" ");
                floatBuffer.put(Float.parseFloat(tokens[0]));
                floatBuffer.put(Float.parseFloat(tokens[1]));
                floatBuffer.put(Float.parseFloat(tokens[2]));
            }
            return floatBuffer;
        } catch (Exception e) {
            Log.e(TAG, "读取txt文件出错");
            e.printStackTrace();
        }
        return null;
    }

    public float[] getTxtFromAssetsFloat(AssetManager assetManager, String fileName) {
        float[] floats=new float[24];
        try {
            InputStream inputStream =assetManager.open(fileName);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            int i=0;
            while ((line = bufReader.readLine()) != null) {
                String[] tokens = line.split(" ");
                floats[i++]=Float.parseFloat(tokens[0]);
                floats[i++]=Float.parseFloat(tokens[1]);
                floats[i++]=Float.parseFloat(tokens[2]);
            }
            //Log.e(TAG, String.valueOf(floats[23]));
            return floats;
        } catch (Exception e) {
            Log.e(TAG, "读取txt文件出错");
            e.printStackTrace();
        }
        return null;
    }

    public boolean getTxtFromAssetsToMat(AssetManager assetManager, String fileName,
                                         List<Point3> pointcoordinates, List<Float> descriptions) {
        try {
            InputStream inputStream =assetManager.open(fileName);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            while ((line = bufReader.readLine()) != null) {

                String[] tokens = line.split(" ");
                Point3 point=new Point3( Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]));
                pointcoordinates.add(point);
                //Log.e(TAG,String.valueOf(list));
                line = bufReader.readLine();
                String[] des = line.split(" ");
                for(int i=0;i<64;i++){
                    descriptions.add(Float.parseFloat(des[i]));
                }
                //Log.e(TAG,String.valueOf(data));
            }
            //Log.e(TAG,"over");
            //array2Mat(data,descriptions);
            Log.e(TAG, String.valueOf(pointcoordinates.size()));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "读取txt文件出错");
            e.printStackTrace();
        }
        return false;
    }

    public PointCLoud_AKAZE getTxtFromAssetsToMatChange(AssetManager assetManager, String fileName) {
        try {
            InputStream inputStream =assetManager.open(fileName);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            line = bufReader.readLine();
            int size=Integer.parseInt(line);
            PointCLoud_AKAZE pointCLoud_akaze=new PointCLoud_AKAZE(size);
            while ((line = bufReader.readLine()) != null) {
                String[] tokens = line.split(" ");
                Point3 point=new Point3( Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]));
                pointCLoud_akaze.addPoint(point);
                //Log.e(TAG,String.valueOf(list));
                line = bufReader.readLine();
                String[] des = line.split(" ");
                float[] floats=new float[64];
                for(int i=0;i<64;i++){
                    floats[i]=Float.parseFloat(des[i]);
                }
                pointCLoud_akaze.addDescription(floats);
                //Log.e(TAG,pointCLoud_akaze.getDescriptions().toString());
                //Log.e(TAG,String.valueOf(data));
            }
            return pointCLoud_akaze;
        } catch (Exception e) {
            Log.e(TAG, "读取txt文件出错");
            e.printStackTrace();
        }
        return null;
    }

    public void  VectorToMat(List<Point3> pointcoordinates, List<Float> descriptions,
                               MatOfPoint3f point3f,Mat descriptionsmat){
        float[] floats=new float[descriptions.size()];
        for(int i=0;i<descriptions.size();i++){
            floats[i]=descriptions.get(i);
        }
        point3f.fromList(pointcoordinates);
        //descriptions = new Mat(list.size(), 64, CvType.CV_32FC1);
        descriptionsmat.put(0,0,floats);
        //Log.e(TAG,descriptionsmat.toString());
        //Log.e(TAG,pointcoordinates.toString());

    }

    public static void array2Mat(float[][][] data,Mat mat) {

        int height = data.length;
        int width = data[0].length;
        mat = new Mat(height, width, CvType.CV_32FC1);//AKAZE是这个类型
        float[] mat_data = new float[width * height * 1];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                mat_data[i * width * 1 + j * 1] = data[i][j][0];
            }
        }
        mat.put(0, 0, mat_data);
    }
}
