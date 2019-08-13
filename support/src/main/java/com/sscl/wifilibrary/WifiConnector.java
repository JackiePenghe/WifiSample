package com.sscl.wifilibrary;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.sscl.wifilibrary.enums.EncryptWay;
import com.sscl.wifilibrary.intefaces.OnWifiConnectStateChangedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jackie
 */
public class WifiConnector {

    private static final String TAG = WifiConnector.class.getSimpleName();
    private WifiConnectStatusBroadcastReceiver wifiConnectStatusBroadcastReceiver = new WifiConnectStatusBroadcastReceiver(this);
    private ArrayList<OnWifiConnectStateChangedListener> onWifiConnectStateChangedListeners = new ArrayList<>();
    private final android.net.wifi.WifiManager systemWifiManager;

    WifiConnector() {
        WifiManager.getContext().registerReceiver(wifiConnectStatusBroadcastReceiver, makeIntentFilter());
        systemWifiManager = WifiManager.getSystemWifiManager();
    }

    public void connectNewWifi(@NonNull String ssid, @Nullable String password, boolean isHidden, EncryptWay encryptWay, boolean attemptConnect) {
        int existsNetworkId = isExists(ssid);
        if (existsNetworkId != -1) {
            boolean enableNetwork = systemWifiManager.enableNetwork(existsNetworkId, attemptConnect);
            if (enableNetwork) {
                performWifiConnectingListener(ssid);
            } else {
                performWifiConnectFailedListener(ssid);
            }
        } else {
            String connectedWifiSsid = WifiManager.getConnectedWifiSsid();
            if (connectedWifiSsid != null && WifiManager.isWifiSsidEquals(connectedWifiSsid, ssid)) {
                performWifiConnectedListener(ssid);
                return;
            }
            systemWifiManager.disconnect();
            WifiConfiguration wifiConfiguration = createWifiConfiguration(ssid, password, encryptWay, isHidden);
            connect(wifiConfiguration);
        }
    }

    public void close() {
        try {
            WifiManager.getContext().unregisterReceiver(wifiConnectStatusBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean disConnect() {
        return systemWifiManager.disconnect();
    }

    public void forgetNetwork(String ssid) {
        Context context = WifiManager.getContext();
        int checkSelfPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE);
        if (checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
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
    }

    public boolean addOnWifiConnectStateChangedListener(@NonNull OnWifiConnectStateChangedListener onWifiConnectStateChangedListener) {
        if (onWifiConnectStateChangedListeners.contains(onWifiConnectStateChangedListener)) {
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

    public boolean isContainswifi(String ssid) {
        return isExists(ssid) != -1;
    }

    public void connectExistsWifi(String ssid, boolean attemptConnect) {
        int exists = isExists(ssid);
        if (exists == -1) {
            performWifiConnectFailedListener(ssid);
            return;
        }
        systemWifiManager.enableNetwork(exists, attemptConnect);
    }

    void onWifiConnected(String ssid) {
        performWifiConnectedListener(ssid);
    }

    /**
     * 判断当前wifi是否有保存
     *
     * @param ssid 当前WiFi的SSID
     * @return 当前wifi已保存的配置
     */
    private int isExists(String ssid) {
        Context context = WifiManager.getContext();
        int checkSelfPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE);
        if (checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
            List<WifiConfiguration> existingConfigs = systemWifiManager.getConfiguredNetworks();
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (WifiManager.isWifiSsidEquals(existingConfig.SSID, ssid)) {
                    return existingConfig.networkId;
                }
            }
            return -1;
        }
        return -1;
    }

    private IntentFilter makeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);
        return intentFilter;
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

    /**
     * 安卓8.0及以上的系统使用这个方法创建
     *
     * @param ssid       网络SSID
     * @param password   密码
     * @param encryptWay 加密方式
     * @param isHidden   是否为隐藏WiFi
     * @return WifiConfiguration
     */
    private WifiConfiguration createWifiConfiguration(String ssid, String password, EncryptWay encryptWay, boolean isHidden) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";
        if (encryptWay == EncryptWay.NO_ENCRYPT) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.hiddenSSID = isHidden;
        } else if (encryptWay == EncryptWay.WEP_ENCRYPT) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = isHidden;
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (encryptWay == EncryptWay.WPA_ENCRYPT || encryptWay == EncryptWay.WPA_WPA2_ENCRYPT) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = isHidden;
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
        int network = systemWifiManager.addNetwork(config);
        if (network == -1) {
            performWifiConnectFailedListener(config.SSID);
            return;
        }
        boolean enableNetwork = systemWifiManager.enableNetwork(network, true);
        if (enableNetwork) {
            performWifiConnectingListener(config.SSID);
        } else {
            performWifiConnectFailedListener(config.SSID);
        }
    }
}
