package com.jackiepenghe.wifilibrary;

/**
 * 默认的WiFi扫描回调
 * @author jackie
 */
public class DefaultWifiScanCallback implements WifiOperatingTools.WifiScanCallback {

    private static final String TAG = DefaultWifiScanCallback.class.getSimpleName();

    /**
     * 扫描开启失败
     */
    @Override
    public void startScanFailed() {
        Tool.warnOut(TAG,"startScanFailed");
    }

    /**
     * 扫描开启成功，正在扫描中
     */
    @Override
    public void isScanning() {
        Tool.warnOut(TAG,"isScanning");
    }
}
