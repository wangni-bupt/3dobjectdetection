package com.example.a3dobjectdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Target;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String FILE_ENCODING = "UTF-8";
    private Vector<Vector<Double>> pointcoordinates = new Vector<>();
    private Vector<Double> descriptions = new Vector<>();
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getTxtFromAssets("3D-x.txt")) {
            Log.i(TAG, "文件读取成功");
            Log.i(TAG, String.valueOf(pointcoordinates.size()));
            Log.i(TAG, String.valueOf(descriptions.size()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //加载opencv和库
        if (!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this,mLoaderCallback);
        }else{
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private boolean getTxtFromAssets(String fileName) {
        try {
            InputStream inputStream = getResources().getAssets().open(fileName);
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
            Log.e(TAG, "wrong");
            e.printStackTrace();
        }
        return false;
    }
}