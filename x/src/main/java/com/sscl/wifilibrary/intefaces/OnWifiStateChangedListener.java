package com.sscl.wifilibrary.intefaces;

/**
 * WiFi状态改变时调用此接口
 *
 * @author jackie
 */
public interface OnWifiStateChangedListener {

    /**
     * WiFi已打开
     */
    void onWifiEnabled();

    /**
     * WiFi正在打开
     */
    void onWifiEnabling();

    /**
     * WiFi已关闭
     */
    void onWifiDisabled();

    /**
     * WiFi正在关闭
     */
    void onWifiDisabling();

    /**
     * 未知的WiFi状态
     *
     * @param wifiState WiFi状态
     */
    void unknownWifiState(int wifiState);

}