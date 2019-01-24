package com.onesatoshi.mm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.onesatoshi.mm.util.AccessibilityUtil;
import com.onesatoshi.mm.util.DeviceUtil;
import com.onesatoshi.mm.util.SimplePreference;
import com.onesatoshi.mm.util.StringUtils;
import com.tencent.bugly.Bugly;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class WxRobotActivity extends AppCompatActivity {

    private CheckBox cb_assist;
    private CheckBox cb_window;

    public static final String TAG = WxRobotActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 关闭提示框
            closeAndroidPDialog();
        }

        setContentView(R.layout.activity_main);

        RadioGroup rgroup = findViewById(R.id.radioGroup);
        rgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                              @Override

                                              public void onCheckedChanged(RadioGroup rg, int checkedId) {

                      if (!AccessibilityUtil.isSettingOpen(WxRobotActivity.this, WxRobotService.class)) {
                          Toast.makeText(WxRobotActivity.this, "辅助功能未开启", Toast.LENGTH_SHORT).show();

                          rg.setOnCheckedChangeListener(null);
                          rg.check(-1);
                          rg.setOnCheckedChangeListener(this);

                          return;
                      }

                      Log.i(TAG, "mask:" + Config.taskMask + ",checkedId:" + checkedId);

                      switch (checkedId) {

                          /**
                           * 自动抢红包
                           */
                          case R.id.cb_lucky_money:
                              Config.taskMask = Config.MASK_AUTO_OPEN_LUCKY_MONEY;
                              Log.d(TAG, "自动抢红包功能，掩码设置成功");

                              break;
                      }

                      Log.i(TAG, "mask:" + Config.taskMask + ",checkedId:" + checkedId);
                  }
              }
        );

        // 辅助功能
        cb_assist = (CheckBox) findViewById(R.id.cb_assist_permission);
        if (cb_assist != null) {
            cb_assist.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked && !AccessibilityUtil.isSettingOpen(WxRobotActivity.this, WxRobotService.class)) {
                        /**
                         * 申请权限
                         */
                        AccessibilityUtil.checkSetting(WxRobotActivity.this, WxRobotService.class);

                    }
                }
            });
        }

        // 悬浮框
        cb_window = (CheckBox) findViewById(R.id.cb_show_window);
        if (cb_window != null) {
            cb_window.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.getId() == R.id.cb_show_window) {

                        /**
                         * 展示悬浮框
                         */
                        if (isChecked) {
                            requestFloatWindowPermissionIfNeeded();

                            TasksWindow.init(getApplicationContext());
                            TasksWindow.show("一聪助手使用......");
                        } else {
                            TasksWindow.dismiss();
                        }

                    }
                }
            });
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //获取保存在sd中的 设备唯一标识符
                    String readDeviceID = DeviceUtil.readDeviceID(WxRobotActivity.this);

                    //获取缓存在  sharepreference 里面的 设备唯一标识
                    String string = SimplePreference.getString(SpConstant.SP_DEVICES_ID, readDeviceID, getApplicationContext());

                    //判断 app 内部是否已经缓存,  若已经缓存则使用app 缓存的 设备id
                    if (string != null) {

                        //app 缓存的和SD卡中保存的不相同 以app 保存的为准, 同时更新SD卡中保存的 唯一标识符
                        if (StringUtils.isBlank(readDeviceID) && !string.equals(readDeviceID)) {

                            // 取有效地 app缓存 进行更新操作
                            if (StringUtils.isBlank(readDeviceID) && !StringUtils.isBlank(string)) {
                                readDeviceID = string;
                                DeviceUtil.saveDeviceID(readDeviceID, WxRobotActivity.this);
                            }
                        }
                    }

                    // app 没有缓存 (这种情况只会发生在第一次启动的时候)
                    if (StringUtils.isBlank(readDeviceID)) {

                        //保存设备id
                        readDeviceID = DeviceUtil.getDeviceId(WxRobotActivity.this);
                    }

                    //左后再次更新app 的缓存
                    SimplePreference.putString(SpConstant.SP_DEVICES_ID, readDeviceID, getApplicationContext());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        TextView deviceView = findViewById(R.id.deviceId);
        deviceView.setText(SimplePreference.getString(SpConstant.SP_DEVICES_ID, DeviceUtil.getDeviceId(getApplicationContext()), getApplicationContext()));

        Bugly.init(getApplicationContext(), "dd89cee0f6", false);


        // 默认选中
        cb_window.setChecked(true);

    }


    @Override
    protected void onResume() {

        // 设置选中状态
        cb_assist.setChecked(AccessibilityUtil.isSettingOpen(WxRobotActivity.this, WxRobotService.class));

        // 悬浮框
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            cb_window.setChecked(Settings.canDrawOverlays(this));

            if (!Settings.canDrawOverlays(this)) {
                requestFloatWindowPermissionIfNeeded();
            }
        }

        super.onResume();
    }

    //onPause()方法注销
    @Override
    protected void onPause() {

        Log.i(TAG, "pause暂停");
        super.onPause();
    }

    /**
     * 申请悬浮窗权限
     */
    private void requestFloatWindowPermissionIfNeeded() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            new AlertDialog.Builder(this)
                    .setMessage(R.string.dialog_enable_overlay_window_msg)
                    .setPositiveButton(R.string.dialog_enable_overlay_window_positive_btn
                            , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();

                                    // 打开权限设置窗口
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                    intent.setData(Uri.parse("package:" + getPackageName()));

                                    startActivity(intent);
                                }
                            })

                    .setNegativeButton(android.R.string.cancel
                            , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    cb_window.setChecked(false);
                                }
                            })

                    .create()
                    .show();

        }
    }

    /**
     * 在MIUI 10升级到 Android P 后 每次进入程序都会弹一个提醒弹窗
     * 去掉在Android P上的提醒弹窗 （Detected problems with API compatibility(visit g.co/dev/appcompat for more info)
     */
    private void closeAndroidPDialog() {
        try {

            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);

            declaredConstructor.setAccessible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");

            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);

            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
