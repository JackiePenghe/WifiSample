package com.sscl.wifilibrary.enums;

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
    UNKNOWN_ENCRYPT(7);

    int encryptWay;

    EncryptWay(int encryptWay) {
        this.encryptWay = encryptWay;
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
                return EncryptWay.UNKNOWN_ENCRYPT;
        }
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
}