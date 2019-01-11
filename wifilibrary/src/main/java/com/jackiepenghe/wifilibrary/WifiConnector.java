package com.jackiepenghe.wifilibrary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.jackiepenghe.wifilibrary.enums.EncryptWay;
import com.jackiepenghe.wifilibrary.intefaces.OnWifiConnectStateChangedListener;
import com.jackiepenghe.wifilibrary.intefaces.impl.DefaultOnWifiConnectStateChangedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jackie
 */
public class WifiConnector {

    private static final String TAG = WifiConnector.class.getSimpleName();
    private static final int WIFI_PASSWORD_MIN_LENGTH = 8;
    private WifiConnectStatusBroadcastReceiver wifiConnectStatusBroadcastReceiver = new WifiConnectStatusBroadcastReceiver(this);
    private long connectTimeOut = 15000;
    private ArrayList<OnWifiConnectStateChangedListener> onWifiConnectStateChangedListeners = new ArrayList<>();
    private boolean isConnected;
    private String connectingSsid;

    WifiConnector() {
        WifiManager.getContext().registerReceiver(wifiConnectStatusBroadcastReceiver, makeIntentFilter());
    }

    /**
     * 发起连接
     *
     * @param wifiInfo WifiInfo
     */
    public void startConnect(final WifiInfo wifiInfo) {
        connectingSsid = wifiInfo.getSSID();
        int netId = isExists(connectingSsid);
        if (-1 != netId) {
            boolean enableNetwork = WifiManager.getSystemWifiManager().enableNetwork(netId, true);
            if (enableNetwork) {
                startThreadToCheckConnectTimeOut();
                performWifiConnectingListener(wifiInfo.getSSID());
            } else {
                performWifiConnectFailedListener(wifiInfo.getSSID());
            }
            return;
        }
        performWifiConnectFailedListener(wifiInfo.getSSID());
    }

