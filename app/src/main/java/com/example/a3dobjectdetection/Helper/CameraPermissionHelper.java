package com.example.a3dobjectdetection.Helper;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CameraPermissionHelper {
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;//xml文件中的android.permission.CAMERA
    private static final int CAMERA_PERMISSION_CODE = 0;
    //查看是否有相机权限
    public static boolean hasCameraPermission(Activity activity) {
        //检查权限是否等于有权限
        return ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION)//检查权限
                == PackageManager.PERMISSION_GRANTED; //有权限: PackageManager.PERMISSION_GRANTED
    }
    //请求权限
    public static void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity, new String[] {CAMERA_PERMISSION}, CAMERA_PERMISSION_CODE);
    }
}
