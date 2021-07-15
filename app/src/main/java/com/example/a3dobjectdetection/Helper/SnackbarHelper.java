package com.example.a3dobjectdetection.Helper;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarHelper {

    private static final int BACKGROUND_COLOR = 0xbf323232;//背景颜色
    private Snackbar messageSnackbar;
    private String lastMessage = "";//存储上一条消息
    private enum DismissBehavior { HIDE, SHOW, FINISH };//dismiss的类型 隐藏和显示，控制button
    private View snackbarView;
    private int maxLines = 2;

    //是否能显示，判断是否有messageSnackar存在
    public boolean isShowing() {
        return messageSnackbar != null;
    }
    //使用snackar显示信息
    public void showMessage(Activity activity, String message) {
        if (!message.isEmpty() && (!isShowing() || !lastMessage.equals(message))) {
            lastMessage = message;
            show(activity, message, DismissBehavior.HIDE);
        }
    }
    //显示错误信息，并直接结束活动
    public void showError(Activity activity, String errorMessage) {
        show(activity, errorMessage, DismissBehavior.FINISH);
    }

    //隐藏当前snackbar
    public void hide(Activity activity) {
        if (!isShowing()) {
            return;
        }
        lastMessage = "";
        Snackbar messageSnackbarToHide = messageSnackbar;
        messageSnackbar = null;
        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        messageSnackbarToHide.dismiss();
                    }
                });
    }

    private void show(
            final Activity activity, final String message, final DismissBehavior dismissBehavior) {
        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        messageSnackbar =
                                Snackbar.make(
                                        snackbarView == null
                                                ? activity.findViewById(android.R.id.content)
                                                : snackbarView,
                                        message,
                                        Snackbar.LENGTH_SHORT);
                        messageSnackbar.getView().setBackgroundColor(BACKGROUND_COLOR);
                        if (dismissBehavior != DismissBehavior.HIDE) {
                            messageSnackbar.setAction(
                                    "Dismiss",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            messageSnackbar.dismiss();
                                        }
                                    });
                            //如果为finish，那么直接活动结束
                            if (dismissBehavior == DismissBehavior.FINISH) {
                                messageSnackbar.addCallback(
                                        new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                            @Override
                                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                                super.onDismissed(transientBottomBar, event);
                                                activity.finish();
                                            }
                                        });
                            }
                        }
                        ((TextView)
                                messageSnackbar
                                        .getView()
                                        .findViewById(com.google.android.material.R.id.snackbar_text))
                                .setMaxLines(maxLines);
                        messageSnackbar.show();
                    }
                });
    }
}
