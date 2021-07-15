package com.example.a3dobjectdetection.Helper;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
}
