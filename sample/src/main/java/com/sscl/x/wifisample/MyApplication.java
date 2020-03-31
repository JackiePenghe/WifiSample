package com.sscl.x.wifisample;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.sscl.baselibrary.files.FileUtil;
import com.sscl.baselibrary.utils.DebugUtil;
import com.sscl.baselibrary.utils.ToastUtil;
import com.sscl.wifilibrary.WifiManager;

/**
 * @author jackie
 */
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
        DebugUtil.setDebugFlag(true);
        FileUtil.init(this);
        ToastUtil.setToastReuse(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }
}
