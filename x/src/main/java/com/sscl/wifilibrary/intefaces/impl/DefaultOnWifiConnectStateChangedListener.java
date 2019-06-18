package com.sscl.wifilibrary.intefaces.impl;


import com.sscl.wifilibrary.DebugUtil;
import com.sscl.wifilibrary.intefaces.OnWifiConnectStateChangedListener;

/**
 * 默认的连接回调
 *
 * @author jackie
 */
public class DefaultOnWifiConnectStateChangedListener implements OnWifiConnectStateChangedListener {

    private static final String TAG = DefaultOnWifiConnectStateChangedListener.class.getSimpleName();

    /**
     * 正在连接
     *
     * @param ssid WiFi的名称
     */
    @Override
    public void connecting(String ssid) {
        DebugUtil.warnOut(TAG, "connecting ssid = " + ssid);
    }

    /**
     * 已连接
     *
     * @param ssid WiFi的名称
     */
    @Override
    public void connected(String ssid) {
        DebugUtil.warnOut(TAG, "connected ssid = " + ssid);
    }

    /**
     * 已断开连接
     */
    @Override
    public void disconnected() {
        DebugUtil.warnOut(TAG, "disconnected");
    }

    /**
     * 正在进行身份授权
     *
     * @param ssid WiFi的名称
     */
    @Override
    public void authenticating(String ssid) {
        DebugUtil.warnOut(TAG, "authenticating ssid = " + ssid);
    }

    /**
     * 正在获取IP地址
     *
     * @param ssid WiFi的名称
     */
    @Override
    public void obtainingIpAddress(String ssid) {
        DebugUtil.warnOut(TAG, "obtainingIpAddress ssid = " + ssid);
    }

    /**
     * 连接失败
     *
     * @param ssid WiFi的名称
     */
    @Override
    public void connectFailed(String ssid) {
        DebugUtil.warnOut(TAG, "connectFailed ssid = " + ssid);
    }

    /**
     * 正在断开连接
     */
    @Override
    public void disconnecting() {
        DebugUtil.warnOut(TAG, "disconnecting");
    }

    /**
     * 未知状态
     */
    @Override
    public void unknownStatus() {
        DebugUtil.warnOut(TAG, "unknownStatus");
    }

    /**
     * 用户取消了连接动作
     *
     * @param ssid 准备连接的WiFi SSID
     */
    @Override
    public void cancelConnect(String ssid) {
        DebugUtil.warnOut(TAG, "cancelConnect ssid = " + ssid);
    }

    /**
     * 连接超时
     */
    @Override
    public void connectTimeOut() {
        DebugUtil.warnOut(TAG, "connectTimeOut");
    }
}
