package com.sscl.wifilibrary;

import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.support.annotation.Nullable;

import com.sscl.wifilibrary.bean.WifiDevice;
import com.sscl.wifilibrary.intefaces.OnWifiScanStateChangedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Wifi scanner
 *
 * @author jackie
 */
public class WifiScanner {

    private WifiScanDataAndStatusBroadcastReceiver wifiScanDataAndStatusBroadcastReceiver = new WifiScanDataAndStatusBroadcastReceiver(WifiScanner.this);

    private OnWifiScanStateChangedListener onWifiScanStateChangedListener;

    private boolean scanning;

    WifiScanner() {
        WifiManager.getContext().registerReceiver(wifiScanDataAndStatusBroadcastReceiver, getIntentFilter());
    }

    @Nullable
    public List<ScanResult> getScanResults() {
        if (!WifiManager.hasLocationEnablePermission(WifiManager.getContext())) {
            return null;
        }
        return WifiManager.getSystemWifiManager().getScanResults();
    }

    public boolean startScan() {
        if (!WifiManager.hasLocationEnablePermission(WifiManager.getContext())) {
            return false;
        }
        if (!WifiManager.isWifiEnabled()) {
            if (onWifiScanStateChangedListener != null) {
                onWifiScanStateChangedListener.startScanFailed();
            }
            WifiManager.enableWifi(true);
            return false;
        }
        scanning = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            onWifiScanStateChangedListener.isScanning();
            List<ScanResult> scanResults = WifiManager.getSystemWifiManager().getScanResults();
            if (scanResults != null && scanResults.size() > 0) {
                final ArrayList<WifiDevice> wifiDevices = new ArrayList<>();
                for (int i = 0; i < scanResults.size(); i++) {
                    ScanResult scanResult = scanResults.get(i);
                    WifiDevice wifiDevice = new WifiDevice(scanResult);
                    wifiDevices.add(wifiDevice);
                }
                if (scanning) {
                    WifiManager.getHANDLER().post(new Runnable() {
                        @Override
                        public void run() {

                            if (onWifiScanStateChangedListener != null) {
                                onWifiScanStateChangedListener.wifiScanResultObtained(wifiDevices);
                            }
                        }
                    });
                    scanning = false;
                }
            } else {
                if (scanning) {
                    WifiManager.getHANDLER().post(new Runnable() {
                        @Override
                        public void run() {

                            if (onWifiScanStateChangedListener != null) {
                                onWifiScanStateChangedListener.wifiScanResultObtained(null);
                            }
                        }
                    });
                    scanning = false;
                }
            }
            return true;
        } else {
            boolean result = WifiManager.getSystemWifiManager().startScan();
            if (!result) {
                WifiManager.getHANDLER().post(new Runnable() {
                    @Override
                    public void run() {
                        if (onWifiScanStateChangedListener != null) {
                            onWifiScanStateChangedListener.startScanFailed();
                        }
                    }
                });
            } else {
                List<ScanResult> scanResults = WifiManager.getSystemWifiManager().getScanResults();
                if (scanResults != null && scanResults.size() > 0) {
                    final ArrayList<WifiDevice> wifiDevices = new ArrayList<>();
                    for (int i = 0; i < scanResults.size(); i++) {
                        ScanResult scanResult = scanResults.get(i);
                        WifiDevice wifiDevice = new WifiDevice(scanResult);
                        wifiDevices.add(wifiDevice);
                    }
                    if (scanning) {
                        WifiManager.getHANDLER().post(new Runnable() {
                            @Override
                            public void run() {

                                if (onWifiScanStateChangedListener != null) {
                                    onWifiScanStateChangedListener.wifiScanResultObtained(wifiDevices);
                                }
                            }
                        });
                        scanning = false;
                    }
                } else {
                    WifiManager.getHANDLER().post(new Runnable() {
                        @Override
                        public void run() {
                            if (onWifiScanStateChangedListener != null) {
                                onWifiScanStateChangedListener.isScanning();
                            }
                        }
                    });
                }
            }
            return result;
        }
    }

    public void close() {
        WifiManager.getContext().unregisterReceiver(wifiScanDataAndStatusBroadcastReceiver);
    }

    public void setOnWifiScanStateChangedListener(OnWifiScanStateChangedListener onWifiScanStateChangedListener) {
        this.onWifiScanStateChangedListener = onWifiScanStateChangedListener;
        wifiScanDataAndStatusBroadcastReceiver.setOnWifiScanStateChangedListener(onWifiScanStateChangedListener);
    }

    private IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        return intentFilter;
    }

    boolean isScanning() {
        return scanning;
    }
}
