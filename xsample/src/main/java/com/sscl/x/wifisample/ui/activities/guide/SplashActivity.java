package com.sscl.x.wifisample.ui.activities.guide;

import android.content.Intent;

import com.sscl.baselibrary.activity.BaseSplashActivity;


/**
 * @author jackie
 */
public class SplashActivity extends BaseSplashActivity {
    /**
     * 在本界面第一次启动时执行的操作
     */
    @Override
    protected void onCreate() {
        Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }
}
