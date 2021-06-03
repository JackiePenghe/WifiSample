package com.sscl.wifilibrary.intefaces.impl;

import com.sscl.wifilibrary.WifiManager;
import com.sscl.wifilibrary.intefaces.OnWifiHotspotIsConnectedByOthersListener;

import java.util.ArrayList;

/**
 * @author jackie
 * 默认的WiFi热点回调
 */
public final class DefaultOnWifiHotspotIsConnectedByOthersListener implements OnWifiHotspotIsConnectedByOthersListener {

    private static final String TAG = DefaultOnWifiHotspotIsConnectedByOthersListener.class.getSimpleName();

    /**
     * WiFi热点被连接后，连接手机的设备的IP地址集合
     *
     * @param connectedIPs 连接手机的设备的IP地址集合
     */
    @Override
    public void onConnectedDevices(ArrayList<String> connectedIPs) {
        WifiManager.warnOut(TAG, "onConnectedDevices");
        for (int i = 0; i < connectedIPs.size(); i++) {
            String ip = connectedIPs.get(i);
            WifiManager.warnOut(TAG, "ip[" + i + "] = " + ip);
        }
    }

    /**
     * WiFi热点被连接，连接手机的新的设备的IP地址
     *
     * @param connectedIP 连接手机的新的设备的IP地址
     */
    @Override
    public void onDeviceConnected(String connectedIP) {
        WifiManager.warnOut(TAG, "onDeviceConnected ip = " + connectedIP);
    }

    /**
     * WiFi热点被断开连接，断开连接的设备的IP地址
     *
     * @param disconnectedIP 断开连接的设备的IP地址
     */
    @Override
    public void onDeviceDisconnected(String disconnectedIP) {
        WifiManager.warnOut(TAG, "onDeviceDisconnected ip = " + disconnectedIP);
    }
}
