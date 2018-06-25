package com.jackiepenghe.wifilibrary;

import android.net.wifi.WifiConfiguration;

class DefaultWifiHotspotCreateCallback implements WifiHotspotController.WifiHotspotCreateCallback {

    /*------------------------静态常量----------------------------*/

    private static final String TAG = DefaultWifiHotspotCreateCallback.class.getSimpleName();

    /**
     * 热点正在创建
     *
     * @param wifiConfiguration 热点的WiFi配置
     */
    @Override
    public void onWifiHotspotCreating(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration != null) {
            Tool.warnOut(TAG, "热点正在创建 SSID = " + wifiConfiguration.SSID + "password = " + wifiConfiguration.preSharedKey);
        }else {
            Tool.warnOut(TAG, "热点正在创建");
        }
    }

    /**
     * 热点创建完成
     *
     * @param wifiConfiguration 热点的WiFi配置
     */
    @Override
    public void onWifiHotspotCreated(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration != null) {
            Tool.warnOut(TAG, "热点创建成功 SSID = " + wifiConfiguration.SSID + "password = " + wifiConfiguration.preSharedKey);
        }else {
            Tool.warnOut(TAG, "热点创建成功");
        }
    }

    /**
     * 热点创建失败
     *
     * @param wifiConfiguration 热点的WiFi配置
     */
    @Override
    public void onWifiHotspotCreateFailed(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration != null) {
            Tool.warnOut(TAG, "热点创建失败 SSID = " + wifiConfiguration.SSID + "password = " + wifiConfiguration.preSharedKey);
        }else {
            Tool.warnOut(TAG, "热点创建失败");
        }
    }

    /**
     * WiFi热点正在关闭
     */
    @Override
    public void onWifiHotspotClosing() {
        Tool.warnOut(TAG, "热点正在关闭");
    }

    /**
     * WiFi热点关闭完成
     *
     * @param wifiConfiguration 热点的WiFi配置
     */
    @Override
    public void onWifiHotspotClosed(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration != null) {
            Tool.warnOut(TAG, "热点关闭完成 SSID = " + wifiConfiguration.SSID + "password = " + wifiConfiguration.preSharedKey);
        }else {
            Tool.warnOut(TAG, "热点关闭完成");
        }
    }

    /**
     * WiFi热点关闭失败
     *
     * @param wifiConfiguration 热点的WiFi配置
     */
    @Override
    public void onWifiHotspotCloseFailed(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration != null) {
            Tool.warnOut(TAG, "热点关闭失败 SSID = " + wifiConfiguration.SSID + "password = " + wifiConfiguration.preSharedKey);
        }else {
            Tool.warnOut(TAG, "热点关闭失败");
        }
    }
}
