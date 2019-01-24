package com.onesatoshi.mm.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.Collections;
import java.util.List;

public class CommonUtil {

    private static final String TAG = CommonUtil.class.getSimpleName();

    public static void printAllApp(Context context) {

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager mPackageManager = context.getPackageManager();
        List<ResolveInfo> mAllApps = mPackageManager.queryIntentActivities(mainIntent, 0);

        //按报名排序
        Collections.sort(mAllApps, new ResolveInfo.DisplayNameComparator(mPackageManager));

        for (ResolveInfo res : mAllApps) {

            //该应用的包名和主Activity
            String pkg = res.activityInfo.packageName;
            String cls = res.activityInfo.name;
            Log.d("pkg_cls", "pkg---" + pkg + "  cls---" + cls);

        }
    }



}
