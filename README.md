
# 微信自动抢红包插件

## AccessibilityService

### 1、基于AccessbilityService实现微信自动抢红包插件

[下载链接](http://mihuakabao.oss-cn-beijing.aliyuncs.com/apk/luckymoney.apk)

效果：

<video id="video" controls="" preload="none" poster="http://om2bks7xs.bkt.clouddn.com/2017-08-26-Markdown-Advance-Video.jpg">
      <source id="mp4" src="https://youtu.be/7GGEKzngmJA" type="video/mp4">
</video>
    
#### 使用时注意事项

- 开启微信消息通知栏功能
- 在系统电池相关选项中，设置不锁屏和休眠
- 插件运行过程中，手机显示为系统页面，不要在微信聊天页面


### 2、代码实现讲解

安卓系统上的辅助功能，使用该功能，需要授权App获取该权限。

#### 在AndroidManifest.xml文件中配置服务 WxRobotService

    <service
            android:name="com.onesatoshi.mm.WxRobotService"
            android:enabled="true"
            android:exported="true"
            android:label="@string/app_name"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter android:priority="10000"> 
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>

            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/auto_service_config" />
    </service>

#### 配置自定义Service的属性

    <?xml version="1.0" encoding="utf-8"?>
    <accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
        android:accessibilityEventTypes="typeAllMask"
        android:accessibilityFeedbackType="feedbackGeneric"
        android:accessibilityFlags="flagDefault|flagRetrieveInteractiveWindows|flagIncludeNotImportantViews"
        android:canRetrieveWindowContent="true"
        android:description="@string/app_name" />
    
其中，packageNames空着表示监听所有的应用
    accessibilityFlags这个参数不能设置为`flagDefault`，而需要设置为`flagDefault|flagRetrieveInteractiveWindows|flagIncludeNotImportantViews`

#### 重写方法

    public class WxRobotService extends AccessibilityService {

        @Override
        public void onAccessibilityEvent(final AccessibilityEvent event) {
        
        }
        
        @Override
        protected void onServiceConnected() {
        
        }
    }

#### 实现业务逻辑

业务代码在onAccessibilityEvent实现，直接贴代码

    
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

完整的代码，[戳这里](https://github.com/yankaili2006/luckymoney)


