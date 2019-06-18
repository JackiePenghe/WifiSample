package com.sscl.wifilibrary.bean;


import androidx.annotation.NonNull;

/**
 * 已经连入的设备
 *
 * @author jackie
 */
public class ConnectedDevice {
    private String name;
    private String ip;

    public ConnectedDevice(@NonNull String ip) {
        this("", ip);
    }

    @SuppressWarnings("WeakerAccess")
    public ConnectedDevice(@NonNull String name, @NonNull String ip) {
        this.name = name;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
    public String getIp() {
        return ip;
    }

    public void setIp(@NonNull String ip) {
        this.ip = ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConnectedDevice)) {
            return false;
        }

        ConnectedDevice that = (ConnectedDevice) o;

        return getIp().equals(that.getIp());
    }

    @Override
    public int hashCode() {
        return getIp().hashCode();
    }
}
