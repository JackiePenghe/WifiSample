package com.jackiepenghe.wifilibrary;

import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.os.Handler;

/**
 * @author jackie
 */
public class WifiConnectStatusBroadcastReceiver extends BroadcastReceiver {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = WifiConnectStatusBroadcastReceiver.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * 系统的WiFi管理器
     */
    private android.net.wifi.WifiManager systemWifiManager;

    /**
     * WiFi连接的回调
     */
    private com.jackiepenghe.wifilibrary.WifiOperatingTools.WifiConnectCallback wifiConnectCallback;

    /*---------------------------构造方法---------------------------*/

    /**
     * 构造方法
     *
     * @param systemWifiManager 系统的WiFi管理器
     */
    public WifiConnectStatusBroadcastReceiver(android.net.wifi.WifiManager systemWifiManager) {
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
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        switch (action) {
            case android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION:
                NetworkInfo info = intent.getParcelableExtra(android.net.wifi.WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    if (wifiConnectCallback != null) {
                        wifiConnectCallback.disconnected();
                    }

                } else if (info.getState().equals(NetworkInfo.State.CONNECTING)) {
                    WifiInfo wifiInfo = systemWifiManager.getConnectionInfo();
                    if (wifiInfo == null) {
                        Tool.warnOut(TAG, "wifiInfo == null");
                        return;
                    }
                    if (wifiConnectCallback != null) {
                        wifiConnectCallback.connecting(wifiInfo.getSSID());
                    }
                    NetworkInfo.DetailedState state = info.getDetailedState();
                    if (state == NetworkInfo.DetailedState.AUTHENTICATING) {
                        if (wifiConnectCallback != null) {
                            wifiConnectCallback.authenticating(wifiInfo.getSSID());
                        }
                    } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                        if (wifiConnectCallback != null) {
                            wifiConnectCallback.obtainingIpAddress(wifiInfo.getSSID());
                        }
                    } else if (state == NetworkInfo.DetailedState.FAILED) {
                        if (wifiConnectCallback != null) {
                            wifiConnectCallback.connectFailed(wifiInfo.getSSID());
                        }
                    }
                    //获取当前wifi名称
                    if (wifiConnectCallback != null) {
                        wifiConnectCallback.connected(wifiInfo.getSSID());
                    }
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiInfo wifiInfo = systemWifiManager.getConnectionInfo();
                    NetworkInfo.DetailedState state = info.getDetailedState();
                    if (state == NetworkInfo.DetailedState.AUTHENTICATING) {
                        if (wifiConnectCallback != null) {
                            wifiConnectCallback.authenticating(wifiInfo.getSSID());
                        }
                    } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                        if (wifiConnectCallback != null) {
                            wifiConnectCallback.obtainingIpAddress(wifiInfo.getSSID());
                        }
                    } else if (state == NetworkInfo.DetailedState.FAILED) {
                        if (wifiConnectCallback != null) {
                            wifiConnectCallback.connectFailed(wifiInfo.getSSID());
                        }
                    }
                    //获取当前wifi名称
                    if (wifiConnectCallback != null) {
                        wifiConnectCallback.connected(wifiInfo.getSSID());
                    }
                } else if (info.getState().equals(NetworkInfo.State.DISCONNECTING)) {
                    wifiConnectCallback.disconnecting();
                } else if (info.getState().equals(NetworkInfo.State.UNKNOWN)) {
                    wifiConnectCallback.unknownStatus();
                }
                break;
            default:
                Tool.warnOut(TAG, "action = " + action);
                break;
        }
    }

    /*---------------------------getter & setter---------------------------*/

    /**
     * 设置WiFi连接的回调
     *
     * @param wifiConnectCallback WiFi连接的回调
     */
    public void setWifiConnectCallback(WifiOperatingTools.WifiConnectCallback wifiConnectCallback) {
        this.wifiConnectCallback = wifiConnectCallback;
    }
}
