package com.jackiepenghe.wifilibrary;


import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.jackiepenghe.wifilibrary.WifiDevice.EncryptWay;

import java.util.ArrayList;
import java.util.List;

/**
 * WiFi扫描器
 *
 * @author jackie
 */
@SuppressWarnings("unused")
public class WifiOperatingTools {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = WifiOperatingTools.class.getSimpleName();
    /**
     * WiFi密码的最小长度
     */
    private static final int WIFI_PASSWORD_MIN_LENGTH = 8;
    /**
     * Handler
     */
    private static final Handler HANDLER = new Handler();

    /*---------------------------成员变量---------------------------*/

    /**
     * 系统WiFi管理器
     */
    private android.net.wifi.WifiManager systemWifiManager;
    /**
     * 记录初始化的状态
     */
    private boolean isInit;
    /**
     * WiFi扫描的回调
     */
    private WifiScanCallback wifiScanCallback;
    /**
     * 上下文，用于广播接收者的注册
     */
    private Context context;
    /**
     * WiFi连接相关的广播接收者
     */
    private WifiConnectStatusBroadcastReceiver wifiConnectStatusBroadcastReceiver;
    /**
     * WiFi扫描相关的广播接收者
     */
    private WifiScanDataAndStatusBroadcastReceiver wifiScanDataAndStatusBroadcastReceiver;
    /**
     * WiFi连接相关的回调
     */
    private WifiConnectCallback wifiConnectCallback;
    /**
     * 获取到WiFi扫描结果的相关回调
     */
    private WifiScanResultObtainedListener wifiScanResultObtainedListener;

    /*---------------------------构造方法---------------------------*/

    /**
     * 构造方法
     */
    WifiOperatingTools() {
        systemWifiManager = WifiManager.getSystemWifiManager();
        wifiConnectStatusBroadcastReceiver = new WifiConnectStatusBroadcastReceiver(systemWifiManager);
        wifiScanDataAndStatusBroadcastReceiver = new WifiScanDataAndStatusBroadcastReceiver(systemWifiManager);
        context = WifiManager.getContext();
    }

    /*---------------------------公开方法---------------------------*/

    public boolean removeWifiInfo(WifiInfo wifiInfo) {
        return removeWifiInfo(wifiInfo.getSSID());
    }

    public boolean removeWifiInfo(WifiDevice wifiDevice) {
        return removeWifiInfo(wifiDevice.getSSID());
    }

    @SuppressWarnings("WeakerAccess")
    public boolean removeWifiInfo(String wifiSsid) {
        int netId = isExists(wifiSsid);
        if (-1 != netId) {
            systemWifiManager.removeNetwork(netId);
            return true;
        }
        return false;
    }

    /**
     * 发起连接
     *
     * @param wifiInfo WifiInfo
     */
    public void startConnect(WifiInfo wifiInfo) {
        int netId = isExists(wifiInfo.getSSID());
        if (-1 != netId) {
            boolean enableNetwork = systemWifiManager.enableNetwork(netId, true);
            if (enableNetwork) {
                if (wifiConnectCallback != null) {
                    wifiConnectCallback.connecting(wifiInfo.getSSID());
                }
            } else {
                if (wifiConnectCallback != null) {
                    wifiConnectCallback.connectFailed(wifiInfo.getSSID());
                }
            }
            return;
        }
        if (wifiConnectCallback != null) {
            wifiConnectCallback.connectFailed(wifiInfo.getSSID());
        }
    }

    /**
     * 发起连接
     *
     * @param context    上下文
     * @param wifiDevice WiFi设备
     */
    public void startConnect(Context context, WifiDevice wifiDevice) {
        startConnect(context, wifiDevice, null);
    }

    /**
     * 发起连接
     *
     * @param context  上下文
     * @param wifiSSid WiFi名
     */
    public void startConnect(Context context, String wifiSSid) {
        startConnect(context, wifiSSid, null);
    }

