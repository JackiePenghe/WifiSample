package com.sscl.wifilibrary.intefaces.impl;


import androidx.annotation.Nullable;

import com.sscl.wifilibrary.WifiManager;
import com.sscl.wifilibrary.bean.WifiDevice;
import com.sscl.wifilibrary.intefaces.OnWifiScanStateChangedListener;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 默认的WiFi扫描回调
 *
 * @author jackie
 */
public class DefaultOnWifiScanStateChangedListener implements OnWifiScanStateChangedListener {

    private static final String TAG = DefaultOnWifiScanStateChangedListener.class.getSimpleName();

    /**
     * 扫描开启失败
     */
    @Override
    public void startScanFailed() {
        WifiManager.warnOut(TAG, "startScanFailed");
    }

    /**
     * 扫描开启成功，正在扫描中
     */
    @Override
    public void isScanning() {
        WifiManager.warnOut(TAG, "isScanning");
    }

    /**
     * 获取到WiFi扫描结果
     *
     * @param wifiDevices WiFi扫描结果
     */
    @Override
    public void wifiScanResultObtained(@Nullable ArrayList<WifiDevice> wifiDevices) {
        if (wifiDevices != null) {
            WifiDevice[] wifiDevicesArray = new WifiDevice[wifiDevices.size()];
            wifiDevices.toArray(wifiDevicesArray);
            WifiManager.warnOut(TAG, Arrays.toString(wifiDevicesArray));
        } else {
            WifiManager.warnOut(TAG, "wifiDevices == null");
        }
    }
}
