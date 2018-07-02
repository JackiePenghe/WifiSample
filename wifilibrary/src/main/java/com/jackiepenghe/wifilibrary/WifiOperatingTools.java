package com.jackiepenghe.wifilibrary;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * WiFi扫描器
 *
 * @author jackie
 */
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

    /*---------------------------构造方法---------------------------*/

    /**
     * 构造方法
     */
    WifiOperatingTools() {
        systemWifiManager = WifiManager.getSystemWifiManager();
        wifiConnectStatusBroadcastReceiver = new WifiConnectStatusBroadcastReceiver(systemWifiManager);
        wifiScanDataAndStatusBroadcastReceiver = new WifiScanDataAndStatusBroadcastReceiver(systemWifiManager);
        context = WifiManager.getContext();
        context.registerReceiver(wifiConnectStatusBroadcastReceiver, makeWifiConnectStatusBroadcastReceiverIntentFilter());
        context.registerReceiver(wifiScanDataAndStatusBroadcastReceiver, makeWifiScanDataAndStatusBroadcastReceiverIntentFilter());
    }

    /*---------------------------公开方法---------------------------*/

    /**
     * 开始连接
     *
     * @param activity   Activity
     * @param scanResult 扫描设备
     */
    public void startConnect(Activity activity, final ScanResult scanResult) {
        checkInitStatus();
        final EncryptWay encryptionWay = getEncryptionWay(scanResult);
        final WifiConfiguration[] config = {isExists(scanResult.SSID, encryptionWay)};
        if (config[0] == null) {
            //需要密码
            if (encryptionWay != EncryptWay.NO_ENCRYPT) {
                //搜到的WiFi的SSID有内容（可见WiFi）
                if (scanResult.SSID != null && !"".equals(scanResult.SSID)) {
                    showConnectWifiWithPassWord(activity, scanResult, encryptionWay, config);
                }
                //搜到的WiFi的SSID无内容（隐藏的WiFi）
                else {
                    Tool.warnOut(TAG, "连接隐藏WiFi");
                    showConnectWifiWithNameAndPassWord(activity, encryptionWay, config);
                }
            } else {
                config[0] = createWifiInfo(scanResult.SSID, "", encryptionWay);
                connect(config[0]);
            }
        } else {
            connect(config[0]);
        }
    }

    /**
     * 显示连接隐藏WiFi
     *
     * @param activity      Activity
     * @param encryptionWay 加密方式
     * @param config        WifiConfiguration[](final类型)
     */
    private void showConnectWifiWithNameAndPassWord(Activity activity, final EncryptWay encryptionWay, final WifiConfiguration[] config) {
        final View view = View.inflate(context, R.layout.hidden_wifi_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
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
                            Tool.toastL(context, R.string.wifi_name_null);
                            return;
                        }

                        String wifiPassword = wifiPasswordEt.getText().toString();
                        if ("".equals(wifiPassword) || wifiPassword.length() < WIFI_PASSWORD_MIN_LENGTH) {
                            Tool.toastL(context, R.string.wifi_password_null);
                            return;
                        }
                        config[0] = createWifiInfo(wifiName, wifiPassword, encryptionWay);
                        connect(config[0]);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (wifiConnectCallback != null) {
                            HANDLER.post(new Runnable() {
                                @Override
                                public void run() {
                                    wifiConnectCallback.cancelConnect();
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
    public void init(@NonNull WifiScanCallback wifiScanCallback, @NonNull WifiConnectCallback wifiConnectCallback) {
        this.wifiScanCallback = wifiScanCallback;
        wifiConnectStatusBroadcastReceiver.setWifiConnectCallback(wifiConnectCallback);
        this.wifiConnectCallback = wifiConnectCallback;
        isInit = true;
    }

    /**
     * 开始扫描
     */
    public void startScan() {
        checkInitStatus();
        boolean result = systemWifiManager.startScan();
        if (!result) {
            if (wifiScanCallback != null) {
                wifiScanCallback.startScanFailed();
            }
        } else {
            wifiScanCallback.isScanning();
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
        wifiScanDataAndStatusBroadcastReceiver.setWifiScanResultObtainedListener(wifiScanResultObtainedListener);
    }

    /*---------------------------公开静态方法---------------------------*/
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

    /*---------------------------枚举定义---------------------------*/

    /**
     * 加密方式的枚举
     */
    public enum EncryptWay {
        /**
         * 未加密
         */
        NO_ENCRYPT(1),
        /**
         * WPA/WPA2加密
         */
        WPA_WPA2_ENCRYPT(2),
        /**
         * WEP加密
         */
        WEP_ENCRYPT(3),
        /**
         * 仅WPA加密
         */
        WPA_ENCRYPT(4),
        /**
         * PSK加密
         */
        EAP_ENCRYPT(5),
        /**
         * 仅WPA2加密
         */
        WPA2_ENCRYPT(6),
        /**
         * 未知加密方式
         */
        UNKNOWN__ENCRYPT(7);

        int encryptWay;

        EncryptWay(int encryptWay) {
            this.encryptWay = encryptWay;
        }

        /**
         * 获取当前的枚举的值
         *
         * @return 当前的枚举的值
         */
        @SuppressWarnings("unused")
        public int getEncryptWayValue() {
            return encryptWay;
        }

        /**
         * 通过枚举的值来获取枚举对象
         *
         * @param encryptWayValue 枚举的值
         * @return 枚举对象
         */
        @SuppressWarnings("unused")
        public static EncryptWay getEncryptWayByValue(int encryptWayValue) {
            switch (encryptWayValue) {
                case 1:
                    return EncryptWay.NO_ENCRYPT;
                case 2:
                    return EncryptWay.WPA_WPA2_ENCRYPT;
                case 3:
                    return EncryptWay.WEP_ENCRYPT;
                default:
                    return EncryptWay.UNKNOWN__ENCRYPT;
            }
        }
    }

    /**
     * 获取WiFi的加密方式
     *
     * @param config WiFi的配置
     * @return WiFi加密方式
     */
    @SuppressWarnings("WeakerAccess")
    public static EncryptWay geEncryptWay(WifiConfiguration config) {
        if (config == null) {
            return null;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.NONE)) {
            return EncryptWay.NO_ENCRYPT;
        }

        //KeyMgmt.WPA2_PSK
        int wpa2Psk = 4;
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK) && config.allowedKeyManagement.get(wpa2Psk)) {
            return EncryptWay.WPA_WPA2_ENCRYPT;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return EncryptWay.WPA_ENCRYPT;
        }
        if (config.allowedKeyManagement.get(wpa2Psk)) {
            return EncryptWay.WPA2_ENCRYPT;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) && config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return EncryptWay.EAP_ENCRYPT;
        }

        if (config.wepKeys[0] != null) {
            return EncryptWay.WEP_ENCRYPT;
        } else {
            return EncryptWay.NO_ENCRYPT;
        }
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
        void wifiScanResultObtained(ArrayList<WifiDevice> wifiDevices);
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
         */
        void cancelConnect();
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
    private WifiConfiguration isExists(String ssid, EncryptWay encryptWay) {
        List<WifiConfiguration> existingConfigs = systemWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            String s = "\"" + ssid + "\"";
            if (existingConfig.SSID.equals(s)) {
                if (geEncryptWay(existingConfig) == encryptWay) {
                    return existingConfig;
                }
            }
        }
        return null;
    }

    private WifiConfiguration createWifiInfo(String ssid, String password, EncryptWay encryptWay) {
        Log.w(TAG, "ssid = " + ssid + "password " + password + "encryptWay =" + encryptWay);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";
        if (encryptWay == EncryptWay.NO_ENCRYPT) {
            config.wepKeys[0] = "\"" + "\"";
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (encryptWay == EncryptWay.WEP_ENCRYPT) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (encryptWay == EncryptWay.WPA_ENCRYPT || encryptWay == EncryptWay.WPA_WPA2_ENCRYPT) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }

    /**
     * 连接指定的WiFi网络
     *
     * @param activity      Activity
     * @param scanResult    扫描结果
     * @param encryptionWay WiFi加密方式
     * @param config        WiFi配置
     */
    private void showConnectWifiWithPassWord(final Activity activity, final ScanResult scanResult, final EncryptWay encryptionWay, final WifiConfiguration[] config) {
        final EditText editText = new EditText(context);
        new AlertDialog.Builder(activity)
                .setTitle(R.string.input_wifi_password)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(editText)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = editText.getText().toString();
                        Log.w(TAG, "editText.getText():" + password);
                        if ("".equals(password) || password.length() < WIFI_PASSWORD_MIN_LENGTH) {
                            Tool.toastL(context, R.string.wifi_password_null);
                            showConnectWifiWithPassWord(activity, scanResult, encryptionWay, config);
                            return;
                        }
                        config[0] = createWifiInfo(scanResult.SSID, password, encryptionWay);
                        connect(config[0]);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (wifiConnectCallback != null) {
                            HANDLER.post(new Runnable() {
                                @Override
                                public void run() {
                                    wifiConnectCallback.cancelConnect();
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
        systemWifiManager.enableNetwork(network, true);
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
}
