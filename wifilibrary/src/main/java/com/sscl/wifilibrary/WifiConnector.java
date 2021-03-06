package com.sscl.wifilibrary;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.PatternMatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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
    private final ConnectivityManager connectivityManager;
    private String ssid = WifiManager.getRealSsid(WifiManager.getConnectedWifiSsid());
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            WifiManager.getHANDLER().post(new Runnable() {
                @Override
                public void run() {
                    performWifiConnectedListener(ssid);
                }
            });
        }

        @Override
        public void onLosing(@NonNull Network network, int maxMsToLive) {
            super.onLosing(network, maxMsToLive);
            DebugUtil.warnOut(TAG, "onLosing");
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            DebugUtil.warnOut(TAG, "onLost");
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            DebugUtil.warnOut(TAG, "onCapabilitiesChanged networkCapabilities = " + networkCapabilities);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.reportNetworkConnectivity(network, false);
            }
        }

        @Override
        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
            DebugUtil.warnOut(TAG, "onLinkPropertiesChanged");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.reportNetworkConnectivity(network, false);
            }
        }

        @Override
        public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
            super.onBlockedStatusChanged(network, blocked);
            DebugUtil.warnOut(TAG, "onBlockedStatusChanged");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.reportNetworkConnectivity(network, false);
            }
        }

        @Override
        public void onUnavailable() {
            WifiManager.getHANDLER().post(new Runnable() {
                @Override
                public void run() {
                    performWifiConnectFailedListener(ssid);
                }
            });
        }
    };

    WifiConnector() {
        WifiManager.getContext().registerReceiver(wifiConnectStatusBroadcastReceiver, makeIntentFilter());
        systemWifiManager = WifiManager.getSystemWifiManager();
        connectivityManager = (ConnectivityManager) WifiManager.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
            }
        }
    }

    public void connect(@NonNull final String ssid, @Nullable String password, boolean isHidden, EncryptWay encryptWay, boolean attemptConnect) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
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
                DebugUtil.warnOut(TAG, "connectedWifiSsid = " + connectedWifiSsid);
                if (connectedWifiSsid != null && WifiManager.isWifiSsidEquals(connectedWifiSsid, ssid)) {
                    performWifiConnectedListener(ssid);
                    return;
                }
                systemWifiManager.disconnect();
                WifiConfiguration wifiConfiguration = createWifiConfiguration(ssid, password, encryptWay, isHidden);
                connect(wifiConfiguration);
            }
        } else {
            WifiConnector.this.ssid = ssid;
            String connectedWifiSsid = WifiManager.getConnectedWifiSsid();
            DebugUtil.warnOut(TAG, "connectedWifiSsid = " + connectedWifiSsid);
            if (connectedWifiSsid != null && WifiManager.isWifiSsidEquals(connectedWifiSsid, ssid)) {
                performWifiConnectedListener(ssid);
                return;
            }
            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder()
//                    .setSsid(ssid)
                    .setSsidPattern(new PatternMatcher(ssid, PatternMatcher.PATTERN_PREFIX))
                    .setIsHiddenSsid(isHidden);
            switch (encryptWay) {
                case WPA_ENCRYPT:
                case WPA2_ENCRYPT:
                case WPA_WPA2_ENCRYPT:
                case WEP_ENCRYPT:
                case EAP_ENCRYPT:
                    if (password != null) {
                        builder.setWpa2Passphrase(password);
                    }
                    break;
                case UNKNOWN_ENCRYPT:
                    if (password != null) {
                        builder.setWpa3Passphrase(password);
                    }
                    break;
                case NO_ENCRYPT:
                default:
                    break;
            }
            WifiNetworkSpecifier specifier = builder.build();
            NetworkRequest request =
                    new NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .setNetworkSpecifier(specifier)
                            .build();
            connectivityManager.requestNetwork(request, networkCallback);
        }
    }


    public void close() {
        try {
            WifiManager.getContext().unregisterReceiver(wifiConnectStatusBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    connectivityManager.unregisterNetworkCallback(networkCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Deprecated
    public boolean disConnect() {
        return systemWifiManager.disconnect();
    }

    @Deprecated
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
