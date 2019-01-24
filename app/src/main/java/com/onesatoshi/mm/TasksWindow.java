package com.onesatoshi.mm;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class TasksWindow {

    private static WindowManager.LayoutParams sWindowParams;

    private static WindowManager sWindowManager;

    private static View infoView = null;

    private static final String TAG = TasksWindow.class.getSimpleName();

    public static void init(Context context) {

        if (context == null) {
            Log.e(TAG, "context cannot be null");
            return;
        }

        if (sWindowManager == null) {
            sWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }


        if (sWindowParams == null) {
            sWindowParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.N ? WindowManager.LayoutParams.TYPE_TOAST : WindowManager.LayoutParams.TYPE_PHONE, 0x18,
                    PixelFormat.TRANSLUCENT);

            sWindowParams.gravity = Gravity.RIGHT | Gravity.TOP;
        }

        if (infoView == null) {
            infoView = LayoutInflater.from(context).inflate(R.layout.float_window_info, null);

        }
    }


    /**
     * 展示
     */
    public static void show(String text) {

        if (infoView == null) {
            Log.e(TAG, "infoView cannot be null");
            return;
        }

        TextView tv_name = (TextView) infoView.findViewById(R.id.tv_name);
        tv_name.setText(text);

        try {

            sWindowManager.addView(infoView, sWindowParams);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void changeMsg(String text) {

        if (infoView == null) {
            Log.e(TAG, "infoView cannot be null");
            return;
        }

        TextView tv_name = (TextView) infoView.findViewById(R.id.tv_name);
        tv_name.setText(text);

    }

    /**
     * 销毁
     */
    public static void dismiss() {

        if (infoView != null) {

            try {
                sWindowManager.removeViewImmediate(infoView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "infoView is null");
        }
    }
}
