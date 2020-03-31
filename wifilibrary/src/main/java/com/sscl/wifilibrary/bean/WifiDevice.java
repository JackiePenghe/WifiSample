package com.sscl.wifilibrary.bean;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import com.sscl.wifilibrary.WifiManager;
import com.sscl.wifilibrary.enums.EncryptWay;
import com.sscl.wifilibrary.x.R;


/**
 * WiFi设备bean类
 *
 * @author jackie
 */
public class WifiDevice implements Parcelable {

    /*---------------------------静态常量---------------------------*/

    public static final Creator<WifiDevice> CREATOR = new Creator<WifiDevice>() {
        @Override
        public WifiDevice createFromParcel(Parcel source) {
            return new WifiDevice(source);
        }

        @Override
        public WifiDevice[] newArray(int size) {
            return new WifiDevice[size];
        }
    };
    private static final int LEVEL_1 = -50;
    private static final int LEVEL_2 = -60;
    private static final int LEVEL_3 = -70;

    /*---------------------------成员变量---------------------------*/
    private static final int LEVEL_4 = -80;
    /**
     * WiFi扫描结果
     */
    private ScanResult scanResult;

    /*---------------------------构造方法---------------------------*/
    /**
     * 是否是隐藏WiFi
     */
    private boolean hidden;

    /**
     * 构造方法
     *
     * @param scanResult WiFi设备扫描结果
     */
    public WifiDevice(ScanResult scanResult) {
        this(scanResult, false);
    }

    /*---------------------------getter---------------------------*/

    /**
     * 构造方法
     *
     * @param scanResult WiFi设备扫描结果
     * @param hidden     是否是隐藏WiFi
     */
    public WifiDevice(ScanResult scanResult, boolean hidden) {
        this.scanResult = scanResult;
        this.hidden = hidden;
    }

    /*---------------------------公开方法---------------------------*/

    @SuppressWarnings("WeakerAccess")
    protected WifiDevice(Parcel in) {
        this.scanResult = in.readParcelable(ScanResult.class.getClassLoader());
        this.hidden = in.readByte() != 0;
        int tmpScanType = in.readInt();
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public String getSSID() {
        return scanResult.SSID;
    }

    @SuppressWarnings("unused")
    public String getBSSID() {
        return scanResult.BSSID;
    }

    public String getFormatSSID() {
        return "\"" + scanResult.SSID + "\"";
    }

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

    public EncryptWay getEncryptWay() {
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
}
