package com.jackiepenghe.wifilibrary;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;

/**
 * WiFi设备bean类
 *
 * @author jackie
 */
public class WifiDevice implements Parcelable {


    /*---------------------------静态常量---------------------------*/

    private static final int LEVEL_1 = -50;
    private static final int LEVEL_2 = -60;
    private static final int LEVEL_3 = -70;
    private static final int LEVEL_4 = -80;

    /*---------------------------成员变量---------------------------*/

    /**
     * WiFi扫描结果
     */
    private ScanResult scanResult;

    /**
     * 是否是隐藏WiFi
     */
    private boolean hidden;

    /*---------------------------构造方法---------------------------*/

    /**
     * 构造方法
     *
     * @param scanResult WiFi设备扫描结果
     */
    WifiDevice( ScanResult scanResult) {
      this(scanResult,false);
    }

    /**
     * 构造方法
     * @param scanResult WiFi设备扫描结果
     * @param hidden 是否是隐藏WiFi
     */
    @SuppressWarnings("WeakerAccess")
    public WifiDevice(ScanResult scanResult, boolean hidden) {
        this.scanResult = scanResult;
        this.hidden = hidden;
    }

    /*---------------------------枚举定义---------------------------*/

    /**
     * 加密方式的枚举
     */
    public enum EncryptWay {
        /**
         * 未加密
         */
        NO_ENCRYPT(1),
        /**
         * WPA/WPA2加密
         */
        WPA_WPA2_ENCRYPT(2),
        /**
         * WEP加密
         */
        WEP_ENCRYPT(3),
        /**
         * 仅WPA加密
         */
        WPA_ENCRYPT(4),
        /**
         * PSK加密
         */
        EAP_ENCRYPT(5),
        /**
         * 仅WPA2加密
         */
        WPA2_ENCRYPT(6),
        /**
         * 未知加密方式
         */
        UNKNOWN__ENCRYPT(7);

        int encryptWay;

        EncryptWay(int encryptWay) {
            this.encryptWay = encryptWay;
        }

        /**
         * 获取当前的枚举的值
         *
         * @return 当前的枚举的值
         */
        @SuppressWarnings("unused")
        public int getEncryptWayValue() {
            return encryptWay;
        }

        /**
         * 通过枚举的值来获取枚举对象
         *
         * @param encryptWayValue 枚举的值
         * @return 枚举对象
         */
        @SuppressWarnings("unused")
        public static EncryptWay getEncryptWayByValue(int encryptWayValue) {
            switch (encryptWayValue) {
                case 1:
                    return EncryptWay.NO_ENCRYPT;
                case 2:
                    return EncryptWay.WPA_WPA2_ENCRYPT;
                case 3:
                    return EncryptWay.WEP_ENCRYPT;
                default:
                    return EncryptWay.UNKNOWN__ENCRYPT;
            }
        }
    }

    /*---------------------------getter---------------------------*/

    public ScanResult getScanResult() {
        return scanResult;
    }

    /*---------------------------公开方法---------------------------*/

    public String getSSID() {
        return scanResult.SSID;
    }

    @SuppressWarnings("unused")
    public String getBSSID() {
        return scanResult.BSSID;
    }

    public String getFormatSSID(){
        return "\"" +  scanResult.SSID + "\"";
    }

    @SuppressWarnings({"WeakerAccess", "BooleanMethodIsAlwaysInverted"})
    public boolean isHidden() {
        return hidden;
    }

    public String getEncryptionWayString() {
        return WifiManager.getEncryptionWayString(scanResult);
    }

    @SuppressWarnings("unused")
    public String getCapabilities() {
        return scanResult.capabilities;
    }

    @SuppressWarnings("unused")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @TargetApi(Build.VERSION_CODES.M)
    public int getCenterFreq0() {
        return scanResult.centerFreq0;
    }

    @SuppressWarnings("unused")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @TargetApi(Build.VERSION_CODES.M)
    public int getCenterFreq1() {
        return scanResult.centerFreq1;
    }

    @SuppressWarnings("unused")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @TargetApi(Build.VERSION_CODES.M)
    public int getChannelWidth() {
        return scanResult.channelWidth;
    }

    @SuppressWarnings("unused")
    public int getFrequency() {
        return scanResult.frequency;
    }

    @SuppressWarnings("unused")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @TargetApi(Build.VERSION_CODES.M)
    public CharSequence getOperatorFriendlyName() {
        return scanResult.operatorFriendlyName;
    }

    @SuppressWarnings("unused")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @TargetApi(Build.VERSION_CODES.M)
    public long getTimestamp() {
        return scanResult.timestamp;
    }

    @SuppressWarnings("unused")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @TargetApi(Build.VERSION_CODES.M)
    public CharSequence getVenueName() {
        return scanResult.venueName;
    }


    public int getIntLevel() {
        return scanResult.level;
    }

    public String getStringLevel(Context context) {
        int level = scanResult.level;
        if (level >= LEVEL_1) {
            return context.getString(R.string.level_1);
        } else if (level >= LEVEL_2) {
            return context.getString(R.string.level_2);
        } else if (level >= LEVEL_3) {
            return context.getString(R.string.level_3);
        } else if (level >= LEVEL_4) {
            return context.getString(R.string.level_4);
        } else {
            return context.getString(R.string.level_5);
        }
    }

    public int getLevelDrawableResId() {
        int level = scanResult.level;
        if (level >= LEVEL_1) {
            return R.drawable.wifi_signal_4_full;
        } else if (level >= LEVEL_2) {
            return R.drawable.wifi_signal_3;
        } else if (level >= LEVEL_3) {
            return R.drawable.wifi_signal_2;
        } else if (level >= LEVEL_4) {
            return R.drawable.wifi_signal_1;
        } else {
            return R.drawable.wifi_signal_0;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public EncryptWay getEncryptWay(){
        return WifiManager.getEncryptionWay(scanResult);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.scanResult, flags);
        dest.writeByte(this.hidden ? (byte) 1 : (byte) 0);
    }

    @SuppressWarnings("WeakerAccess")
    protected WifiDevice(Parcel in) {
        this.scanResult = in.readParcelable(ScanResult.class.getClassLoader());
        this.hidden = in.readByte() != 0;
    }

    public static final Parcelable.Creator<WifiDevice> CREATOR = new Parcelable.Creator<WifiDevice>() {
        @Override
        public WifiDevice createFromParcel(Parcel source) {
            return new WifiDevice(source);
        }

        @Override
        public WifiDevice[] newArray(int size) {
            return new WifiDevice[size];
        }
    };
}
