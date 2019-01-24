package com.onesatoshi.mm.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SimplePreference {

    private static String FILLNAME = "config";// 文件名称
    private static SharedPreferences mSimplePreferences = null;

    public final static String PRE_ACCOUNT = "account";
    public final static String PRE_USERID = "userid";
    public final static String PRE_TOKENID = "tokenid";
    public final static String PRE_USERNAME = "username";
    public final static String PRE_USEREMAIL = "useremail";
    public final static String PRE_PHONE = "phone";

    /**
     * 单例模式
     */
    public static synchronized SharedPreferences getInstance(Context context) {
        if (mSimplePreferences == null) {
            mSimplePreferences = context.getApplicationContext().getSharedPreferences(FILLNAME, Context.MODE_PRIVATE);
        }
        return mSimplePreferences;
    }

    /**
     * SimplePreferences常用的10个操作方法
     */
    public static void putBoolean(String key, boolean value, Context context) {
        SimplePreference.getInstance(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key, boolean defValue, Context context) {
        return SimplePreference.getInstance(context).getBoolean(key, defValue);
    }

    public static void putString(String key, String value, Context context) {
        SimplePreference.getInstance(context).edit().putString(key, value).apply();
    }

    public static String getString(String key, String defValue, Context context) {
        return SimplePreference.getInstance(context).getString(key, defValue);
    }

    public static void putInt(String key, int value, Context context) {
        SimplePreference.getInstance(context).edit().putInt(key, value).apply();
    }

    public static int getInt(String key, int defValue, Context context) {
        return SimplePreference.getInstance(context).getInt(key, defValue);
    }

    /**
     * 移除某个key值已经对应的值
     */
    public static void remove(String key, Context context) {
        SimplePreference.getInstance(context).edit().remove(key).apply();
    }

    /**
     * 清除所有内容
     */
    public static void clear(Context context) {
        SimplePreference.getInstance(context).edit().clear().apply();
    }
}
