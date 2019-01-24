package com.onesatoshi.mm;

/**
 * 保存全局变量
 */
public class Config {

    /**
     * 是否打开自动抢红包
     */
    public static final int MASK_AUTO_OPEN_LUCKY_MONEY = 0x01;

    /**
     * 是否打开自动添加附近的人
     */
    public static final int MASK_AUTO_ADD_NEARBY = 0x01 << 1;

    /**
     * 是否打开自动通讯录
     */
    public static final int MASK_AUTO_ADD_PHONEBOOK = 0x01 << 2;

    /**
     * 是否打开自动添加客户
     */
    public static final int MASK_AUTO_ADD_CUSTOMER = 0x01 << 3;

    /**
     * 是否自动点赞
     */
    public static final int MASK_AUTO_SNS_PRIZE = 0x01 << 4;

    /**
     * 自动添加群好友
     */
    public static final int MASK_AUTO_ACCEPT_FRIEND_ADD_2GROUP = 0x01 << 5;

    /**
     * 自动发布朋友圈
     */
    public static final int MASK_AUTO_POST_SNS = 0x01 << 6;

    /**
     * 是否打开自动回复
     */
    public static final int MASK_AUTO_REPLY = 0x01 << 7;

    /**
     * 是否打开自动抓取群信息
     */
    public static final int MASK_AUTO_CRAWL_GROUPMSG = 0x01 << 8;


    public static int taskMask = 0;

    public static final int MASK_DEFAULT = 0x00;


    // 服务器地址
    public static final String SERVER_URL = "http:\\/\\/45.77.248.237:9010";

    // query handler执行周期
    public static long QUERY_RUNNABLE_PERIOD = 2 * 60 * 1000;

    // push handler执行周期
    public static long PUSH_RUNNABLE_PERIOD = 30 * 1000;

    // query handler默认执行周期
    public static long QUERY_RUNNABLE_PERIOD_DEFAULT = 2 * 60 * 1000;

    // query handler启动延时
    public static long RUNNABLE_DELAY = 10 * 1000;
}