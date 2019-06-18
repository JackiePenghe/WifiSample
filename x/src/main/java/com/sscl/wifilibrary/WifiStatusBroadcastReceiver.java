package com.sscl.wifilibrary;

import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.sscl.wifilibrary.intefaces.OnWifiStateChangedListener;


/**
 * @author jackie
 */
public class WifiStatusBroadcastReceiver extends BroadcastReceiver {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = WifiStatusBroadcastReceiver.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * WiFi状态改变时进行的回调
     */
    private OnWifiStateChangedListener wifiStateChangedListener;

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
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                //获取当前的wifi状态int类型数据
                int mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (mWifiState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        //已打开
                        if (wifiStateChangedListener != null) {
                            wifiStateChangedListener.onWifiEnabled();
                        }
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        //打开中
                        if (wifiStateChangedListener != null) {
                            wifiStateChangedListener.onWifiEnabling();
                        }
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        if (wifiStateChangedListener != null) {
                            wifiStateChangedListener.onWifiDisabled();
                        }
                        //已关闭
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        //关闭中
                        if (wifiStateChangedListener != null) {
                            wifiStateChangedListener.onWifiDisabling();
                        }
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        //未知
                        if (wifiStateChangedListener != null) {
                            wifiStateChangedListener.unknownWifiState(mWifiState);
                        }
                        break;
                    default:
                        if (wifiStateChangedListener != null) {
                            wifiStateChangedListener.unknownWifiState(mWifiState);
                        }
                        break;
                }
                break;
            default:
                DebugUtil.warnOut(TAG, "action = " + action);
                break;
        }
    }

    /**
     * 设置WiFi状态改变时进行的回调
     *
     * @param wifiStateChangedListener WiFi状态改变时进行的回调
     */
    public void setWifiStateChangedListener(OnWifiStateChangedListener wifiStateChangedListener) {
        this.wifiStateChangedListener = wifiStateChangedListener;
    }
}
