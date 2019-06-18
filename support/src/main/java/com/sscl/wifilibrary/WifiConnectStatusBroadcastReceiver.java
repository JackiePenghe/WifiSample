package com.sscl.wifilibrary;

import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.sscl.wifilibrary.intefaces.OnWifiConnectStateChangedListener;

import java.util.ArrayList;

/**
 * @author jackie
 */
public class WifiConnectStatusBroadcastReceiver extends BroadcastReceiver {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = WifiConnectStatusBroadcastReceiver.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * WifiConnector
     */
    private WifiConnector wifiConnector;

    /**
     * WiFi连接的回调
     */
    private ArrayList<OnWifiConnectStateChangedListener> onWifiConnectStateChangedListeners = new ArrayList<>();

    private android.net.wifi.WifiManager wifiManager;

    /*---------------------------构造方法---------------------------*/

    /**
     * 构造方法
     *
     * @param wifiConnector WifiConnector
     */
    public WifiConnectStatusBroadcastReceiver(WifiConnector wifiConnector) {
        this.wifiConnector = wifiConnector;
        wifiManager = WifiManager.getSystemWifiManager();
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
        //noinspection SwitchStatementWithTooFewBranches
        switch (action) {
            case android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION:
                NetworkInfo info = intent.getParcelableExtra(android.net.wifi.WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    performWifiDisconnectedListener();

                } else if (info.getState().equals(NetworkInfo.State.CONNECTING)) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo == null) {
                        DebugUtil.warnOut(TAG, "wifiInfo == null");
                        return;
                    }
                    performWifiConnectingListener(wifiInfo.getSSID());
                    NetworkInfo.DetailedState state = info.getDetailedState();
                    if (state == NetworkInfo.DetailedState.AUTHENTICATING) {
                        performWifiAuthenticatingListener(wifiInfo.getSSID());
                    } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                        performWifiObtainingIpAddressListener(wifiInfo.getSSID());
                    } else if (state == NetworkInfo.DetailedState.FAILED) {
                        performWifiConnectFailedListener(wifiInfo.getSSID());
                    }
                    wifiConnector.onWifiConnected(wifiInfo.getSSID());
                    //获取当前wifi名称
                    performWifiConnectedListener(wifiInfo.getSSID());
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    NetworkInfo.DetailedState state = info.getDetailedState();
                    final String ssid = wifiInfo.getSSID();
                    DebugUtil.warnOut(TAG, "ssid = " + ssid);
                    if (state == NetworkInfo.DetailedState.AUTHENTICATING) {
                        performWifiAuthenticatingListener(ssid);

                    } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                        performWifiObtainingIpAddressListener(ssid);
                    } else if (state == NetworkInfo.DetailedState.FAILED) {
                        performWifiConnectFailedListener(ssid);
                    }
                    wifiConnector.onWifiConnected(ssid);
                    //获取当前wifi名称
                    performWifiConnectedListener(ssid);
                } else if (info.getState().equals(NetworkInfo.State.DISCONNECTING)) {
                    performWifiDisconnectingListener();
                } else if (info.getState().equals(NetworkInfo.State.UNKNOWN)) {
                    performWifiUnknownStateListener();
                }
                break;
            default:
                DebugUtil.warnOut(TAG, "action = " + action);
                break;
        }
    }

    /*---------------------------getter & setter---------------------------*/

    /**
     * 添加WiFi连接的回调
     *
     * @param onWifiConnectStateChangedListener WiFi连接的回调
     */
    public boolean addOnWifiConnectStateChangedListener(@NonNull OnWifiConnectStateChangedListener onWifiConnectStateChangedListener) {
        if (onWifiConnectStateChangedListeners.contains(onWifiConnectStateChangedListener)) {
            return false;
        }
        return onWifiConnectStateChangedListeners.add(onWifiConnectStateChangedListener);
    }

    /**
     * 移除WiFi连接的回调
     *
     * @param onWifiConnectStateChangedListener WiFi连接的回调
     */
    public boolean removeOnWifiConnectStateChangedListener(@NonNull OnWifiConnectStateChangedListener onWifiConnectStateChangedListener) {
        return onWifiConnectStateChangedListeners.remove(onWifiConnectStateChangedListener);
    }

    public void removeAllOnWifiConnectStateChangedListener() {
        onWifiConnectStateChangedListeners.clear();
    }

    private void performWifiAuthenticatingListener(final String ssid) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < onWifiConnectStateChangedListeners.size(); i++) {
                    OnWifiConnectStateChangedListener onWifiConnectStateChangedListener = onWifiConnectStateChangedListeners.get(i);
                    if (onWifiConnectStateChangedListener != null) {
                        onWifiConnectStateChangedListener.authenticating(ssid);
                    }
                }
            }
        });
    }

    private void performWifiObtainingIpAddressListener(final String ssid) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < onWifiConnectStateChangedListeners.size(); i++) {
                    OnWifiConnectStateChangedListener onWifiConnectStateChangedListener = onWifiConnectStateChangedListeners.get(i);
                    if (onWifiConnectStateChangedListener != null) {
                        onWifiConnectStateChangedListener.obtainingIpAddress(ssid);
                    }
                }
            }
        });
    }

    private void performWifiConnectFailedListener(final String ssid) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < onWifiConnectStateChangedListeners.size(); i++) {
                    OnWifiConnectStateChangedListener onWifiConnectStateChangedListener = onWifiConnectStateChangedListeners.get(i);
                    if (onWifiConnectStateChangedListener != null) {
                        onWifiConnectStateChangedListener.connectFailed(ssid);
                    }
                }
            }
        });
    }

    private void performWifiConnectedListener(final String ssid) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < onWifiConnectStateChangedListeners.size(); i++) {
                    OnWifiConnectStateChangedListener onWifiConnectStateChangedListener = onWifiConnectStateChangedListeners.get(i);
                    if (onWifiConnectStateChangedListener != null) {
                        onWifiConnectStateChangedListener.connected(ssid);
                    }
                }
            }
        });
    }

    private void performWifiDisconnectingListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < onWifiConnectStateChangedListeners.size(); i++) {
                    OnWifiConnectStateChangedListener onWifiConnectStateChangedListener = onWifiConnectStateChangedListeners.get(i);
                    if (onWifiConnectStateChangedListener != null) {
                        onWifiConnectStateChangedListener.disconnecting();
                    }
                }
            }
        });
    }

    private void performWifiUnknownStateListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < onWifiConnectStateChangedListeners.size(); i++) {
                    OnWifiConnectStateChangedListener onWifiConnectStateChangedListener = onWifiConnectStateChangedListeners.get(i);
                    if (onWifiConnectStateChangedListener != null) {
                        onWifiConnectStateChangedListener.unknownStatus();
                    }
                }
            }
        });
    }

    private void performWifiDisconnectedListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < onWifiConnectStateChangedListeners.size(); i++) {
                    OnWifiConnectStateChangedListener onWifiConnectStateChangedListener = onWifiConnectStateChangedListeners.get(i);
                    if (onWifiConnectStateChangedListener != null) {
                        onWifiConnectStateChangedListener.disconnected();
                    }
                }
            }
        });
    }

    private void performWifiConnectingListener(final String ssid) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < onWifiConnectStateChangedListeners.size(); i++) {
                    OnWifiConnectStateChangedListener onWifiConnectStateChangedListener = onWifiConnectStateChangedListeners.get(i);
                    if (onWifiConnectStateChangedListener != null) {
                        onWifiConnectStateChangedListener.connecting(ssid);
                    }
                }
            }
        });
    }

}
