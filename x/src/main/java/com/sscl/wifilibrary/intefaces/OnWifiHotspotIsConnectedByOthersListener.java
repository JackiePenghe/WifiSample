package com.sscl.wifilibrary.intefaces;

import java.util.ArrayList;

/**
 * WiFi热点被连接后的回调
 *
 * @author jackie
 */
public interface OnWifiHotspotIsConnectedByOthersListener {
    /**
     * WiFi热点被连接后，连接手机的设备的IP地址集合
     *
     * @param connectedIPs 连接手机的设备的IP地址集合
     */
    void onConnectedDevices(ArrayList<String> connectedIPs);

    /**
     * WiFi热点被连接，连接手机的新的设备的IP地址
     *
     * @param connectedIP 连接手机的新的设备的IP地址
     */
    void onDeviceConnected(String connectedIP);

    /**
     * WiFi热点被断开连接，断开连接的设备的IP地址
     *
     * @param disconnectedIP 断开连接的设备的IP地址
     */
    void onDeviceDisconnected(String disconnectedIP);
}