    /**
     * 发起连接
     *
     * @param context  上下文
     * @param wifiSSid WiFi名
     * @param password WiFi密码
     */
    @SuppressWarnings("WeakerAccess")
    public void startConnect(Context context, String wifiSSid, String password) {
        startConnect(context, wifiSSid, password, 0);
    }


    /**
     * 发起连接
     *
     * @param context    上下文
     * @param wifiDevice 扫描设备
     */
    @SuppressWarnings("WeakerAccess")
    public void startConnect(Context context, WifiDevice wifiDevice, String password) {
        if (password == null) {
            connectWifiDeviceWithoutPassword(context, wifiDevice);
        } else {
            connectWifiDeviceWithPassword(context, wifiDevice, password);
        }
    }

    private void connectWifiDeviceWithPassword(Context context, WifiDevice wifiDevice, String password) {
        checkInitStatus();
        String connectedWifiSSID = WifiManager.getConnectedWifiSSID();
        if (connectedWifiSSID != null && WifiManager.isWifiSsidEquals(connectedWifiSSID, wifiDevice.getSSID())) {
            if (wifiConnectCallback != null) {
                wifiConnectCallback.connected(wifiDevice.getSSID());
            }
            return;
        }
        systemWifiManager.disconnect();
        int netId = isExists(wifiDevice.getSSID());
        if (-1 != netId) {
            boolean enableNetwork = systemWifiManager.enableNetwork(netId, true);
            if (enableNetwork) {
                if (wifiConnectCallback != null) {
                    wifiConnectCallback.connecting(wifiDevice.getSSID());
                }
            } else {
                if (wifiConnectCallback != null) {
                    wifiConnectCallback.connectFailed(wifiDevice.getSSID());
                }
            }
            return;
        }

        EncryptWay encryptWay = wifiDevice.getEncryptWay();
        //不需要密码
        if (encryptWay != EncryptWay.NO_ENCRYPT) {
            //连接WiFi
            if (!wifiDevice.isHidden()) {
                WifiConfiguration wifiConfiguration = createWifiInfo(wifiDevice.getSSID(), password, wifiDevice.getEncryptWay(), false);
                connect(wifiConfiguration);
            }
            //连接隐藏的WiFi
            else {
                Tool.warnOut(TAG, "连接隐藏WiFi");
                showConnectWifiWithNameAndPassWord(context, encryptWay);
            }
        } else {
            WifiConfiguration wifiConfiguration = createWifiInfo(wifiDevice.getSSID(), password, encryptWay, false);
            connect(wifiConfiguration);
        }
    }

    private void connectWifiDeviceWithoutPassword(Context context, WifiDevice wifiDevice) {
        checkInitStatus();
        String connectedWifiSSID = WifiManager.getConnectedWifiSSID();
        if (connectedWifiSSID != null && WifiManager.isWifiSsidEquals(connectedWifiSSID, wifiDevice.getSSID())) {
            if (wifiConnectCallback != null) {
                wifiConnectCallback.connected(wifiDevice.getSSID());
            }
            return;
        }
        systemWifiManager.disconnect();
        int netId = isExists(wifiDevice.getSSID());
        if (-1 != netId) {
            boolean enableNetwork = systemWifiManager.enableNetwork(netId, true);
            if (enableNetwork) {
                if (wifiConnectCallback != null) {
                    wifiConnectCallback.connecting(wifiDevice.getSSID());
                }
            } else {
                if (wifiConnectCallback != null) {
                    wifiConnectCallback.connectFailed(wifiDevice.getSSID());
                }
            }
            return;
        }

        EncryptWay encryptWay = wifiDevice.getEncryptWay();
        //不需要密码
        if (encryptWay != EncryptWay.NO_ENCRYPT) {
            //连接WiFi
            if (!wifiDevice.isHidden()) {
                showConnectWifiWithPassWord(context, wifiDevice);
            }
            //连接隐藏的WiFi
            else {
                Tool.warnOut(TAG, "连接隐藏WiFi");
                showConnectWifiWithNameAndPassWord(context, encryptWay);
            }
        } else {
            WifiConfiguration wifiConfiguration = createWifiInfo(wifiDevice.getSSID(), "", encryptWay, false);
            connect(wifiConfiguration);
        }
    }

