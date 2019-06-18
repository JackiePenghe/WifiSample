package com.sscl.wifilibrary;

import android.net.wifi.WifiConfiguration;

import com.sscl.wifilibrary.intefaces.OnP2pReceiverStateChangedListener;
import com.sscl.wifilibrary.intefaces.OnWifiHotspotIsConnectedByOthersListener;
import com.sscl.wifilibrary.intefaces.OnWifiHotspotStateChangedListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * P2P接收器
 *
 * @author jackie
 */
public class P2pReceiver {
    private static final byte[] REQUEST_NAME_BYTES = "REQUEST_DEVICE_NAME".getBytes();
    private static final int PORT = 5200;
    private HashMap<String, String> ipNameHashMap = new HashMap<>();
    private WifiHotspotController wifiHotspotController;
    private OnP2pReceiverStateChangedListener onP2PReceiverStateChangedListener;
    private OnWifiHotspotStateChangedListener defaultOnWifiHotspotStateChangedListener = new OnWifiHotspotStateChangedListener() {
        /**
         * 热点正在创建
         *
         * @param wifiConfiguration WiFi的配置
         */
        @Override
        public void onWifiHotspotCreating(WifiConfiguration wifiConfiguration) {
            performP2pReceiverStateChangedHotspotCreatingListener(wifiConfiguration);
        }

        /**
         * 热点创建完成
         *
         * @param wifiConfiguration WiFi的配置
         */
        @Override
        public void onWifiHotspotCreated(WifiConfiguration wifiConfiguration) {
            performP2pReceiverStateChangedHotspotCreatedListener(wifiConfiguration);
        }

        /**
         * 热点创建失败
         *
         * @param wifiConfiguration WiFi的配置
         */
        @Override
        public void onWifiHotspotCreateFailed(WifiConfiguration wifiConfiguration) {
            performP2pReceiverStateChangedHotspotCreateFailedListener(wifiConfiguration);
        }

        /**
         * WiFi热点正在关闭
         */
        @Override
        public void onWifiHotspotClosing() {
            performP2pReceiverStateChangedHotspotClosingListener();
        }

        /**
         * WiFi热点关闭完成
         *
         * @param wifiConfiguration WiFi的配置
         */
        @Override
        public void onWifiHotspotClosed(WifiConfiguration wifiConfiguration) {
            performP2pReceiverStateChangedHotspotClosedListener(wifiConfiguration);
        }

        /**
         * WiFi热点关闭失败
         *
         * @param wifiConfiguration WiFi的配置
         */
        @Override
        public void onWifiHotspotCloseFailed(WifiConfiguration wifiConfiguration) {
            performP2pReceiverStateChangedHotspotCloseFailedListener(wifiConfiguration);
        }
    };
    private OnWifiHotspotIsConnectedByOthersListener onWifiHotspotIsConnectedByOthersListener = new OnWifiHotspotIsConnectedByOthersListener() {
        @Override
        public void onConnectedDevices(ArrayList<String> connectedIPs) {
            performP2pReceiverStateChangedHotspotConnectedDevicesListener(connectedIPs);
        }

        @Override
        public void onDeviceConnected(String connectedIP) {
//            performP2pReceiverStateChangedHotspotDeviceConnectedListener(connectedIP);
            wifiHotspotController.sendData(connectedIP, REQUEST_NAME_BYTES);
        }

        @Override
        public void onDeviceDisconnected(String disconnectedIP) {
            performP2pReceiverStateChangedHotspotDeviceDisconnectedListener(disconnectedIP);
        }
    };

    P2pReceiver() {
        this.wifiHotspotController = WifiManager.newWifiHotspotController();
        wifiHotspotController.init(WifiConstants.HOT_SPOTS_NAME, WifiConstants.HOT_SPOTS_PASSWORD, WifiConfiguration.KeyMgmt.WPA_PSK, defaultOnWifiHotspotStateChangedListener, onWifiHotspotIsConnectedByOthersListener);
    }

    public void setOnP2PReceiverStateChangedListener(OnP2pReceiverStateChangedListener onP2PReceiverStateChangedListener) {
        this.onP2PReceiverStateChangedListener = onP2PReceiverStateChangedListener;
    }

    public void startReceiverListener() {
        wifiHotspotController.setPort(PORT);
        wifiHotspotController.createHotspot();
    }

    public void close() {
        wifiHotspotController.close();
        wifiHotspotController = null;
        onP2PReceiverStateChangedListener = null;
        onWifiHotspotIsConnectedByOthersListener = null;
        defaultOnWifiHotspotStateChangedListener = null;
        ipNameHashMap = null;
    }

    private void performP2pReceiverStateChangedHotspotCreatingListener(final WifiConfiguration wifiConfiguration) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2PReceiverStateChangedListener != null) {
                    onP2PReceiverStateChangedListener.onHotspotCreating(wifiConfiguration);
                }
            }
        });
    }

    private void performP2pReceiverStateChangedHotspotCreatedListener(final WifiConfiguration wifiConfiguration) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2PReceiverStateChangedListener != null) {
                    onP2PReceiverStateChangedListener.onHotspotCreated(wifiConfiguration);
                }
            }
        });
    }

    private void performP2pReceiverStateChangedHotspotCreateFailedListener(final WifiConfiguration wifiConfiguration) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2PReceiverStateChangedListener != null) {
                    onP2PReceiverStateChangedListener.onHotspotCreateFailed(wifiConfiguration);
                }
            }
        });
    }

    private void performP2pReceiverStateChangedHotspotClosingListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2PReceiverStateChangedListener != null) {
                    onP2PReceiverStateChangedListener.onHotspotClosing();
                }
            }
        });
    }

    private void performP2pReceiverStateChangedHotspotClosedListener(final WifiConfiguration wifiConfiguration) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2PReceiverStateChangedListener != null) {
                    onP2PReceiverStateChangedListener.onHotspotClosed(wifiConfiguration);
                }
            }
        });
    }

    private void performP2pReceiverStateChangedHotspotCloseFailedListener(final WifiConfiguration wifiConfiguration) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2PReceiverStateChangedListener != null) {
                    onP2PReceiverStateChangedListener.onHotspotCloseFailed(wifiConfiguration);
                }
            }
        });
    }

    private void performP2pReceiverStateChangedHotspotConnectedDevicesListener(final ArrayList<String> connectedIPs) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                //noinspection StatementWithEmptyBody
                if (onP2PReceiverStateChangedListener != null) {
//                    onP2PReceiverStateChangedListener.onConnectedDevices(connectedIPs);
                }
            }
        });
    }

    private void performP2pReceiverStateChangedHotspotDeviceConnectedListener(final String connectedIP) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                //noinspection StatementWithEmptyBody
                if (onP2PReceiverStateChangedListener != null) {
//                    onP2PReceiverStateChangedListener.onDeviceConnected(connectedIP);
                }
            }
        });
    }

    private void performP2pReceiverStateChangedHotspotDeviceDisconnectedListener(final String disconnectedIP) {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                //noinspection StatementWithEmptyBody
                if (onP2PReceiverStateChangedListener != null) {
//                    onP2PReceiverStateChangedListener.onDeviceDisconnected(disconnectedIP);
                }
            }
        });
    }
}
