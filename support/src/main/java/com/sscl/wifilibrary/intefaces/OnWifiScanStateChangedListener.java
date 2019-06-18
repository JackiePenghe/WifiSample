package com.sscl.wifilibrary.intefaces;

import android.support.annotation.Nullable;

import com.sscl.wifilibrary.bean.WifiDevice;

import java.util.ArrayList;

/**
 * WiFi扫描的相关回调
 *
 * @author jackie
 */
public interface OnWifiScanStateChangedListener {
    /**
     * 扫描开启失败
     */
    void startScanFailed();

    /**
     * 扫描开启成功，正在扫描中
     */
    void isScanning();

    /**
     * 获取到WiFi扫描结果
     *
     * @param wifiDevices WiFi扫描结果
     */
    void wifiScanResultObtained(@Nullable ArrayList<WifiDevice> wifiDevices);
}