    /**
     * 显示连接隐藏WiFi
     *
     * @param context       上下文
     * @param encryptionWay 加密方式
     */
    private void showConnectWifiWithNameAndPassWord(Context context, final EncryptWay encryptionWay) {
        final View view = View.inflate(this.context, R.layout.com_jackiepenghe_hidden_wifi_dialog, null);
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
                            Tool.toastL(WifiOperatingTools.this.context, R.string.wifi_name_null);
                            return;
                        }

                        String wifiPassword = wifiPasswordEt.getText().toString();
                        if ("".equals(wifiPassword) || wifiPassword.length() < WIFI_PASSWORD_MIN_LENGTH) {
                            Tool.toastL(WifiOperatingTools.this.context, R.string.wifi_password_null);
                            return;
                        }
                        WifiConfiguration wifiConfiguration = createWifiInfo(wifiName, wifiPassword, encryptionWay, true);
                        connect(wifiConfiguration);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (wifiConnectCallback != null) {
                            HANDLER.post(new Runnable() {
                                @Override
                                public void run() {
                                    wifiConnectCallback.cancelConnect(WifiOperatingTools.this.context.getString(R.string.hidden_network));
                                }
                            });
                        }
                    }
                })
                .setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /*---------------------------公开方法---------------------------*/

    /**
     * 初始化
     */
    @SuppressWarnings("unused")
    public void init() {
        init(new DefaultWifiScanCallback(), new DefaultWifiConnectCallback());
    }

    /**
     * 初始化
     *
     * @param wifiScanCallback WiFi扫描相关的回调
     */
    @SuppressWarnings("unused")
    public void init(@NonNull WifiScanCallback wifiScanCallback) {
        init(wifiScanCallback, new DefaultWifiConnectCallback());
    }

    /**
     * 初始化
     *
     * @param wifiConnectCallback WiFi连接相关的回调
     */
    @SuppressWarnings("unused")
    public void init(@NonNull WifiConnectCallback wifiConnectCallback) {
        init(new DefaultWifiScanCallback(), wifiConnectCallback);
    }

    /**
     * 初始化
     *
     * @param wifiScanCallback    WiFi扫描相关的回调
     * @param wifiConnectCallback WiFi连接相关的回调
     */
    public void init(@NonNull WifiScanCallback wifiScanCallback, @NonNull WifiConnectCallback wifiConnectCallback) {
        context.registerReceiver(wifiConnectStatusBroadcastReceiver, makeWifiConnectStatusBroadcastReceiverIntentFilter());
        context.registerReceiver(wifiScanDataAndStatusBroadcastReceiver, makeWifiScanDataAndStatusBroadcastReceiverIntentFilter());
        this.wifiScanCallback = wifiScanCallback;
        wifiConnectStatusBroadcastReceiver.setWifiConnectCallback(wifiConnectCallback);
        this.wifiConnectCallback = wifiConnectCallback;
        isInit = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean isScanAlwaysAvailable() {
        return systemWifiManager.isScanAlwaysAvailable();
    }

    /**
     * 手动请求获取WiFi扫描结果
     */
    public void requestScanResult() {
        List<ScanResult> scanResults = systemWifiManager.getScanResults();
        if (scanResults != null && scanResults.size() > 0) {
            ArrayList<WifiDevice> wifiDevices = new ArrayList<>();
            for (int i = 0; i < scanResults.size(); i++) {
                ScanResult scanResult = scanResults.get(i);
                WifiDevice wifiDevice = new WifiDevice( scanResult);
                wifiDevices.add(wifiDevice);
            }
            if (wifiScanResultObtainedListener != null) {
                wifiScanResultObtainedListener.wifiScanResultObtained(wifiDevices);
            }
        } else {
            wifiScanResultObtainedListener.wifiScanResultObtained(null);
        }
    }

    /**
     * 开始扫描
     */
    public void startScan() {
        checkInitStatus();
        if (!WifiManager.isWifiEnabled()) {
            if (wifiScanCallback != null) {
                wifiScanCallback.startScanFailed();
            }
            WifiManager.enableWifi(true);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            wifiScanCallback.isScanning();
            List<ScanResult> scanResults = systemWifiManager.getScanResults();
            if (scanResults != null && scanResults.size() > 0) {
                ArrayList<WifiDevice> wifiDevices = new ArrayList<>();
                for (int i = 0; i < scanResults.size(); i++) {
                    ScanResult scanResult = scanResults.get(i);
                    WifiDevice wifiDevice = new WifiDevice( scanResult);
                    wifiDevices.add(wifiDevice);
                }
                if (wifiScanResultObtainedListener != null) {
                    wifiScanResultObtainedListener.wifiScanResultObtained(wifiDevices);
                }
            } else {
                wifiScanResultObtainedListener.wifiScanResultObtained(null);
            }
        } else {
            boolean result = systemWifiManager.startScan();
            if (!result) {
                if (wifiScanCallback != null) {
                    wifiScanCallback.startScanFailed();
                }
            } else {
                wifiScanCallback.isScanning();
                List<ScanResult> scanResults = systemWifiManager.getScanResults();
                if (scanResults != null && scanResults.size() > 0) {
                    ArrayList<WifiDevice> wifiDevices = new ArrayList<>();
                    for (int i = 0; i < scanResults.size(); i++) {
                        ScanResult scanResult = scanResults.get(i);
                        WifiDevice wifiDevice = new WifiDevice( scanResult);
                        wifiDevices.add(wifiDevice);
                    }
                    if (wifiScanResultObtainedListener != null) {
                        wifiScanResultObtainedListener.wifiScanResultObtained(wifiDevices);
                    }
                } else {
                    wifiScanResultObtainedListener.wifiScanResultObtained(null);
                }
            }
        }
    }

    /**
     * 关闭扫描器
     */
    public void close() {
        checkInitStatus();
        if (wifiConnectStatusBroadcastReceiver != null) {
            wifiConnectStatusBroadcastReceiver.setWifiConnectCallback(null);
        }
        if (wifiScanDataAndStatusBroadcastReceiver != null) {
            wifiScanDataAndStatusBroadcastReceiver.setWifiScanResultObtainedListener(null);
        }

        try {
            context.unregisterReceiver(wifiScanDataAndStatusBroadcastReceiver);
            context.unregisterReceiver(wifiConnectStatusBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        systemWifiManager = null;
        wifiScanCallback = null;
        context = null;
        wifiConnectStatusBroadcastReceiver = null;
        wifiScanDataAndStatusBroadcastReceiver = null;
    }

    /**
     * 设置获取到WiFi扫描的结果时的回调
     *
     * @param wifiScanResultObtainedListener 获取到WiFi扫描的结果时的回调
     */
    public void setWifiScanResultObtainedListener(WifiScanResultObtainedListener wifiScanResultObtainedListener) {
        checkInitStatus();
        this.wifiScanResultObtainedListener = wifiScanResultObtainedListener;
        if (wifiScanDataAndStatusBroadcastReceiver != null) {
            wifiScanDataAndStatusBroadcastReceiver.setWifiScanResultObtainedListener(wifiScanResultObtainedListener);
        }
    }

    /*---------------------------公开方法---------------------------*/

    @SuppressWarnings("unused")
    public void sendData(byte[] data) {
        String wifiIp = getWifiIp();
    }

    /**
     * 获取WiFi的IP地址
     *
     * @return WiFi的IP地址
     */
    @SuppressWarnings("WeakerAccess")
    public String getWifiIp() {
        checkInitStatus();
        //检查Wifi状态
        if (!systemWifiManager.isWifiEnabled()) {
            return null;
        }
        WifiInfo wi = systemWifiManager.getConnectionInfo();
        //获取32位整型IP地址
        int ipAdd = wi.getIpAddress();
        //把整型地址转换成“*.*.*.*”地址
        return intToIp(ipAdd);
    }

    /**
     * 设置扫描相关的回调
     *
     * @param wifiScanCallback 扫描相关的回调
     */
    public void setWifiScanCallback(WifiScanCallback wifiScanCallback) {
        this.wifiScanCallback = wifiScanCallback;
    }

    /**
     * 设置连接相关的回调
     *
     * @param wifiConnectCallback 接相关的回调
     */
    public void setWifiConnectCallback(WifiConnectCallback wifiConnectCallback) {
        this.wifiConnectCallback = wifiConnectCallback;
        wifiConnectStatusBroadcastReceiver.setWifiConnectCallback(wifiConnectCallback);
    }

    /**
     * 断开当前WiFi
     *
     * @return 请求是否成功
     */
    public boolean disconnect() {
        checkInitStatus();
        return systemWifiManager.disconnect();
    }

    /*---------------------------接口定义---------------------------*/

    /**
     * WiFi扫描的相关回调
     */
    public interface WifiScanCallback {
        /**
         * 扫描开启失败
         */
        void startScanFailed();

        /**
         * 扫描开启成功，正在扫描中
         */
        void isScanning();
    }

    /**
     * 获取到WiFi扫描的结果时的回调
     */
    public interface WifiScanResultObtainedListener {
        /**
         * 获取到WiFi扫描的结果
         *
         * @param wifiDevices WiFi扫描的结果
         */
        void wifiScanResultObtained(@Nullable ArrayList<WifiDevice> wifiDevices);
    }

    /**
     * WiFi连接时的相关回调
     */
    public interface WifiConnectCallback {
        /**
         * 正在连接
         *
         * @param ssid WiFi的名称
         */
        void connecting(String ssid);

        /**
         * 已连接
         *
         * @param ssid WiFi的名称
         */
        void connected(String ssid);

        /**
         * 已断开连接
         */
        void disconnected();

        /**
         * 正在进行身份授权
         *
         * @param ssid WiFi的名称
         */
        void authenticating(String ssid);

        /**
         * 正在获取IP地址
         *
         * @param ssid WiFi的名称
         */
        void obtainingIpAddress(String ssid);

        /**
         * 连接失败
         *
         * @param ssid WiFi的名称
         */
        void connectFailed(String ssid);

        /**
         * 正在断开连接
         */
        void disconnecting();

        /**
         * 未知状态
         */
        void unknownStatus();

        /**
         * 用户取消了连接动作
         *
         * @param ssid 准备连接的WiFi SSID
         */
        void cancelConnect(String ssid);
    }

    /*---------------------------私有方法---------------------------*/


    /**
     * 检查是否已经初始化
     */
    private void checkInitStatus() {
        if (!isInit) {
            throw new IllegalStateException("Please invoke method init(...) before invoke this method");
        }
    }


    /**
     * 判断当前wifi是否有保存
     *
     * @param ssid 当前WiFi的SSID
     * @return 当前wifi已保存的配置
     */
    private int isExists(String ssid) {
        List<WifiConfiguration> existingConfigs = systemWifiManager.getConfiguredNetworks();

        for (WifiConfiguration existingConfig : existingConfigs) {
            if (WifiManager.isWifiSsidEquals(existingConfig.SSID, ssid)) {
                return existingConfig.networkId;
            }
        }
        return -1;
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
    private WifiConfiguration createWifiInfoO(String ssid, String password, EncryptWay encryptWay, boolean isHiddenSSID) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";
        if (encryptWay == EncryptWay.NO_ENCRYPT) {
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            config.hiddenSSID = isHiddenSSID;
        } else if (encryptWay == EncryptWay.WEP_ENCRYPT) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = isHiddenSSID;
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(KeyMgmt.NONE);
        } else if (encryptWay == EncryptWay.WPA_ENCRYPT || encryptWay == EncryptWay.WPA_WPA2_ENCRYPT) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = isHiddenSSID;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }

    /**
     * 低版本使用这个方法创建
     *
     * @param ssid         网络SSID
     * @param password     密码
     * @param encryptWay   加密方式
     * @param isHiddenSSID 是否为隐藏WiFi
     * @return WifiConfiguration
     */
    private WifiConfiguration createWifiInfoNormal(String ssid, String password, EncryptWay encryptWay, boolean isHiddenSSID) {
        Log.w(TAG, "ssid = " + ssid + "password " + password + "encryptWay =" + encryptWay);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";
        if (encryptWay == EncryptWay.NO_ENCRYPT) {
            //noinspection AliDeprecation
            config.wepKeys[0] = "\"" + "\"";
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            config.hiddenSSID = isHiddenSSID;
            //noinspection AliDeprecation
            config.wepTxKeyIndex = 0;
        } else if (encryptWay == EncryptWay.WEP_ENCRYPT) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = isHiddenSSID;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            //noinspection AliDeprecation
            config.wepTxKeyIndex = 0;
        } else if (encryptWay == EncryptWay.WPA_ENCRYPT || encryptWay == EncryptWay.WPA_WPA2_ENCRYPT) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = isHiddenSSID;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            //noinspection AliDeprecation
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }

    private WifiConfiguration createWifiInfo(String ssid, String password, EncryptWay encryptWay, boolean isHiddenSSID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return createWifiInfoO(ssid, password, encryptWay, isHiddenSSID);
        } else {
            return createWifiInfoNormal(ssid, password, encryptWay, isHiddenSSID);
        }
    }

    /**
     * 连接指定的WiFi网络
     *
     * @param context    上下文
     * @param wifiDevice 扫描结果
     */
    private void showConnectWifiWithPassWord(final Context context, final WifiDevice wifiDevice) {
        final EditText editText = (EditText) View.inflate(context, R.layout.com_jackiepenghe_edit_text, null);
        new AlertDialog.Builder(context)
                .setTitle(R.string.input_wifi_password)
                .setView(editText)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = editText.getText().toString();
                        Log.w(TAG, "editText.getText():" + password);
                        if ("".equals(password) || password.length() < WIFI_PASSWORD_MIN_LENGTH) {
                            Tool.toastL(WifiOperatingTools.this.context, R.string.wifi_password_null);
                            showConnectWifiWithPassWord(context, wifiDevice);
                            return;
                        }
                        WifiConfiguration wifiConfiguration = createWifiInfo(wifiDevice.getSSID(), password, wifiDevice.getEncryptWay(), false);
                        connect(wifiConfiguration);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (wifiConnectCallback != null) {
                            HANDLER.post(new Runnable() {
                                @Override
                                public void run() {
                                    wifiConnectCallback.cancelConnect(wifiDevice.getSSID());
                                }
                            });
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 开始连接
     *
     * @param config WiFi配置数据
     */
    private void connect(WifiConfiguration config) {
        int network = systemWifiManager.addNetwork(config);
        if (network == -1) {
            if (wifiConnectCallback != null) {
                wifiConnectCallback.connectFailed(config.SSID);
            }
            return;
        }
        boolean enableNetwork = systemWifiManager.enableNetwork(network, true);
        if (enableNetwork) {
            if (wifiConnectCallback != null) {
                wifiConnectCallback.connecting(config.SSID);
            }
        } else {
            if (wifiConnectCallback != null) {
                wifiConnectCallback.connectFailed(config.SSID);
            }
        }
    }

    /**
     * 将32位的整型的IP地址转为*.*.*.*格式
     *
     * @param ip 32位的整型的IP
     * @return *.*.*.*格式的IP地址
     */
    private String intToIp(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    private IntentFilter makeWifiConnectStatusBroadcastReceiverIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);
        return intentFilter;
    }

    private IntentFilter makeWifiScanDataAndStatusBroadcastReceiverIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        return intentFilter;
    }

    private void startConnect(Context context, String wifiSSid, String password, int tryCount) {
        tryCount++;
        List<ScanResult> scanResults = systemWifiManager.getScanResults();
        WifiDevice wifiDevice = null;
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult scanResult = scanResults.get(i);
            if (WifiManager.isWifiSsidEquals(scanResult.SSID, wifiSSid)) {
                wifiDevice = new WifiDevice( scanResult);
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
}
