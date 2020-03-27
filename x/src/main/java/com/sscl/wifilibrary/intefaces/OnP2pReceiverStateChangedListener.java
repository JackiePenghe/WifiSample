package com.sscl.wifilibrary.intefaces;

import android.net.wifi.WifiConfiguration;

import com.sscl.wifilibrary.bean.ConnectedDevice;

import java.util.ArrayList;

/**
 * P2P接收器
 *
 * @author jackie
 */
@SuppressWarnings("deprecation")
public interface OnP2pReceiverStateChangedListener {

    /**
     * 正在创建热点
     *
     * @param wifiConfiguration WiFi配置信息
     */
    void onHotspotCreating(WifiConfiguration wifiConfiguration);

    /**
     * 热点创建成功
     *
     * @param wifiConfiguration WiFi配置信息
     */
    void onHotspotCreated(WifiConfiguration wifiConfiguration);

    /**
     * 热点创建失败
     *
     * @param wifiConfiguration WiFi配置信息
     */
    void onHotspotCreateFailed(WifiConfiguration wifiConfiguration);

    /**
     * WiFi热点正在关闭
     */
    void onHotspotClosing();

    /**
     * WiFi热点已经关闭
     *
     * @param wifiConfiguration WiFi配置信息
     */
    void onHotspotClosed(WifiConfiguration wifiConfiguration);

    /**
     * WiFi热点关闭失败
     *
     * @param wifiConfiguration WiFi配置信息
     */
    void onHotspotCloseFailed(WifiConfiguration wifiConfiguration);

    /**
     * 当前已经连接的设备的IP地址集合
     *
     * @param connectedDevices 已经连接的设备的IP地址集合
     */
    void onConnectedDevices(ArrayList<ConnectedDevice> connectedDevices);

    /**
     * 有一个新设备连接到热点
     *
     * @param connectedDevice 设备连入的IP地址
     */
    void onDeviceConnected(ConnectedDevice connectedDevice);

    /**
     * 某个已经连接的设备断开连接时执行的回调
     *
     * @param connectedDevice 断开连接时，设备的IP地址
     */
    void onDeviceDisconnected(ConnectedDevice connectedDevice);
}
