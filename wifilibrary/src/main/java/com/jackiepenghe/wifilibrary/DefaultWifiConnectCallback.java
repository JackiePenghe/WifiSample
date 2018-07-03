package com.jackiepenghe.wifilibrary;

/**
 * 默认的连接回调
 *
 * @author jackie
 */
public class DefaultWifiConnectCallback implements WifiOperatingTools.WifiConnectCallback {

    private static final String TAG = DefaultWifiConnectCallback.class.getSimpleName();

    /**
     * 正在连接
     *
     * @param ssid WiFi的名称
     */
    @Override
    public void connecting(String ssid) {
        Tool.warnOut(TAG,"connecting ssid = " + ssid);
    }

    /**
     * 已连接
     *
     * @param ssid WiFi的名称
     */
    @Override
    public void connected(String ssid) {
        Tool.warnOut(TAG,"connected ssid = " + ssid);
    }

    /**
     * 已断开连接
     */
    @Override
    public void disconnected() {
        Tool.warnOut(TAG,"disconnected");
    }

    /**
     * 正在进行身份授权
     *
     * @param ssid WiFi的名称
     */
    @Override
    public void authenticating(String ssid) {
        Tool.warnOut(TAG,"authenticating ssid = " + ssid);
    }

    /**
     * 正在获取IP地址
     *
     * @param ssid WiFi的名称
     */
    @Override
    public void obtainingIpAddress(String ssid) {
        Tool.warnOut(TAG,"obtainingIpAddress ssid = " + ssid);
    }

    /**
     * 连接失败
     *
     * @param ssid WiFi的名称
     */
    @Override
    public void connectFailed(String ssid) {
        Tool.warnOut(TAG,"connectFailed ssid = " + ssid);
    }

    /**
     * 正在断开连接
     */
    @Override
    public void disconnecting() {
        Tool.warnOut(TAG,"disconnecting");
    }

    /**
     * 未知状态
     */
    @Override
    public void unknownStatus() {
        Tool.warnOut(TAG,"unknownStatus");
    }

    /**
     * 用户取消了连接动作
     *
     * @param ssid 准备连接的WiFi SSID
     */
    @Override
    public void cancelConnect(String ssid) {
        Tool.warnOut(TAG,"cancelConnect ssid = " + ssid);
    }
}
