package com.jackiepenghe.wifilibrary;

import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jackie
 */
public class WifiScanDataAndStatusBroadcastReceiver extends BroadcastReceiver {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = WifiScanDataAndStatusBroadcastReceiver.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * 系统的WiFi管理器
     */
    private android.net.wifi.WifiManager systemWifiManager;

    /**
     * 获取到WiFi扫描的结果时的回调
     */
    private WifiOperatingTools.WifiScanResultObtainedListener wifiScanResultObtainedListener;

    public WifiScanDataAndStatusBroadcastReceiver(WifiManager systemWifiManager) {
        this.systemWifiManager = systemWifiManager;
    }

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.  During this time you can use the other methods on
     * BroadcastReceiver to view/modify the current result values.  This method
     * is always called within the main thread of its process, unless you
     * explicitly asked for it to be scheduled on a different thread using
     * {@link Context#registerReceiver(BroadcastReceiver, * IntentFilter , String, Handler)}. When it runs on the main
     * thread you should
     * never perform long-running operations in it (there is a timeout of
     * 10 seconds that the system allows before considering the receiver to
     * be blocked and a candidate to be killed). You cannot launch a popup dialog
     * in your implementation of onReceive().
     * <p>
     * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
     * then the object is no longer alive after returning from this
     * function.</b> This means you should not perform any operations that
     * return a result to you asynchronously. If you need to perform any follow up
     * background work, schedule a {@link JobService} with
     * {@link JobScheduler}.
     * <p>
     * If you wish to interact with a service that is already running and previously
     * bound using {@link Context#bindService(Intent, ServiceConnection, int) bindService()},
     * you can use {@link #peekService}.
     * <p>
     * <p>The Intent filters used in {@link Context#registerReceiver}
     * and in application manifests are <em>not</em> guaranteed to be exclusive. They
     * are hints to the operating system about how to find suitable recipients. It is
     * possible for senders to force delivery to specific recipients, bypassing filter
     * resolution.  For this reason, {@link #onReceive(Context, Intent) onReceive()}
     * implementations should respond only to known actions, ignoring any unexpected
     * Intents that they may receive.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @SuppressWarnings({"JavadocReference", "JavaDoc"})
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null){
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        switch (action) {
            // wifi已成功扫描到可用wifi。
            case android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:

                List<ScanResult> scanResults = systemWifiManager.getScanResults();
                int size = scanResults.size();

                // 每次最大元素就像气泡一样"浮"到数组的最后
                for (int j = 0; j < size - 1; j++) {
                    // 依次比较相邻的两个元素,使较小的那个向后移
                    for (int i = 0; i < size - 1 - j; i++) {
                        ScanResult curScanResult = scanResults.get(i);
                        ScanResult nextScanResult = scanResults.get(i + 1);
                        if (curScanResult.level < nextScanResult.level) {
                            scanResults.set(i, nextScanResult);
                            scanResults.set(i + 1, curScanResult);
                        }
                    }
                }

                for (int i = 0; i < size; i++) {
                    ScanResult scanResult = scanResults.get(i);
                    String ssid = scanResult.SSID;
                    if (ssid == null || "".equals(ssid)) {
                        scanResults.remove(i);
                        scanResults.add(scanResult);
                    }
                    int level = scanResult.level;
                    Tool.warnOut(TAG, "设备 " + (i + 1) + " :ssid = " + ssid + ",level = " + level);
                }

                ArrayList<WifiDevice> wifiDevices = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    WifiDevice wifiDevice;
                    ScanResult scanResult = scanResults.get(i);
                    String ssid = scanResult.SSID;
                    if (ssid == null || "".equals(ssid)) {
                        scanResult.SSID = context.getString(R.string.hidden_network);
                        wifiDevice = new WifiDevice(context, scanResult, true);
                    }else {
                        wifiDevice = new WifiDevice(context, scanResult);

                    }
                    wifiDevices.add(wifiDevice);
                }

                if (wifiScanResultObtainedListener != null) {
                    wifiScanResultObtainedListener.wifiScanResultObtained(wifiDevices);
                }
                break;
            default:
                Tool.warnOut(TAG, "action = " + action);
                break;
        }
    }

    /*---------------------------getter & setter---------------------------*/

    /**
     * 设置获取到WiFi扫描的结果时的回调
     *
     * @param wifiScanResultObtainedListener 获取到WiFi扫描的结果时的回调
     */
    public void setWifiScanResultObtainedListener(WifiOperatingTools.WifiScanResultObtainedListener wifiScanResultObtainedListener) {
        this.wifiScanResultObtainedListener = wifiScanResultObtainedListener;
    }
}
