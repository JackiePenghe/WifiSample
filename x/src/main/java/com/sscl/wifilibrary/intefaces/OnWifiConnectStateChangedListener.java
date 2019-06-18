package com.sscl.wifilibrary.intefaces;

/**
 * WiFi连接时的相关回调
 *
 * @author jackie
 */
public interface OnWifiConnectStateChangedListener {
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

    /**
     * 连接超时
     */
    void connectTimeOut();
}