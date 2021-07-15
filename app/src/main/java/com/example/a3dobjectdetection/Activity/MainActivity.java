package com.example.a3dobjectdetection.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.example.a3dobjectdetection.Helper.CameraPermissionHelper;
import com.example.a3dobjectdetection.Helper.FileHelper;
import com.example.a3dobjectdetection.Helper.SnackbarHelper;
import com.example.a3dobjectdetection.R;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    //用于特征检测和匹配的相关变量
    public static final String FILE_ENCODING = "UTF-8";
    private Vector<Vector<Double>> pointcoordinates;
    private Vector<Double> descriptions ;
    private FileHelper fileHelper;
    private BaseLoaderCallback mLoaderCallback;

    //用于启动ARCore的相关变量
    private Session session;
    private boolean installRequested;
    private CameraPermissionHelper cameraPermissionHelper;
    private SnackbarHelper snackbarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pointcoordinates = new Vector<>();
        descriptions = new Vector<>();
        fileHelper=new FileHelper();

        installRequested=false;
        cameraPermissionHelper=new CameraPermissionHelper();
        snackbarHelper=new SnackbarHelper();

        //加载特征信息文件
        AssetManager assetManager = getResources().getAssets();
        if (fileHelper.getTxtFromAssets(assetManager,"3D-x.txt",pointcoordinates,descriptions)) {
            snackbarHelper.showMessage(this,"文件读取成功");
            Log.i(TAG, "文件读取成功");
            //Log.i(TAG, String.valueOf(pointcoordinates.size()));
            //Log.i(TAG, String.valueOf(descriptions.size()));
        }
        //判断opencv是否加载
        mLoaderCallback = new BaseLoaderCallback(this) {
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
        //启动ARCore
        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                //安装ARCore
                //Log.i(TAG,String.valueOf(ArCoreApk.getInstance().requestInstall(this, !installRequested)));
                //Log.i(TAG, String.valueOf(installRequested));
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }
                //请求相机权限
                //有权限就不请求，没有就再请求
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }
                // 创建一个session
                session = new Session(/* context= */ this);
            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                snackbarHelper.showError(this, message);
                Log.i(TAG, "Exception creating session", exception);
                return;
            }
        }
    }


}