package com.jackiepenghe.wifisample;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.jackiepenghe.baselibrary.FileUtil;
import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.wifilibrary.WifiManager;

public class MyApplication extends Application {
    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();
        WifiManager.init(this);
        WifiManager.setDebugFlag(true);
        Tool.setDebugFlag(true);
        FileUtil.init(this);

    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }
}
