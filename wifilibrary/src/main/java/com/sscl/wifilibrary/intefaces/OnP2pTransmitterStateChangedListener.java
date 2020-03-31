package com.sscl.wifilibrary.intefaces;

/**
 * P2P传输状态改变时的回调
 *
 * @author jackie
 */
public interface OnP2pTransmitterStateChangedListener {

    /**
     * 正在连接
     */
    void connecting();

    /**
     * 已连接
     */
    void connected();

    /**
     * 已断开连接
     */
    void disconnected();

    /**
     * 正在验证身份
     */
    void authenticating();

    /**
     * 正在获取IP地址
     */
    void obtainingIpAddress();

    /**
     * 连接失败
     */
    void connectFailed();

    /**
     * 正在断开连接
     */
    void disconnecting();

    /**
     * 未知状态
     */
    void unknownState();

    /**
     * 取消连接
     */
    void cancelConnect();

    /**
     * 连接超时
     */
    void connectTime();

    /**
     * 未搜索到可连接的传输WiFi
     */
    void searchedNothing();
}
