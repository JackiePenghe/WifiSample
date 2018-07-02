package com.jackiepenghe.wifilibrary;

import android.content.Context;
import android.net.wifi.ScanResult;

/**
 * WiFi设备bean类
 *
 * @author jackie
 */
public class WifiDevice {

    /*---------------------------静态常量---------------------------*/

    private static final int LEVEL_1 = -50;
    private static final int LEVEL_2 = -60;
    private static final int LEVEL_3 = -70;
    private static final int LEVEL_4 = -80;

    /*---------------------------成员变量---------------------------*/

    /**
     * WiFi扫描结果
     */
    private ScanResult scanResult;

    /**
     * 上下文
     */
    private Context context;
    /*---------------------------构造方法---------------------------*/

    /**
     * 构造方法
     *
     * @param scanResult WiFi设备扫描结果
     */
    WifiDevice(Context context, ScanResult scanResult) {
        this.context = context;
        this.scanResult = scanResult;
    }

    /*---------------------------getter---------------------------*/

    public ScanResult getScanResult() {
        return scanResult;
    }

    /*---------------------------公开方法---------------------------*/

    public String getSSID() {
        return scanResult.SSID;
    }

    public String getBSSID() {
        return scanResult.BSSID;
    }

    public int getIntLevel() {
        return scanResult.level;
    }

    public String getStringLevel() {
        int level = scanResult.level;
        if (level >= LEVEL_1) {
            return context.getString(R.string.level_1);
        } else if (level >= LEVEL_2) {
            return context.getString(R.string.level_2);
        } else if (level >= LEVEL_3) {
            return context.getString(R.string.level_3);
        } else if (level >= LEVEL_4) {
            return context.getString(R.string.level_4);
        } else {
            return context.getString(R.string.level_5);
        }
    }

    public int getLevelDrawableResId() {
        int level = scanResult.level;
        if (level >= LEVEL_1) {
            return R.drawable.wifi_signal_4_full;
        } else if (level >= LEVEL_2) {
            return R.drawable.wifi_signal_3;
        } else if (level >= LEVEL_3) {
            return R.drawable.wifi_signal_2;
        } else if (level >= LEVEL_4) {
            return R.drawable.wifi_signal_1;
        } else {
            return R.drawable.wifi_signal_0;
        }
    }

    /**
     * 获取WiFi的加密方式
     *
     * @return WiFi的加密方式
     */
    public String getEncryptionWayString() {
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
}
