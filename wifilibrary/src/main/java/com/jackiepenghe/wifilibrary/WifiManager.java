package com.jackiepenghe.wifilibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.jackiepenghe.wifilibrary.WifiDevice.EncryptWay;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * @author jackie
 */
@SuppressWarnings("WeakerAccess")
public class WifiManager {

    /*---------------------------静态常量---------------------------*/

    static final int REQUEST_CODE_WRITE_SETTINGS = 10;

    /**
     * 线程工厂
     */
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r);
        }
    };


    /**
     * 定时任务执行服务
     */
    private static final ScheduledExecutorService SCHEDULED_THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(1, THREAD_FACTORY);

    /*---------------------------静态成员变量---------------------------*/

    /**
     * 安卓系统的WiFi管理器
     */
    private static android.net.wifi.WifiManager systemWifiManager;
    /**
     * 记录是否已经初始化
     */
    private static boolean isInit;
    /**
     * WiFi热点创建器
     */
    private static WifiHotspotController wifiHotspotController;
    /**
     * WiFi扫描器
     */
    @SuppressLint("StaticFieldLeak")
    private static WifiOperatingTools wifiOperatingTools;
    /**
     * 上下文
     */
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    /**
     * 监听广播接收者
     */
    private static WifiStatusBroadcastReceiver wifiStatusBroadcastReceiver;

    /*---------------------------公开函数---------------------------*/

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public static void init(@NonNull Context context) {
        init(context, null);
    }

    /**
     * 初始化
     *
     * @param context                  上下文
     * @param wifiStateChangedListener WiFi状态被改变时进行的回调
     */
    @SuppressWarnings("WeakerAccess")
    public static void init(@NonNull Context context, WifiStateChangedListener wifiStateChangedListener) {
        initWifiManager(context.getApplicationContext());
        WifiManager.context = context.getApplicationContext();
        wifiStatusBroadcastReceiver = new WifiStatusBroadcastReceiver();
        wifiStatusBroadcastReceiver.setWifiStateChangedListener(wifiStateChangedListener);
        context.getApplicationContext().registerReceiver(wifiStatusBroadcastReceiver, makeIntentFilter());
        isInit = true;
    }

    /**
     * 获取Wifi热点创建器单例
     *
     * @return Wifi热点创建器
     */
    public static WifiHotspotController getWifiHotspotCreatorInstance() {
        checkInitStatus();
        if (wifiHotspotController == null) {
            synchronized (WifiManager.class) {
                if (wifiHotspotController == null) {
                    wifiHotspotController = new WifiHotspotController();
                }
            }
        }
        return wifiHotspotController;
    }

    /**
     * 创建一个新的Wifi热点创建器
     *
     * @return Wifi热点创建器
     */
    @Deprecated
    public static WifiHotspotController newWifiHotspotCreator() {
        checkInitStatus();
        return new WifiHotspotController();
    }

    /**
     * 释放Wifi热点创建器
     */
    public static void releaseWifiHotspotCreator() {
        checkInitStatus();
        if (wifiHotspotController != null) {
            if (wifiHotspotController.isWifiApEnabled()) {
                wifiHotspotController.close();
            }
            wifiHotspotController.setOnDataReceivedListener(null);
            wifiHotspotController = null;
        }
    }

    /**
     * 获取WiFi扫描器单例
     */
    public static WifiOperatingTools getWifiOperatingToolsInstance() {
        checkInitStatus();
        if (wifiOperatingTools == null) {
            synchronized (WifiManager.class) {
                if (wifiOperatingTools == null) {
                    wifiOperatingTools = new WifiOperatingTools();
                }
            }
        }
        return wifiOperatingTools;
    }

    /**
     * 安卓6.0以上的手机在创建WiFi热点时，需要修改系统设置的权限，可以用这个函数进行判断
     *
     * @return true表示有权限
     */
    public static boolean hasWifiHotspotPermission() {
        checkInitStatus();
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(context);
    }

    /**
     * 获取WiFi扫描器单例
     */
    @Deprecated
    public static WifiOperatingTools newWifiOperatingToolsInstance() {
        checkInitStatus();
        return new WifiOperatingTools();
    }

    /**
     * 释放WiFi扫描器的资源
     */
    public static void releaseWifiOperatingTools() {
        checkInitStatus();
        if (wifiOperatingTools != null) {
            wifiOperatingTools.close();
            wifiOperatingTools = null;
        }
    }

    /**
     * 释放全部内存
     */
    public static void releaseAll() {
        checkInitStatus();
        releaseWifiHotspotCreator();
        releaseWifiOperatingTools();
        try {
            context.unregisterReceiver(wifiStatusBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前WiFi开启状态
     */
    public static boolean isWifiEnabled() {
        checkInitStatus();
        return systemWifiManager != null && systemWifiManager.isWifiEnabled();
    }

    /**
     * 打开或关闭WiFi
     *
     * @param enabled true表示打开，false表示关闭
     * @return 请求是否成功
     */
    public static boolean enableWifi(boolean enabled) {
        checkInitStatus();
        return systemWifiManager != null && systemWifiManager.setWifiEnabled(enabled);
    }

    /**
     * 设置是否打印调试信息
     *
     * @param debugFlag true表示需要打印
     */
    public static void setDebugFlag(boolean debugFlag) {
        checkInitStatus();
        Tool.setDebugFlag(debugFlag);
    }

    /**
     * 获取系统的WiFi管理器
     *
     * @return 系统的WiFi管理器
     */
    public static android.net.wifi.WifiManager getSystemWifiManager() {
        checkInitStatus();
        return systemWifiManager;
    }

    /**
     * 请求更改系统设置的权限
     */
    public static void requestSystemSettingsPermission(Context context, RequestPermissionResult requestPermissionResult) {
        checkInitStatus();
        PermissionActivity.requestPermission(context, requestPermissionResult);
    }

    /**
     * 重新请求更系统设置的权限
     *
     * @param activity                Activity
     * @param requestPermissionResult 权限请求的回调接口
     */
    public static void requestSystemSettingsPermissionRational(Activity activity, RequestPermissionResult requestPermissionResult) {
        checkInitStatus();
        showNoWriteSettingsPermissionDialog(activity, requestPermissionResult);
    }

    /**
     * 将int型的IP转为IP_V4格式的字符串
     *
     * @param i int型的IP
     * @return IP_V4格式的字符串
     */
    @SuppressWarnings("unused")
    public static String intIpToIpV4String(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 获取上下文
     *
     * @return 上下文
     */
    public static Context getContext() {
        return context;
    }

    /**
     * 设置WiFi
     *
     * @param wifiStateChangedListener WiFi状态改变时调用此接口
     */
    @SuppressWarnings("unused")
    public static void setWifiStateChangedListener(com.jackiepenghe.wifilibrary.WifiManager.WifiStateChangedListener wifiStateChangedListener) {
        checkInitStatus();
        WifiManager.wifiStatusBroadcastReceiver.setWifiStateChangedListener(wifiStateChangedListener);
    }

    /**
     * 获取加密方式
     *
     * @param scanResult 扫描结果
     * @return 加密方式的枚举
     */
    @SuppressWarnings("WeakerAccess")
    public static EncryptWay getEncryptionWay(ScanResult scanResult) {
        String wpaUpper = "WPA";
        String wpa = "wpa";
        String wpa2Upper = "WPA2";
        String wpa2 = "wpa2";
        String wepUpper = "WEP";
        String wep = "wep";

        String capabilities = scanResult.capabilities;
        boolean supportWPA = false;
        boolean supportWPA2 = false;
        boolean supportWEP = false;
        capabilities = capabilities.replace("[", " ");
        capabilities = capabilities.replace("]", " ");
        capabilities = capabilities.replace("  ", "\n");

        if (capabilities.contains(wepUpper) || capabilities.contains(wep)) {
            supportWEP = true;
        }

        if (capabilities.contains(wpa2Upper) || capabilities.contains(wpa2)) {
            supportWPA2 = true;
        }

        if (capabilities.contains(wpaUpper) || capabilities.contains(wpa)) {
            supportWPA = true;
        }

        StringBuilder passType = new StringBuilder();
        if (supportWEP) {
            passType.append("WEP");
        }

        if (supportWPA) {
            if ("".equals(passType.toString())) {
                passType.append("WPA");
            } else {
                passType.append("/WPA");
            }
        }

        if (supportWPA2) {
            if ("".equals(passType.toString())) {
                passType.append("WPA2");
            } else {
                passType.append("/WPA2");
            }
        }

        //未加密
        if ("".equals(passType.toString())) {
            return EncryptWay.NO_ENCRYPT;
        }
        //WEP加密
        else if (passType.toString().contains(wepUpper)) {
            return EncryptWay.WEP_ENCRYPT;
        }
        //WPA/WPA2加密
        else if (passType.toString().contains(wpaUpper) && passType.toString().contains(wpa2Upper)) {
            return EncryptWay.WPA_WPA2_ENCRYPT;
        }
        //仅WPA加密
        else if (passType.toString().contains(wpaUpper)) {
            return EncryptWay.WPA_ENCRYPT;
        } else {
            return EncryptWay.UNKNOWN__ENCRYPT;
        }

    }

    /**
     * 获取WiFi的加密方式
     *
     * @return WiFi的加密方式
     */
    public static String getEncryptionWayString(ScanResult scanResult) {
        String wpaUpper = "WPA";
        String wpa = "wpa";
        String wpa2Upper = "WPA2";
        String wpa2 = "wpa2";
        String wepUpper = "WEP";
        String wep = "wep";

        String capabilities = scanResult.capabilities;
        boolean supportWPA = false;
        boolean supportWPA2 = false;
        boolean supportWEP = false;
        capabilities = capabilities.replace("[", " ");
        capabilities = capabilities.replace("]", " ");
        capabilities = capabilities.replace("  ", "\n");

        if (capabilities.contains(wepUpper) || capabilities.contains(wep)) {
            supportWEP = true;
        }

        if (capabilities.contains(wpa2Upper) || capabilities.contains(wpa2)) {
            supportWPA2 = true;
        }

        if (capabilities.contains(wpaUpper) || capabilities.contains(wpa)) {
            supportWPA = true;
        }

        StringBuilder passType = new StringBuilder();
        if (supportWEP) {
            passType.append("WEP");
        }

        if (supportWPA) {
            if ("".equals(passType.toString())) {
                passType.append("WPA");
            } else {
                passType.append("/WPA");
            }
        }

        if (supportWPA2) {
            if ("".equals(passType.toString())) {
                passType.append("WPA2");
            } else {
                passType.append("/WPA2");
            }
        }

        if ("".equals(passType.toString())) {
            passType.append(context.getString(R.string.opened));
        } else {
            passType.append(" ")
                    .append(context.getString(R.string.encryption));
        }

        return passType.toString();
    }

    /**
     * 获取当前已连接的WiFi的信息
     *
     * @return 当前已连接的WiFi的信息
     */
    @SuppressWarnings("WeakerAccess")
    public static WifiInfo getConnectedWifiInfo() {
        checkInitStatus();
        if (!isWifiEnabled()) {
            return null;
        }
        WifiInfo connectionInfo = systemWifiManager.getConnectionInfo();
        if (connectionInfo == null) {
            return null;
        }
        return connectionInfo;
    }

    /**
     * 获取已连接的WiFi的SSID名称
     *
     * @return 已连接的WiFi的SSID名称
     */
    @SuppressWarnings("unused")
    public static String getConnectedWifiSSID() {
        checkInitStatus();
        if (!isWifiEnabled()) {
            return null;
        }
        WifiInfo connectedWifiInfo = getConnectedWifiInfo();
        if (connectedWifiInfo == null) {
            return null;
        }
        return connectedWifiInfo.getSSID();
    }

    /**
     * 判断两个SSID是否相同
     *
     * @param ssid1 SSID1
     * @param ssid2 SSID2
     * @return true表示相同
     */
    public static boolean isWifiSsidEquals(String ssid1, String ssid2) {
        String doubleQuotes = "\"";
        return ssid1.equals(doubleQuotes + ssid2 + doubleQuotes) || ssid1.equals(ssid2) || ssid2.equals(doubleQuotes + ssid1 + doubleQuotes);
    }

    public static ScheduledExecutorService newScheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(1, newThreadFactory());
    }

    public static ThreadFactory newThreadFactory() {
        return new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                return new Thread(r);
            }
        };
    }


    /*---------------------------接口定义---------------------------*/

    /**
     * 用于回调权限请求结果
     */
    public interface RequestPermissionResult {
        /**
         * 请求成功
         */
        void requestSuccess();

        /**
         * 请求失败
         */
        void requestFailed();

        /**
         * 取消二次权限请求
         */
        void requestRationalCanceled();
    }

    /**
     * WiFi状态改变时调用此接口
     */
    public interface WifiStateChangedListener {

        /**
         * WiFi已打开
         */
        void onWifiEnabled();

        /**
         * WiFi正在打开
         */
        void onWifiEnabling();

        /**
         * WiFi已关闭
         */
        void onWifiDisabled();

        /**
         * WiFi正在关闭
         */
        void onWifiDisabling();

        /**
         * 未知的WiFi状态
         *
         * @param wifiState WiFi状态
         */
        void unknownWifiState(int wifiState);

    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 显示没有“更改系统设置”权限的对话框
     *
     * @param activity                Activity
     * @param requestPermissionResult 显示没有“更改系统设置”权限的对话框
     */
    private static void showNoWriteSettingsPermissionDialog(final Activity activity, final RequestPermissionResult requestPermissionResult) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.no_write_setting_permission)
                .setMessage(R.string.no_write_setting_permission_message)
                .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionActivity.requestPermission(activity, requestPermissionResult);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (requestPermissionResult != null) {
                            requestPermissionResult.requestRationalCanceled();
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 检查是否已经初始化
     */
    private static void checkInitStatus() {
        if (!isInit) {
            throw new IllegalStateException("Please invoke method init(...) in your Applications class");
        }
    }

    /**
     * 创建广播接收者的过滤器
     *
     * @return IntentFilter
     */
    private static IntentFilter makeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION);
        return intentFilter;
    }

    /**
     * 获取安卓系统的WiFi管理器
     */
    private static void initWifiManager(Context context) {
        if (systemWifiManager == null) {
            synchronized (WifiManager.class) {
                if (systemWifiManager == null) {
                    systemWifiManager = (android.net.wifi.WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                }
            }
        }
    }
}