    public void setConnectTimeOut(long connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    /**
     * 发起连接
     *
     * @param wifiDevice WiFi设备
     */
    public void startConnect(Context context,WifiDevice wifiDevice) {
        startConnect(context,wifiDevice, null);
    }

    /**
     * 发起连接
     *
     * @param wifiSSid WiFi名
     */
    public void startConnect(Context context,String wifiSSid) {
        startConnect(context, wifiSSid, null);
    }

    /**
     * 发起连接
     *
     * @param wifiSSid WiFi名
     * @param password WiFi密码
     */
    @SuppressWarnings("WeakerAccess")
    public void startConnect(Context context, String wifiSSid, String password) {
        startConnect(context,wifiSSid, password, 0);
    }


    /**
     * 发起连接
     *
     * @param wifiDevice 扫描设备
     */
    @SuppressWarnings("WeakerAccess")
    public void startConnect(Context context, WifiDevice wifiDevice, String password) {
        if (password == null) {
            connectWifiDeviceWithoutPassword(context,wifiDevice);
        } else {
            connectWifiDeviceWithPassword(context,wifiDevice, password);
        }
    }

    public void close() {
        try {
            WifiManager.getContext().unregisterReceiver(wifiConnectStatusBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断当前wifi是否有保存
     *
     * @param ssid 当前WiFi的SSID
     * @return 当前wifi已保存的配置
     */
    private int isExists(String ssid) {
        List<WifiConfiguration> existingConfigs = WifiManager.getSystemWifiManager().getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (WifiManager.isWifiSsidEquals(existingConfig.SSID, ssid)) {
                return existingConfig.networkId;
            }
        }
        return -1;
    }

    private void startConnect(Context context,String wifiSSid, String password, int tryCount) {
        tryCount++;
        List<ScanResult> scanResults = WifiManager.getSystemWifiManager().getScanResults();
        WifiDevice wifiDevice = null;
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult scanResult = scanResults.get(i);
            if (WifiManager.isWifiSsidEquals(scanResult.SSID, wifiSSid)) {
                wifiDevice = new WifiDevice(scanResult);
                break;
            }
        }
        if (wifiDevice == null) {
            if (tryCount > 3) {
                return;
            }
            startConnect(context, wifiSSid, password, tryCount);
            return;
        }
        startConnect(context, wifiDevice, password);
    }

    private void connectWifiDeviceWithoutPassword(Context context,WifiDevice wifiDevice) {
        String connectedWifiSSID = WifiManager.getConnectedWifiSSID();
        if (connectedWifiSSID != null && WifiManager.isWifiSsidEquals(connectedWifiSSID, wifiDevice.getSSID())) {
            isConnected = true;
            performWifiConnectedListener(wifiDevice.getSSID());
            return;
        }
        WifiManager.getSystemWifiManager().disconnect();
        connectingSsid = wifiDevice.getSSID();
        int netId = isExists(wifiDevice.getSSID());
        if (-1 != netId) {
            boolean enableNetwork = WifiManager.getSystemWifiManager().enableNetwork(netId, true);
            if (enableNetwork) {
                performWifiConnectingListener(wifiDevice.getSSID());
            } else {
                performWifiConnectFailedListener(wifiDevice.getSSID());
            }
            return;
        }

        EncryptWay encryptWay = wifiDevice.getEncryptWay();
        //不需要密码
        if (encryptWay != EncryptWay.NO_ENCRYPT) {
            //连接WiFi
            if (!wifiDevice.isHidden()) {
                showConnectWifiWithPassWord(context ,wifiDevice);
            }
            //连接隐藏的WiFi
            else {
                DebugUtil.warnOut(TAG, "连接隐藏WiFi");
                showConnectWifiWithNameAndPassWord(context, encryptWay);
            }
        } else {
            WifiConfiguration wifiConfiguration = createWifiConfiguration(wifiDevice.getSSID(), "", encryptWay, false);
            connect(wifiConfiguration);
        }
    }

    private void connectWifiDeviceWithPassword(Context context,WifiDevice wifiDevice, String password) {
        String connectedWifiSSID = WifiManager.getConnectedWifiSSID();
        if (connectedWifiSSID != null && WifiManager.isWifiSsidEquals(connectedWifiSSID, wifiDevice.getSSID())) {
            isConnected = true;
            performWifiConnectedListener(wifiDevice.getSSID());
            return;
        }
        connectingSsid = wifiDevice.getSSID();
        WifiManager.getSystemWifiManager().disconnect();
        int netId = isExists(wifiDevice.getSSID());
        if (-1 != netId) {
            boolean enableNetwork = WifiManager.getSystemWifiManager().enableNetwork(netId, true);
            if (enableNetwork) {
                performWifiConnectingListener(wifiDevice.getSSID());
            } else {
                performWifiConnectFailedListener(wifiDevice.getSSID());
            }
            return;
        }

        EncryptWay encryptWay = wifiDevice.getEncryptWay();
        //不需要密码
        if (encryptWay != EncryptWay.NO_ENCRYPT) {
            //连接WiFi
            if (!wifiDevice.isHidden()) {
                WifiConfiguration wifiConfiguration = createWifiConfiguration(wifiDevice.getSSID(), password, wifiDevice.getEncryptWay(), false);
                connect(wifiConfiguration);
            }
            //连接隐藏的WiFi
            else {
                DebugUtil.warnOut(TAG, "连接隐藏WiFi");
                showConnectWifiWithNameAndPassWord(context, encryptWay);
            }
        } else {
            WifiConfiguration wifiConfiguration = createWifiConfiguration(wifiDevice.getSSID(), password, encryptWay, false);
            connect(wifiConfiguration);
        }
    }

    /**
     * 连接指定的WiFi网络
     *
     * @param context    上下文
     * @param wifiDevice 扫描结果
     */
    private void showConnectWifiWithPassWord(final Context context, final WifiDevice wifiDevice) {
        final EditText editText = (EditText) View.inflate(context, R.layout.com_jackiepenghe_wifi_password_edit_text, null);
        new AlertDialog.Builder(context)
                .setTitle(R.string.input_wifi_password)
                .setView(editText)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = editText.getText().toString();
                        Log.w(TAG, "editText.getText():" + password);
                        if ("".equals(password) || password.length() < WIFI_PASSWORD_MIN_LENGTH) {
                            showConnectWifiWithPassWord(context, wifiDevice);
                            return;
                        }
                        WifiConfiguration wifiConfiguration = createWifiConfiguration(wifiDevice.getSSID(), password, wifiDevice.getEncryptWay(), false);
                        connect(wifiConfiguration);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performWifiConnectCanceledListener(wifiDevice.getSSID());
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 显示连接隐藏WiFi
     *
     * @param context       上下文
     * @param encryptionWay 加密方式
     */
    private void showConnectWifiWithNameAndPassWord(final Context context, final EncryptWay encryptionWay) {
        final View view = View.inflate(context, R.layout.com_jackiepenghe_hidden_wifi_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.input_wifi_name_and_password)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(view)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText wifiNameEt = view.findViewById(R.id.wifi_name);
                        EditText wifiPasswordEt = view.findViewById(R.id.wifi_password);

                        String wifiName = wifiNameEt.getText().toString();

                        if ("".equals(wifiName)) {
                            showConnectWifiWithNameAndPassWord(context, encryptionWay);
                            return;
                        }

                        String wifiPassword = wifiPasswordEt.getText().toString();
                        if ("".equals(wifiPassword) || wifiPassword.length() < WIFI_PASSWORD_MIN_LENGTH) {
                            showConnectWifiWithNameAndPassWord(context, encryptionWay);
                            return;
                        }
                        WifiConfiguration wifiConfiguration = createWifiConfiguration(wifiName, wifiPassword, encryptionWay, true);
                        connect(wifiConfiguration);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performWifiConnectCanceledListener(context.getString(R.string.hidden_network));
                    }
                })
                .setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 安卓8.0及以上的系统使用这个方法创建
     *
     * @param ssid         网络SSID
     * @param password     密码
     * @param encryptWay   加密方式
     * @param isHiddenSSID 是否为隐藏WiFi
     * @return WifiConfiguration
     */
    private WifiConfiguration createWifiConfiguration(String ssid, String password, EncryptWay encryptWay, boolean isHiddenSSID) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";
        if (encryptWay == EncryptWay.NO_ENCRYPT) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.hiddenSSID = isHiddenSSID;
        } else if (encryptWay == EncryptWay.WEP_ENCRYPT) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = isHiddenSSID;
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (encryptWay == EncryptWay.WPA_ENCRYPT || encryptWay == EncryptWay.WPA_WPA2_ENCRYPT) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = isHiddenSSID;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }

    /**
     * 开始连接
     *
     * @param config WiFi配置数据
     */
    private void connect(WifiConfiguration config) {
        int network = WifiManager.getSystemWifiManager().addNetwork(config);
        if (network == -1) {
            performWifiConnectFailedListener(config.SSID);
            return;
        }
        boolean enableNetwork = WifiManager.getSystemWifiManager().enableNetwork(network, true);
        if (enableNetwork) {
            performWifiConnectingListener(config.SSID);
        } else {
            performWifiConnectFailedListener(config.SSID);
        }
    }

    private void startThreadToCheckConnectTimeOut() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                while (!isConnected) {
                    if (System.currentTimeMillis() - currentTimeMillis >= connectTimeOut) {
                        WifiManager.getHANDLER().post(new Runnable() {
                            @Override
                            public void run() {
                                performWifiConnectTimeOutListener();
                            }
                        });
                        break;
                    }
                }
            }
        };
        WifiManager.getThreadFactory().newThread(runnable).start();
    }

    void onWifiConnected(String ssid) {
        if (connectingSsid == null) {
            return;
        }
        if (WifiManager.isWifiSsidEquals(ssid, connectingSsid)) {
            isConnected = true;
        }
    }

    private IntentFilter makeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);
        return intentFilter;
    }

    public boolean disConnect() {
        return WifiManager.getSystemWifiManager().disconnect();
    }

    public void forgetNetwork(String ssid) {
        android.net.wifi.WifiManager systemWifiManager = WifiManager.getSystemWifiManager();
        List<WifiConfiguration> configuredNetworks = systemWifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (int i = 0; i < configuredNetworks.size(); i++) {
                WifiConfiguration wifiConfiguration = configuredNetworks.get(i);
                if (WifiManager.isWifiSsidEquals(wifiConfiguration.SSID, ssid)) {
                    int networkId = wifiConfiguration.networkId;
                    systemWifiManager.removeNetwork(networkId);
                    break;
                }
            }
        }
    }

    public boolean addOnWifiConnectStateChangedListener(@NonNull OnWifiConnectStateChangedListener onWifiConnectStateChangedListener) {
        if(onWifiConnectStateChangedListeners.contains(onWifiConnectStateChangedListener)){
            return false;
        }
        return onWifiConnectStateChangedListeners.add(onWifiConnectStateChangedListener) && wifiConnectStatusBroadcastReceiver.addOnWifiConnectStateChangedListener(onWifiConnectStateChangedListener);
    }

    public boolean removeOnWifiConnectStateChangedListener(@NonNull OnWifiConnectStateChangedListener onWifiConnectStateChangedListener) {
        return onWifiConnectStateChangedListeners.remove(onWifiConnectStateChangedListener) && wifiConnectStatusBroadcastReceiver.removeOnWifiConnectStateChangedListener(onWifiConnectStateChangedListener);
    }

    @SuppressWarnings("WeakerAccess")
    public void removeAllOnWifiConnectStateChangedListener() {
        onWifiConnectStateChangedListeners.clear();
        wifiConnectStatusBroadcastReceiver.removeAllOnWifiConnectStateChangedListener();
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

    private void performWifiConnectCanceledListener(final String ssid) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < onWifiConnectStateChangedListeners.size(); i++) {
                    OnWifiConnectStateChangedListener onWifiConnectStateChangedListener = onWifiConnectStateChangedListeners.get(i);
                    if (onWifiConnectStateChangedListener != null) {
                        onWifiConnectStateChangedListener.cancelConnect(ssid);
                    }
                }
            }
        });
    }

    private void performWifiConnectTimeOutListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < onWifiConnectStateChangedListeners.size(); i++) {
                    OnWifiConnectStateChangedListener onWifiConnectStateChangedListener = onWifiConnectStateChangedListeners.get(i);
                    if (onWifiConnectStateChangedListener != null) {
                        onWifiConnectStateChangedListener.connectTimeOut();
                    }
                }
            }
        });
    }
}
