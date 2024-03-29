package com.sscl.wifilibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sscl.wifilibrary.enums.EncryptWay;
import com.sscl.wifilibrary.intefaces.OnWifiStateChangedListener;
import com.sscl.wifilibrary.x.R;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * @author jackie
 */
@SuppressWarnings({"WeakerAccess", "deprecation"})
public class WifiManager {

    /*---------------------------静态常量---------------------------*/

    private static ArrayList<DataReceiver> dataReceivers = new ArrayList<>();

    private static ArrayList<DataTransmitter> dataTransmitters = new ArrayList<>();

    private static ArrayList<WifiHotspotController> wifiHotspotControllers = new ArrayList<>();

    private static ArrayList<WifiScanner> wifiScanners = new ArrayList<>();

    private static ArrayList<WifiConnector> wifiConnectors = new ArrayList<>();

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
     * Handler
     */
    private static final Handler HANDLER = new Handler();

    /*---------------------------静态成员变量---------------------------*/

    /**
     * 安卓系统的WiFi管理器
     */
    private static android.net.wifi.WifiManager systemWifiManager;
    /**
     * Wifi scanner
     */
    private static WifiScanner wifiScanner;
    /**
     * wifi connector
     */
    private static WifiConnector wifiConnector;
    /**
     * 记录是否已经初始化
     */
    private static boolean isInit;
    /**
     * WiFi热点创建器
     */
    private static WifiHotspotController wifiHotspotController;
    /**
     * P2P数据接收器
     */
    private static DataReceiver dataReceiver;
    /**
     * P2P数据传输器
     */
    private static DataTransmitter dataTransmitter;
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
     * @param context                    上下文
     * @param onWifiStateChangedListener WiFi状态被改变时进行的回调
     */
    @SuppressWarnings("WeakerAccess")
    public static void init(@NonNull Context context, OnWifiStateChangedListener onWifiStateChangedListener) {
        initWifiManager(context.getApplicationContext());
        WifiManager.context = context.getApplicationContext();
        wifiStatusBroadcastReceiver = new WifiStatusBroadcastReceiver();
        wifiStatusBroadcastReceiver.setWifiStateChangedListener(onWifiStateChangedListener);
        context.getApplicationContext().registerReceiver(wifiStatusBroadcastReceiver, makeIntentFilter());
        isInit = true;
    }

