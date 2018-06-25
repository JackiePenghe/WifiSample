package com.jackiepenghe.wifilibrary;

import java.util.ArrayList;

/**
 * @author jackie
 * 默认的WiFi热点回调
 */
public class DefaultWifiHotspotReceiveConnectedDeviceListener implements WifiHotspotController.WifiHotspotReceiveConnectedDeviceListener {

    private static final String TAG = DefaultWifiHotspotReceiveConnectedDeviceListener.class.getSimpleName();

    /**
     * WiFi热点被连接后，连接手机的设备的IP地址集合
     *
     * @param connectedIPs 连接手机的设备的IP地址集合
     */
    @Override
    public void onConnectedDevices(ArrayList<String> connectedIPs) {
        Tool.warnOut(TAG, "onConnectedDevices");
        for (int i = 0; i < connectedIPs.size(); i++) {
            String ip = connectedIPs.get(i);
            Tool.warnOut(TAG, "ip[" + i + "] = " + ip);
        }
    }

    /**
     * WiFi热点被连接，连接手机的新的设备的IP地址
     *
     * @param connectedIP 连接手机的新的设备的IP地址
     */
    @Override
    public void onDeviceConnected(String connectedIP) {
        Tool.warnOut(TAG, "onDeviceConnected ip = " + connectedIP);
    }

    /**
     * WiFi热点被断开连接，断开连接的设备的IP地址
     *
     * @param connectedIP 断开连接的设备的IP地址
     */
    @Override
    public void onDeviceDisConnected(String connectedIP) {
        Tool.warnOut(TAG, "onDeviceDisConnected ip = " + connectedIP);
    }
}
