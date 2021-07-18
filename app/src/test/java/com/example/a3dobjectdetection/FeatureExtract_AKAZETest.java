package com.example.a3dobjectdetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.util.Log;

import com.example.a3dobjectdetection.Feature.FeatureExtract_AKAZE;

import org.junit.Before;
import org.junit.Test;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.osgi.OpenCVNativeLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class FeatureExtract_AKAZETest {

    @Before
    public void StartOpenCV(){
        OpenCVLoader.initDebug();
    }
    @Test
    public void FeatureExtract() throws IOException {
        System.out.println(System.getProperty("java.library.path"));

        File directory = new File("");//参数为空
        String courseFile = directory.getCanonicalPath() ;
        System.out.println(courseFile);
        String path=courseFile+ "/src/test/java/image/1.jpg";
        System.out.println(path);
        Mat src = new Mat();
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Utils.bitmapToMat(bitmap,src);
        System.out.println(src);

//        Mat image1=
//        FeatureExtract_AKAZE featureExtract_akaze1=new FeatureExtract_AKAZE();
//        FeatureExtract_AKAZE featureExtract_akaze2=new FeatureExtract_AKAZE();
//        featureExtract_akaze1.extractFeatureandDescription();
//        featureExtract_akaze2.extractFeatureandDescription();
    }

}
