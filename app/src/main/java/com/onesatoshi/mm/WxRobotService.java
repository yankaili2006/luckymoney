package com.onesatoshi.mm;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.onesatoshi.mm.util.AccessibilityUtil;

import java.util.List;

import static com.onesatoshi.mm.Config.taskMask;

/**
 * 辅助服务
 */
public class WxRobotService extends AccessibilityService {

    public static final String TAG = WxRobotService.class.getName();

    private int luckyMoneyStep = -1;

    private int scrollNum = 0;

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {

        int eventType = event.getEventType();

        if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return;
        }

        Log.d(TAG, event.toString());

        if (event.getPackageName() != null && "com.tencent.mm".equals(event.getPackageName().toString())) {

            // 修改悬浮框 信息
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

                TasksWindow.changeMsg(event.getPackageName() + "\n" + event.getClassName());
                Log.d(TAG, event.getPackageName() + "\t" + event.getClassName());
            }

            switch (Config.taskMask) {

                // 自动抢红包
                case Config.MASK_AUTO_OPEN_LUCKY_MONEY:

                    processLuckMoneyEvent(event);

                    break;

                default:

                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);

                    Log.e(TAG, "mask is default:" + taskMask);
                    break;
            }
        }
    }


    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        Log.i(TAG, "服务已开启");

        Toast.makeText(this, "服务已开启", Toast.LENGTH_SHORT).show();


    }


    /**
     * 有人发信息 记录信息内容 会直接点开信息
     *
     * @param event
     */
    private boolean handleNotification(AccessibilityEvent event, String keyWord) {

        //通知栏事件
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {

            Notification notification = (Notification) event.getParcelableData();

            // 事件附加信息
            String tickerText = String.valueOf(notification.tickerText);
            Log.i(TAG, "接收到通知，【" + tickerText + "】");


            if (tickerText != null && !"".equals(tickerText)) {
                String[] cc = tickerText.split(":");

                if (cc.length >= 2) {
                    String noticeWxNickName = cc[0].trim();// 发送人
                    String content = cc[1].trim(); // 发送信息

                    // jia命令，特殊消息处理
                    if (content.startsWith(keyWord)) {
                        // 拉起微信界面
                        notifyWechat(event);
                        doSleep(1000);
                        return true;
                    }

                }
            }
        }

        return false;
    }


    /**
     * 拉起微信界面
     *
     * @param event event
     */
    private void notifyWechat(AccessibilityEvent event) {

        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {

            Notification notification = (Notification) event.getParcelableData();

            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 自动抢红包 =================================================================================分割线=========================================================================================================================
     */

    private void processLuckMoneyEvent(AccessibilityEvent event) {

        Log.d(TAG, "luckyMoneyStep:" + luckyMoneyStep);

        // 1.当前在聊天界面，查找并点击领取红包
        if (luckyMoneyStep == -1) {

            // 有人发信息 记录信息内容 会直接点开信息

            if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                if (handleNotification(event, "[微信红包]")) {

                    luckyMoneyStep = 1;
                }

            } else if (event.getClassName() != null && "com.tencent.mm.ui.LauncherUI".equals(event.getClassName().toString())) {

                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode == null) {
                    Log.e(TAG, "rootNode is null");
                    return;
                }

                List<AccessibilityNodeInfo> chatList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b4m");
                if (chatList != null && !chatList.isEmpty()) {
                    for (int i = 0; i < chatList.size(); i++) {
                        AccessibilityNodeInfo chatNode = chatList.get(i);

                        if (chatNode != null) {
                            List<AccessibilityNodeInfo> tipList = chatNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/mm");
                            if (tipList != null && tipList.size() > 0) {
                                chatNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                doSleep(200);
                                Log.i(TAG, "有新消息，点击消息提示");
                                luckyMoneyStep = 1;
                            } else {
                                Log.i(TAG, "没有消息提示");
                            }
                        } else {
                            Log.i(TAG, "chatNode is null");
                        }
                    }
                } else {
                    Log.i(TAG, "对话列表");
                }
            } else {
            }

        } else if (luckyMoneyStep == 1) {


            if ("com.tencent.mm.plugin.readerapp.ui.ReaderAppUI".equals(event.getClassName().toString())) {
                goBack();
                doSleep(200);
                luckyMoneyStep = -1;
            } else {
                String keyWord = "微信红包";
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode == null) {
                    Log.e(TAG, "rootNode is null");
                    return;
                }

                boolean foundLuckyMoney = false;
                List<AccessibilityNodeInfo> bu = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ao4");
                if (bu == null || bu.isEmpty()) {
                    Log.e(TAG, keyWord + " is null");
                } else {
                    for (int i = 0; i < bu.size(); i++) {
                        Log.i(TAG, "bu.size:" + bu.size() + ", i:" + i);

                        AccessibilityNodeInfo nodeInfo = bu.get(i);
                        // 已经领取了
                        List<AccessibilityNodeInfo> hasDoneList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ape");
                        if (hasDoneList != null && hasDoneList.size() > 0) {
                            Log.d(TAG, "<" + keyWord + "> 已经领过了");
                            continue;
                        }

                        hasDoneList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/apw");
                        if (hasDoneList != null && hasDoneList.size() > 0) {
                            Log.d(TAG, "<" + keyWord + "> 已收钱");
                            continue;
                        }

                        hasDoneList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/apf");
                        if (hasDoneList == null || hasDoneList.size() <= 0) {
                            Log.d(TAG, "<" + keyWord + "> 不是微信红包");
                            continue;
                        }

                        if (nodeInfo != null && nodeInfo.isClickable()) {
                            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                            Log.d(TAG, "点击 <" + keyWord + ">" + ", id:" + nodeInfo.getClassName().toString());

                            luckyMoneyStep = 2;
                            foundLuckyMoney = true;
                            break;
                        } else {
                            Log.d(TAG, "<" + keyWord + "> 不能点击");
                        }
                    }
                }

                // 没有找到可以抢的红包
                if (!foundLuckyMoney) {
                    if (scrollNum > 2) {
                        Log.i(TAG, "滚动多次，scrollNum:" + scrollNum);
                        scrollNum = 0;

                        goBack();
                        luckyMoneyStep = -1;

                    } else {
                        rootNode = getRootInActiveWindow();
                        List<AccessibilityNodeInfo> listViewNodeParent = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/akn");
                        if (listViewNodeParent != null && listViewNodeParent.size() > 0) {

                            AccessibilityNodeInfo listViewNode = listViewNodeParent.get(0).getChild(1);
                            if (listViewNode != null && listViewNode.isScrollable()) {

                                scrollNum++;
                                listViewNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                                doSleep(200);
                                Log.i(TAG, "没有更多红包了，滑动一页");
                            } else {
                                Log.e(TAG, "listViewNode is null");
                            }
                        } else {
                            Log.e(TAG, "listViewNodeParent is null");
                        }
                    }
                }
            }

        } else if (luckyMoneyStep == 2) {
            // 3.抢到红包后的详情页面

            if (findAndClickNodeByViewId("com.tencent.mm:id/cv0", 0, "开")) {

                Log.i(TAG, "抢到红包了");
                luckyMoneyStep = 3;
            } else if (findAndClickNodeByViewId("com.tencent.mm:id/dty", 0, "确认收款")) {

                Log.i(TAG, "确认收款");
                luckyMoneyStep = 3;
            } else if (findNodeByViewId("com.tencent.mm:id/cqz", 0, "已存入零钱，可直接转账") != null) {

                goBack();
                luckyMoneyStep = 1;
            } else if (findNodeByViewId("com.tencent.mm:id/dt1", 0, "已收钱") != null) {

                goBack();
                luckyMoneyStep = 1;
            }

        } else if (luckyMoneyStep == 3) {

            doSleep(2000);
            goBack();
            // 继续打开红包
            luckyMoneyStep = 1;
        }
    }

    /**
     * 工具方法 =================================================================================分割线=========================================================================================================================
     */

    /**
     * 查找节点
     */
    private AccessibilityNodeInfo findNodeByViewId(String viewId, int index, String keyWord) {

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {

            Log.e(TAG, "rootNode is null");
            return null;
        }

        List<AccessibilityNodeInfo> bu = rootNode.findAccessibilityNodeInfosByViewId(viewId);
        if (bu == null || bu.isEmpty()) {
            Log.e(TAG, "<" + keyWord + "> is null");
            return null;
        }

        Log.d(TAG, "获取 <" + keyWord + ">");

        return bu.get(index);
    }


    /**
     * 查找并点击要素
     *
     * @param viewId
     * @param index
     * @param keyWord
     * @return
     */
    private boolean findAndClickNodeByViewId(String viewId, int index, String keyWord) {

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();

        if (rootNode == null) {

            Log.e(TAG, "rootNode is null");
            return false;
        }

        List<AccessibilityNodeInfo> bu = rootNode.findAccessibilityNodeInfosByViewId(viewId);
        if (bu == null || bu.isEmpty()) {
            Log.e(TAG, keyWord + " is null");
            return false;
        }

        AccessibilityNodeInfo nodeInfo = bu.get(index);

        while (nodeInfo != null && !nodeInfo.isClickable()) {
            nodeInfo = nodeInfo.getParent();
        }

        if (nodeInfo != null) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);

            Log.d(TAG, "点击 <" + keyWord + ">");

            return true;
        }

        Log.d(TAG, "<" + keyWord + "> 不能点击");

        return false;
    }


    /***
     * 返回键
     *
     * @return
     */
    private boolean goBack() {

//        doSleep(200);

        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);

        return true;


    }

    // 延时
    private void doSleep(long millis) {

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 父类方法 =================================================================================分割线=========================================================================================================================
     */

    @Override
    public void onInterrupt() {

        Log.i(TAG, "服务已中断");
        Toast.makeText(this, "服务已中断", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onUnbind(Intent intent) {

        TasksWindow.dismiss();

        return super.onUnbind(intent);

    }

    @Override
    public void onDestroy() {

        Log.i(TAG, "onDestroy停止了，请重新开启");

        // 服务停止，重新进入系统设置界面
        AccessibilityUtil.jumpToSetting(this);
    }

}