    /**
     * 获取Wifi热点创建器单例
     *
     * @return Wifi热点创建器
     */
    public static WifiHotspotController getWifiHotspotControllerInstance() {
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
    public static WifiHotspotController newWifiHotspotController() {
        checkInitStatus();
        WifiHotspotController wifiHotspotController = new WifiHotspotController();
        wifiHotspotControllers.add(wifiHotspotController);
        return wifiHotspotController;
    }

    public static WifiScanner getWifiScannerInstance() {
        checkInitStatus();
        if (wifiScanner == null) {
            synchronized (WifiManager.class) {
                if (wifiScanner == null) {
                    wifiScanner = new WifiScanner();
                }
            }
        }
        return wifiScanner;
    }

    public static WifiScanner newWifiScannerInstance() {
        checkInitStatus();
        WifiScanner wifiScanner = new WifiScanner();
        wifiScanners.add(wifiScanner);
        return wifiScanner;
    }

    public static WifiConnector getWifiConnectorInstance() {
        checkInitStatus();
        if (wifiConnector == null) {
            synchronized (WifiManager.class) {
                if (wifiConnector == null) {
                    wifiConnector = new WifiConnector();
                }
            }
        }
        return wifiConnector;
    }

    public static WifiConnector newWifiConnectorInstance() {
        checkInitStatus();
        WifiConnector wifiConnector = new WifiConnector();
        wifiConnectors.add(wifiConnector);
        return wifiConnector;
    }

    /**
     * 释放Wifi热点创建器
     */
    public static void releaseWifiHotspotController() {
        checkInitStatus();
        if (wifiHotspotController != null) {
            if (wifiHotspotController.isWifiApEnabled()) {
                wifiHotspotController.close();
            }
            wifiHotspotController.setOnDataReceivedListener(null);
            wifiHotspotController = null;
        }
    }

    public static void releaseP2pRecevier() {
        checkInitStatus();
        if (dataReceiver != null) {
            dataReceiver.close();
        }
    }

    public static void releaseP2pTransmitter() {
        checkInitStatus();
        if (dataTransmitter != null) {
            dataTransmitter.close();
        }
    }

    /**
     * 释放Wifi热点创建器
     */
    public static void releaseWifiConnector() {
        checkInitStatus();
        if (wifiConnector != null) {
            wifiConnector.removeAllOnWifiConnectStateChangedListener();
            wifiConnector.close();
            wifiConnector = null;
        }
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
     * 释放全部内存
     */
    public static void releaseAll() {
        checkInitStatus();
        releaseWifiHotspotController();
        for (int i = 0; i < wifiHotspotControllers.size(); i++) {
            WifiHotspotController wifiHotspotController = wifiHotspotControllers.get(i);
            wifiHotspotController.close();
        }
        wifiHotspotControllers.clear();
        releaseWifiConnector();
        for (int i = 0; i < wifiConnectors.size(); i++) {
            WifiConnector wifiConnector = wifiConnectors.get(i);
            wifiConnector.close();
        }
        wifiConnectors.clear();
        releaseP2pRecevier();
        for (int i = 0; i < dataReceivers.size(); i++) {
            DataReceiver dataReceiver = dataReceivers.get(i);
            dataReceiver.close();
        }
        dataReceivers.clear();
        releaseP2pTransmitter();
        for (int i = 0; i < dataTransmitters.size(); i++) {
            DataTransmitter dataTransmitter = dataTransmitters.get(i);
            dataTransmitter.close();
        }
        dataTransmitters.clear();
        releaseWifiScanner();
        for (int i = 0; i < wifiScanners.size(); i++) {
            WifiScanner wifiScanner = wifiScanners.get(i);
            wifiScanner.close();
        }
        wifiScanners.clear();
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
        DebugUtil.setDebugFlag(debugFlag);
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
    public static void setWifiStateChangedListener(OnWifiStateChangedListener wifiStateChangedListener) {
        checkInitStatus();
        WifiManager.wifiStatusBroadcastReceiver.setWifiStateChangedListener(wifiStateChangedListener);
    }

    public static DataReceiver getDataReceiverInstance() {
        checkInitStatus();
        if (dataReceiver == null) {
            synchronized (WifiManager.class) {
                if (dataReceiver == null) {
                    dataReceiver = new DataReceiver();
                }
            }
        }
        return dataReceiver;
    }

    public static DataReceiver newDataReceiverInstance() {
        checkInitStatus();
        DataReceiver dataReceiver = new DataReceiver();
        dataReceivers.add(dataReceiver);
        return dataReceiver;
    }

    public static DataTransmitter getDataTransmitterInstance() {
        checkInitStatus();
        if (dataTransmitter == null) {
            synchronized (WifiManager.class) {
                if (dataTransmitter == null) {
                    dataTransmitter = new DataTransmitter();
                }
            }
        }
        return dataTransmitter;
    }

    public static DataTransmitter newDataTransmitterInstance() {
        checkInitStatus();
        DataTransmitter dataTransmitter = new DataTransmitter();
        dataTransmitters.add(dataTransmitter);
        return dataTransmitter;
    }

    /**
     * 获取加密方式
     *
     * @param scanResult 扫描结果
     * @return 加密方式的枚举
     */
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
            return EncryptWay.UNKNOWN_ENCRYPT;
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
        return systemWifiManager.getConnectionInfo();
    }

    /**
     * 获取最真实的SSID
     *
     * @param ssid SSID
     * @return 真实的SSID
     */
    @Nullable
    public static String getRealSsid(@Nullable String ssid) {
        if (ssid == null) {
            return null;
        }
        String doubleQuotes = "\"";
        if (ssid.startsWith(doubleQuotes) && ssid.endsWith(doubleQuotes)) {
            ssid = ssid.substring(1);
            ssid = ssid.substring(0, ssid.length() - 1);
        }
        return ssid;
    }

    /**
     * 获取已连接的WiFi的SSID名称
     *
     * @return 已连接的WiFi的SSID名称
     */
    @SuppressWarnings("unused")
    public static String getConnectedWifiSsid() {
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

    public static Handler getHANDLER() {
        return HANDLER;
    }

    public static ThreadFactory getThreadFactory() {
        return THREAD_FACTORY;
    }

    public static void releaseWifiScanner() {
        if (wifiScanner != null) {
            wifiScanner = null;
        }
    }


    /*---------------------------接口定义---------------------------*/

    /**
     * 检查是否已经初始化
     */
    private static void checkInitStatus() {
        if (!isInit) {
            throw new IllegalStateException("Please invoke method init(...) in your Applications class");
        }
    }

    /*---------------------------私有方法---------------------------*/

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

    /**
     * 判断当前程序是否有定位权限
     *
     * @return true表示拥有权限
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean hasLocationEnablePermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            int locationMode = Settings.Secure.LOCATION_MODE_OFF;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Exception ignored) {
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) {
                return false;
            }
            return locationManager.isLocationEnabled();
        }
    }

    public static void toLocationGpsSettings(@NonNull Context context) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void warnOut(String tag, String message) {
        DebugUtil.warnOut(tag, message);
    }

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
}
