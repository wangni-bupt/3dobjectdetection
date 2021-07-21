package com.example.a3dobjectdetection.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.media.Image;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.a3dobjectdetection.Helper.CameraPermissionHelper;
import com.example.a3dobjectdetection.Helper.DisplayRotationHelper;
import com.example.a3dobjectdetection.Helper.FileHelper;
import com.example.a3dobjectdetection.Helper.SnackbarHelper;
import com.example.a3dobjectdetection.Helper.TrackingStateHelper;
import com.example.a3dobjectdetection.R;
import com.example.a3dobjectdetection.Render.BackgroundRenderer;
import com.example.a3dobjectdetection.Render.Framebuffer;
import com.example.a3dobjectdetection.Render.GLError;
import com.example.a3dobjectdetection.Render.GpuBuffer;
import com.example.a3dobjectdetection.Render.Mesh;
import com.example.a3dobjectdetection.Render.SampleRender;
import com.example.a3dobjectdetection.Render.Shader;
import com.example.a3dobjectdetection.Render.VertexBuffer;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements SampleRender.Renderer {
    private static final String TAG = MainActivity.class.getSimpleName();
    //用于特征检测和匹配的相关变量
    public static final String FILE_ENCODING = "UTF-8";
    private FileHelper fileHelper;
    private BaseLoaderCallback mLoaderCallback;

    //用于启动ARCore的相关变量
    private Session session;
    private boolean installRequested;
    private CameraPermissionHelper cameraPermissionHelper;
    private SnackbarHelper snackbarHelper;

    //绘图的相关变量
    private SampleRender sampleRender;
    private GLSurfaceView surfaceView;
    private BackgroundRenderer backgroundRenderer;
    private Framebuffer virtualSceneFramebuffer;
    // Point Cloud
    private VertexBuffer pointCloudVertexBuffer;
    private Mesh pointCloudMesh;
    private Shader pointCloudShader;
    private long lastPointCloudTimestamp = 0;

    //cube
    private VertexBuffer cubeVertexBuffer;
    private Mesh cubeMesh;
    private Shader cubeShader;
    private FloatBuffer vectrics;
    private float[] vec;

    private boolean hasSetTextureNames = false;
    private TrackingStateHelper trackingStateHelper;
    private DisplayRotationHelper displayRotationHelper;

    //所使用到的矩阵信息
    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16]; // view x model
    private final float[] modelViewProjectionMatrix = new float[16]; // projection x view x model
    private final float[] sphericalHarmonicsCoefficients = new float[9 * 3];
    private final float[] viewInverseMatrix = new float[16];
    private final float[] worldLightDirection = {0.0f, 0.0f, 0.0f, 0.0f};
    private final float[] viewLightDirection = new float[4]; // view x world light direction

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileHelper=new FileHelper();


        installRequested=false;
        cameraPermissionHelper=new CameraPermissionHelper();
        snackbarHelper=new SnackbarHelper();
        trackingStateHelper=new TrackingStateHelper(this);
        displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

        surfaceView = findViewById(R.id.surfaceview);
        sampleRender = new SampleRender(surfaceView, this, getAssets());

        //加载特征信息文件
        AssetManager assetManager = getResources().getAssets();
        //vectrics=fileHelper.getTxtFromAssets(assetManager,"v.txt");
        vec=fileHelper.getTxtFromAssetsFloat(assetManager,"vv.txt");

        //判断opencv是否加
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
    protected void onPause() {
        super.onPause();
        if (session != null) {
            // 注意：顺序不能改变！必须先暂停GLSurfaceView, 否则GLSurfaceView会继续调用Session的update方法。
            // 但是Session已经pause状态，所以会报SessionPausedException异常
            surfaceView.onPause();
            session.pause();
            displayRotationHelper.onPause();
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
        try {
            configureSession();
            // To record a live camera session for later playback, call
            // `session.startRecording(recorderConfig)` at anytime. To playback a previously recorded AR
            // session instead of using the live camera feed, call
            // `session.setPlaybackDataset(playbackDatasetPath)` before calling `session.resume()`. To
            // learn more about recording and playback, see:
            // https://developers.google.com/ar/develop/java/recording-and-playback
            session.resume();//配置更改了就必须要重新恢复一下
        } catch (CameraNotAvailableException e) {
            snackbarHelper.showError(this, "Camera not available. Try restarting the app.");
            session = null;
            return;
        }

        surfaceView.onResume();//恢复呈现线程，必要时重新创建OpenGL上下文
        displayRotationHelper.onResume();
    }


    @Override//在surface被创建或者重复创建的时候调用
    public void onSurfaceCreated(SampleRender render) {
        //准备渲染物体
        // Prepare the rendering objects. This involves reading shaders and 3D model files, so may throw
        // an IOException.
        try {
            backgroundRenderer = new BackgroundRenderer(render);
            virtualSceneFramebuffer = new Framebuffer(render, /*width=*/ 1, /*height=*/ 1);

            // Point cloud
            pointCloudShader =
                    Shader.createFromAssets(
                            render, "shaders/point_cloud.vert", "shaders/point_cloud.frag", /*defines=*/ null)
                            .setVec4(
                                    "u_Color", new float[] {31.0f / 255.0f, 188.0f / 255.0f, 210.0f / 255.0f, 1.0f})
                            .setFloat("u_PointSize", 5.0f);
            // four entries per vertex: X, Y, Z, confidence
            pointCloudVertexBuffer =
                    new VertexBuffer(render, /*numberOfEntriesPerVertex=*/ 4, /*entries=*/ null);
            final VertexBuffer[] pointCloudVertexBuffers = {pointCloudVertexBuffer};
            pointCloudMesh =
                    new Mesh(
                            render, Mesh.PrimitiveMode.POINTS, /*indexBuffer=*/ null, pointCloudVertexBuffers);
            //cube
            cubeShader=Shader.createFromAssets(
                    render, "shaders/cube.vert", "shaders/cube.frag", /*defines=*/ null)
                    .setVec4(
                            "u_Color", new float[] {31.0f / 255.0f, 188.0f / 255.0f, 210.0f / 255.0f, 1.0f})
                    .setFloat("u_PointSize", 100.0f);
            FloatBuffer floatBuffer= ByteBuffer.allocateDirect(72*4).order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            floatBuffer.put(new float[]{vec[0],vec[1],vec[2],vec[3],vec[4],vec[5],
                    vec[3],vec[4],vec[5],vec[9],vec[10],vec[11],
                    vec[9],vec[10],vec[11],vec[6],vec[7],vec[8],
                    vec[6],vec[7],vec[8],vec[0],vec[1],vec[2],
                    vec[12],vec[13],vec[14],vec[15],vec[16],vec[17],
                    vec[15],vec[16],vec[17],vec[21],vec[22],vec[23],
                    vec[21],vec[22],vec[23],vec[18],vec[19],vec[20],
                    vec[18],vec[19],vec[20],vec[12],vec[13],vec[14],
                    vec[0],vec[1],vec[2],vec[12],vec[13],vec[14],
                    vec[3],vec[4],vec[5],vec[15],vec[16],vec[17],
                    vec[6],vec[7],vec[8],vec[18],vec[19],vec[20],
                    vec[9],vec[10],vec[11],vec[21],vec[22],vec[23]
                    });
            cubeVertexBuffer =
                    new VertexBuffer(render, /*numberOfEntriesPerVertex=*/ 3, /*entries=*/ floatBuffer);

            final VertexBuffer[] cubeVertexBuffers = {cubeVertexBuffer};
            cubeMesh =
                    new Mesh(
                            render, Mesh.PrimitiveMode.LINES, /*indexBuffer=*/ null, cubeVertexBuffers);

        } catch (IOException e) {
            Log.e(TAG, "Failed to read a required asset file", e);
            snackbarHelper.showError(this, "Failed to read a required asset file: " + e);
        }
    }


    @Override
    protected void onDestroy() {
        if (session != null) {
            // Explicitly close ARCore Session to release native resources.
            // Review the API reference for important considerations before calling close() in apps with
            // more complicated lifecycle requirements:
            // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
            session.close();
            session = null;
        }

        super.onDestroy();
    }


    @Override
    public void onSurfaceChanged(SampleRender render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        virtualSceneFramebuffer.resize(width, height);
    }

    @Override
    public void onDrawFrame(SampleRender render) {
        if (session == null) {
            return;
        }

        // Texture names should only be set once on a GL thread unless they change. This is done during
        // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
        // initialized during the execution of onSurfaceCreated.这是在onDrawFrame
        // 而不是onsurfacecrecreated期间完成的，因为在onsurfacecrecreated执行期间，不能保证会话已经初始化。
        if (!hasSetTextureNames) {
            session.setCameraTextureNames(
                    new int[] {backgroundRenderer.getCameraColorTexture().getTextureId()});
            hasSetTextureNames = true;
        }

        // -- Update per-frame state
        displayRotationHelper.updateSessionIfNeeded(session);

        // Obtain the current frame from ARSession. When the configuration is set to
        // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
        // camera framerate.从ARSession中获取当前帧。当配置设置为UpdateMode时。阻塞(默认情况下)，这将抑制渲染到相机帧率。
        Frame frame;
        try {
            frame = session.update();
        } catch (CameraNotAvailableException e) {
            Log.e(TAG, "Camera not available during onDrawFrame", e);
            snackbarHelper.showError(this, "Camera not available. Try restarting the app.");
            return;
        }
        Camera camera = frame.getCamera();

        // Update BackgroundRenderer state .
        try {
            backgroundRenderer.setbackgroundshader(render);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read a required asset file", e);
            snackbarHelper.showError(this, "Failed to read a required asset file: " + e);
            return;
        }
        // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
        // used to draw the background camera image.BackgroundRenderer。updateDisplayGeometry必须在每一帧调用，以更新用于绘制背景摄像机图像的坐标。
        backgroundRenderer.updateDisplayGeometry(frame);


        //在跟踪时保持屏幕解锁，但在跟踪停止时允许它锁定
        // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
        trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

        // Show a message based on whether tracking has failed, if planes are detected, and if the user
        // has placed any objects.
        String message = null;
        if (camera.getTrackingState() == TrackingState.PAUSED) {
            if (camera.getTrackingFailureReason() == TrackingFailureReason.NONE) {
                message = "None";
            } else {
                message = TrackingStateHelper.getTrackingFailureReasonString(camera);
            }
        }

        if (message == null) {
            snackbarHelper.hide(this);
        } else {
            snackbarHelper.showMessage(this, message);
        }

        // -- Draw background

        if (frame.getTimestamp() != 0) {
            // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
            // drawing possible leftover data from previous sessions if the texture is reused.
            backgroundRenderer.drawBackground(render);
        }

        // If not tracking, don't draw 3D objects.
        if (camera.getTrackingState() == TrackingState.PAUSED) {
            return;
        }

        // -- Draw non-occluded virtual objects (planes, point cloud)

        // Get projection matrix.获取投影矩阵
        camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);

        // Get camera matrix and draw.获取相机矩阵？
        camera.getViewMatrix(viewMatrix, 0);

        // Visualize tracked points.
        // Use try-with-resources to automatically release the point cloud.
        try (PointCloud pointCloud = frame.acquirePointCloud()) {
            if (pointCloud.getTimestamp() > lastPointCloudTimestamp) {
                pointCloudVertexBuffer.set(pointCloud.getPoints());
                lastPointCloudTimestamp = pointCloud.getTimestamp();
            }
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
            pointCloudShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
            render.draw(pointCloudMesh, pointCloudShader);
        }
        //if(vectrics.isDirect()) Log.e(TAG," sdf");
        cubeShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
        render.draw(cubeMesh, cubeShader);

    }
    private void configureSession() {
        Config config = session.getConfig();
        //对焦
        config.setFocusMode(Config.FocusMode.AUTO);
        //config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
        config.setDepthMode(Config.DepthMode.DISABLED);
        Log.i(TAG, String.valueOf(config.getDepthMode()));

        config.setInstantPlacementMode(Config.InstantPlacementMode.DISABLED);
        Log.i(TAG, String.valueOf(config.getInstantPlacementMode()));

        session.configure(config);
    }
}