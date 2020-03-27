package com.sscl.wifilibrary.intefaces;

import android.net.wifi.WifiConfiguration;

/**
 * WiFi热点创建的相关回调
 *
 * @author jackie
 */
@SuppressWarnings("deprecation")
public interface OnWifiHotspotStateChangedListener {
    /**
     * 热点正在创建
     *
     * @param wifiConfiguration WiFi的配置
     */
    void onWifiHotspotCreating(WifiConfiguration wifiConfiguration);

    /**
     * 热点创建完成
     *
     * @param wifiConfiguration WiFi的配置
     */
    void onWifiHotspotCreated(WifiConfiguration wifiConfiguration);

    /**
     * 热点创建失败
     *
     * @param wifiConfiguration WiFi的配置
     */
    void onWifiHotspotCreateFailed(WifiConfiguration wifiConfiguration);

    /**
     * WiFi热点正在关闭
     */
    void onWifiHotspotClosing();

    /**
     * WiFi热点关闭完成
     *
     * @param wifiConfiguration WiFi的配置
     */
    void onWifiHotspotClosed(WifiConfiguration wifiConfiguration);

    /**
     * WiFi热点关闭失败
     *
     * @param wifiConfiguration WiFi的配置
     */
    void onWifiHotspotCloseFailed(WifiConfiguration wifiConfiguration);
}