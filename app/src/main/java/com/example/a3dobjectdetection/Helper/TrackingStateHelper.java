package com.example.a3dobjectdetection.Helper;

import android.app.Activity;
import android.view.WindowManager;

import com.google.ar.core.Camera;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.TrackingState;

//管理跟踪状态
public class TrackingStateHelper {

    private static final String INSUFFICIENT_FEATURES_MESSAGE =
            "Can't find anything. Aim device at a surface with more texture or color.";
    private static final String EXCESSIVE_MOTION_MESSAGE = "Moving too fast. Slow down.";
    private static final String INSUFFICIENT_LIGHT_MESSAGE =
            "Too dark. Try moving to a well-lit area.";
    private static final String BAD_STATE_MESSAGE =
            "Tracking lost due to bad internal state. Please try restarting the AR experience.";
    private static final String CAMERA_UNAVAILABLE_MESSAGE =
            "Another app is using the camera. Tap on this app or try closing the other one.";

    private final Activity activity;

    private TrackingState previousTrackingState;

    public TrackingStateHelper(Activity activity) {
        this.activity = activity;
    }
    //在跟踪时保持屏幕解锁，但在跟踪停止时允许它锁定。
    /** Keep the screen unlocked while tracking, but allow it to lock when tracking stops. */
    public void updateKeepScreenOnFlag(TrackingState trackingState) {
        if (trackingState == previousTrackingState) {
            return;
        }

        previousTrackingState = trackingState;
        switch (trackingState) {
            case PAUSED:
            case STOPPED:
                activity.runOnUiThread(
                        () -> activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));//清除标志中指定的标志位,即无需全屏
                break;
            case TRACKING:
                activity.runOnUiThread(
                        () -> activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));//全屏
                break;
        }
    }

    public static String getTrackingFailureReasonString(Camera camera) {
        TrackingFailureReason reason = camera.getTrackingFailureReason();
        switch (reason) {
            case NONE:
                return "";
            case BAD_STATE:
                return BAD_STATE_MESSAGE;
            case INSUFFICIENT_LIGHT:
                return INSUFFICIENT_LIGHT_MESSAGE;
            case EXCESSIVE_MOTION:
                return EXCESSIVE_MOTION_MESSAGE;
            case INSUFFICIENT_FEATURES:
                return INSUFFICIENT_FEATURES_MESSAGE;
            case CAMERA_UNAVAILABLE:
                return CAMERA_UNAVAILABLE_MESSAGE;
        }
        return "Unknown tracking failure reason: " + reason;
    }
}
