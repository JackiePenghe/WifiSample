package com.jackiepenghe.wifilibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 工具类
 *
 * @author alm
 */

class Tool {


    /**
     * showToastKeep
     */
    private static ScheduledExecutorService SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE;
    /**
     * hideToastKeep
     */
    private static ScheduledExecutorService HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE;

    //log部分

    /**
     * 是否打印日志信息的标志
     */
    private static boolean mDebug = false;
    /**
     *
     */
    private static Toast toast;

    /**
     * 获取当前日志打印标志
     *
     * @return 日志打印标志
     */
    public static boolean isDebug() {
        return mDebug;
    }

    /**
     * 设置日志打印标志
     *
     * @param debug 日志打印标志
     */
    static void setDebugFlag(@SuppressWarnings("SameParameterValue") boolean debug) {
        mDebug = debug;
    }

    /**
     * 等同于Log.e
     *
     * @param tag     tag
     * @param message 日志信息
     */
    @SuppressWarnings("WeakerAccess")
    public static void errorOut(String tag, String message) {
        if (!mDebug) {
            return;
        }
        Log.e(tag, message);
    }

    /**
     * 等同于Log.w
     *
     * @param tag     tag
     * @param message 日志信息
     */
    @SuppressWarnings("WeakerAccess")
    public static void warnOut(String tag, String message) {
        if (!mDebug) {
            return;
        }
        Log.w(tag, message);
    }

    //Toast部分

    /**
     * 弹出Toast
     *
     * @param context    上下文
     * @param messageRes 信息
     * @param duration   持续时间
     */
    private static void showToast(Context context, @StringRes int messageRes, int duration) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            CustomToast.makeText(context, messageRes, duration).show();
            return;
        }
        showMyToast(context, messageRes, duration);
    }

    /**
     * 弹出Toast
     *
     * @param context  上下文
     * @param message  信息
     * @param duration 持续时间
     */
    private static void showToast(Context context, String message, int duration) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            CustomToast.makeText(context, message, duration).show();
            return;
        }
        showMyToast(context, message, duration);
    }

    /**
     * 长时间的吐司
     *
     * @param context    上下文
     * @param messageRes 信息
     */
    public static void toastL(Context context, @StringRes int messageRes) {
        showToast(context, messageRes, CustomToast.LENGTH_LONG);
    }

    /**
     * 长时间的吐司
     *
     * @param context 上下文
     * @param message 信息
     */
    public static void toastL(Context context, String message) {
        showToast(context, message, CustomToast.LENGTH_LONG);
    }

    //数据转换部分

    /**
     * bytes转换成十六进制字符串
     *
     * @param bytes byte数组
     * @return String 每个Byte值之间空格分隔
     */
    @SuppressWarnings("WeakerAccess")
    public static String bytesToHexStr(byte[] bytes) {

        if (bytes == null) {
            return "";
        }

        String stmp;
        StringBuilder sb = new StringBuilder("");
        for (byte aByte : bytes) {
            stmp = Integer.toHexString(aByte & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     *
     * @param src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStrToBytes(String src) {
        int m, n;
        int l = src.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int integer = Integer.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
            ret[i] = (byte) integer;
        }
        return ret;
    }

    private static void showMyToast(Context context, @StringRes int messageRes, final int duration) {
        String message = context.getString(messageRes);
        showMyToast(context, message, duration);
    }

    @SuppressLint("ShowToast")
    private static void showMyToast(Context context, final String message, final int duration) {

        if (toast != null) {
            try {
                toast.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
            toast = null;
        }

        toast = Toast.makeText(context, message, Toast.LENGTH_LONG);

        if (SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE != null && !SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE.isShutdown()) {
            try {
                SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE.shutdownNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
            SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE = null;
        }
        if (HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE != null && !HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE.isShutdown()) {
            try {
                HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE.shutdownNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
            HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE = null;
        }
        if (SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE == null) {
            synchronized (Tool.class) {
                if (SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE == null) {
                    SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE = WifiManager.newScheduledExecutorService();
                }
            }
        }

        if (HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE == null) {
            synchronized (Tool.class) {
                if (HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE == null) {
                    HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE = WifiManager.newScheduledExecutorService();
                }
            }
        }
        SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                toast.setText(message);
                toast.show();
                Looper looper = Looper.myLooper();
                if (looper == null) {
                    return;
                }
                looper.quit();
            }
        }, 0, 3000, TimeUnit.MILLISECONDS);
        HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE.schedule(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                toast.cancel();
                try {
                    SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE.shutdownNow();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE = null;
                HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE = null;
                Looper looper = Looper.myLooper();
                if (looper == null) {
                    return;
                }
                looper.quit();
            }
        }, duration, TimeUnit.MILLISECONDS);
    }
}
