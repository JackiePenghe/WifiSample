package com.sscl.wifilibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sscl.wifilibrary.bean.WifiDevice;
import com.sscl.wifilibrary.enums.EncryptWay;
import com.sscl.wifilibrary.intefaces.OnP2pTransmitterStateChangedListener;
import com.sscl.wifilibrary.intefaces.OnWifiConnectStateChangedListener;
import com.sscl.wifilibrary.intefaces.OnWifiScanStateChangedListener;

import java.util.ArrayList;

/**
 * P2P发送器
 *
 * @author jackie
 */
public class P2pTransmitter {

    private static final String TAG = P2pTransmitter.class.getSimpleName();
    private WifiScanner wifiScanner;
    private WifiConnector wifiConnector;
    private OnP2pTransmitterStateChangedListener onP2pTransmitterStateChangedListener;
    private OnWifiScanStateChangedListener onWifiScanStateChangedListener = new OnWifiScanStateChangedListener() {
        @Override
        public void startScanFailed() {
            performConnectFailedListener();
        }

        @Override
        public void isScanning() {

        }

        @Override
        public void wifiScanResultObtained(@Nullable ArrayList<WifiDevice> wifiDevices) {
            if (wifiDevices == null) {
                performConnectFailedListener();
                return;
            }
            WifiDevice receiverDevice = getReceiverDevice(wifiDevices);
            if (receiverDevice == null) {
                performConnectFailedListener();
                return;
            }
            wifiConnector.connectNewWifi(WifiConstants.HOT_SPOTS_NAME, WifiConstants.HOT_SPOTS_PASSWORD, false, EncryptWay.WPA_WPA2_ENCRYPT, false);
        }
    };
    private OnWifiConnectStateChangedListener onWifiConnectStateChangedListener = new OnWifiConnectStateChangedListener() {
        @Override
        public void connecting(String ssid) {
            DebugUtil.warnOut(TAG, "connecting ssid = " + ssid);
//                performConnectingListener();
        }

        @Override
        public void connected(String ssid) {
            if (ssid.equals(WifiConstants.HOT_SPOTS_NAME)) {
                performConnectedListener();
            }
        }

        @Override
        public void disconnected() {
            performDisconnectedListener();
        }

        @Override
        public void authenticating(String ssid) {
            if (ssid.equals(WifiConstants.HOT_SPOTS_NAME)) {
                performAuthenticatingListener();
            }
        }

        @Override
        public void obtainingIpAddress(String ssid) {
            if (ssid.equals(WifiConstants.HOT_SPOTS_NAME)) {
                performObtainingIpAddressListener();
            }
        }

        @Override
        public void connectFailed(String ssid) {
            if (ssid.equals(WifiConstants.HOT_SPOTS_NAME)) {
                performConnectFailedListener();
            }
        }

        @Override
        public void disconnecting() {
            performDisconnectingListener();
        }

        @Override
        public void unknownStatus() {
            performUnknownStateListener();
        }

        @Override
        public void cancelConnect(String ssid) {
            if (ssid.equals(WifiConstants.HOT_SPOTS_NAME)) {
                performCancelConnectListener();
            }
        }

        @Override
        public void connectTimeOut() {
            performConnectTimeListener();
        }
    };

    @SuppressWarnings("WeakerAccess")
    public P2pTransmitter() {
        this.wifiConnector = WifiManager.newWifiConnectorInstance();
        wifiConnector.addOnWifiConnectStateChangedListener(onWifiConnectStateChangedListener);
        wifiScanner = WifiManager.newWifiScannerInstance();
        wifiScanner.setOnWifiScanStateChangedListener(onWifiScanStateChangedListener);
    }

    public void close() {
        wifiConnector.close();
    }

    public void connect() {
        wifiScanner.startScan();
    }

    public void setOnP2pTransmitterStateChangedListener(OnP2pTransmitterStateChangedListener onP2pTransmitterStateChangedListener) {
        this.onP2pTransmitterStateChangedListener = onP2pTransmitterStateChangedListener;
    }

    private void performConnectingListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2pTransmitterStateChangedListener != null) {
                    onP2pTransmitterStateChangedListener.connecting();
                }
            }
        });
    }

    private void performConnectedListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2pTransmitterStateChangedListener != null) {
                    onP2pTransmitterStateChangedListener.connected();
                }
            }
        });
    }

    private void performDisconnectedListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2pTransmitterStateChangedListener != null) {
                    onP2pTransmitterStateChangedListener.disconnected();
                }
            }
        });
    }

    private void performAuthenticatingListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2pTransmitterStateChangedListener != null) {
                    onP2pTransmitterStateChangedListener.authenticating();
                }
            }
        });
    }

    private void performObtainingIpAddressListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2pTransmitterStateChangedListener != null) {
                    onP2pTransmitterStateChangedListener.obtainingIpAddress();
                }
            }
        });
    }

    private void performConnectFailedListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2pTransmitterStateChangedListener != null) {
                    onP2pTransmitterStateChangedListener.connectFailed();
                }
            }
        });
    }

    private void performDisconnectingListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2pTransmitterStateChangedListener != null) {
                    onP2pTransmitterStateChangedListener.disconnecting();
                }
            }
        });
    }

    private void performUnknownStateListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2pTransmitterStateChangedListener != null) {
                    onP2pTransmitterStateChangedListener.unknownState();
                }
            }
        });
    }

    private void performCancelConnectListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2pTransmitterStateChangedListener != null) {
                    onP2pTransmitterStateChangedListener.cancelConnect();
                }
            }
        });
    }

    private void performConnectTimeListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onP2pTransmitterStateChangedListener != null) {
                    onP2pTransmitterStateChangedListener.connectTime();
                }
            }
        });
    }


    private WifiDevice getReceiverDevice(@NonNull ArrayList<WifiDevice> wifiDevices) {
        for (int i = 0; i < wifiDevices.size(); i++) {
            WifiDevice wifiDevice = wifiDevices.get(i);
            if (WifiManager.isWifiSsidEquals(wifiDevice.getSSID(), WifiConstants.HOT_SPOTS_NAME)) {
                return wifiDevice;
            }
        }
        return null;
    }
}
