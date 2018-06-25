package com.jackiepenghe.wifilibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 自定义Toast，可实现自定义显示时间
 * Created by alm on 17-6-5.
 */

class CustomToast {

    /*------------------------静态常量----------------------------*/

    /**
     * 短时间
     */
    @SuppressWarnings("WeakerAccess")
    public static final int LENGTH_SHORT = 2000;

    /**
     * 长时间
     */
    @SuppressWarnings("WeakerAccess")
    public static final int LENGTH_LONG = 3000;

    /**
     * 总是显示，不消失
     */
    public static final int LENGTH_ALWAYS = 0;

    /*------------------------成员变量----------------------------*/

    /**
     * 系统吐司类
     */
    private Toast mToast;
    /**
     * 吐司持续时间
     */
    private static int mDuration;
    /**
     * 系统吐司的mTN对象（反射获取）
     */
    private Object mTN;
    /**
     * 系统吐司的show方法（反射获取）
     */
    private Method showMethod;
    /**
     * 系统吐司的hide方法（反射获取）
     */
    private Method hideMethod;
    /**
     * Handler
     */
    private Handler mHandler = new Handler();
    /**
     * 隐藏吐司
     */
    private Runnable mHide = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /*------------------------构造函数----------------------------*/

    /**
     * 构造函数
     * @param toast 系统吐司
     */
    private CustomToast(Toast toast) {
        mToast = toast;

    }

    /*------------------------公开静态函数----------------------------*/

    /**
     * 生成一个自定义吐司本类
     * @param context 上下文
     * @param message 吐司信息文本
     * @param duration 吐司持续时间
     * @return 自定义吐司本类
     */
    static CustomToast makeText(Context context, String message, int duration) {
        @SuppressLint("ShowToast") Toast toast = Toast.makeText(context, message, duration);
        mDuration = duration;
        return new CustomToast(toast);
    }

    /**
     * 生成一个自定义吐司本类
     * @param context 上下文
     * @param messageRes 吐司信息资源id
     * @param duration 吐司持续时间
     * @return 自定义吐司本类
     */
    static CustomToast makeText(Context context, @StringRes int messageRes, int duration) {
        @SuppressLint("ShowToast") Toast toast = Toast.makeText(context, messageRes, duration);
        mDuration = duration;
        return new CustomToast(toast);
    }

    /*------------------------私有函数----------------------------*/

    private void initTN() {
        try {
            Field tnField= mToast.getClass().getDeclaredField("mTN");
            tnField.setAccessible(true);
            mTN = tnField.get(mToast);

            showMethod = mTN.getClass().getDeclaredMethod("show");
            hideMethod = mTN.getClass().getDeclaredMethod("hide");

            Field tnTextViewField = mTN.getClass().getDeclaredField("mNextView");
            tnTextViewField.setAccessible(true);
            tnTextViewField.set(mTN,mToast.getView());

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏吐司
     */
    private void hide() {
        try {
            hideMethod.invoke(mTN);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示吐司
     */
    void show(){
        initTN();
        try {
            showMethod.invoke(mTN);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if (mDuration > 0){
            mHandler.postDelayed(mHide,mDuration);
        }else if(mDuration < 0){
            mHandler.postDelayed(mHide,LENGTH_LONG);
        }
    }
}
