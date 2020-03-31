package com.sscl.wifilibrary;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.sscl.wifilibrary.intefaces.OnWifiHotspotIsConnectedByOthersListener;
import com.sscl.wifilibrary.intefaces.OnWifiHotspotStateChangedListener;
import com.sscl.wifilibrary.intefaces.OnWifiStateChangedListener;
import com.sscl.wifilibrary.intefaces.impl.DefaultOnWifiHotspotIsConnectedByOthersListener;
import com.sscl.wifilibrary.intefaces.impl.DefaultOnWifiHotspotStateChangedListener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ThreadFactory;

/**
 * @author jackie
 */
@SuppressWarnings("deprecation")
public class WifiHotspotController {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = WifiHotspotController.class.getSimpleName();
    /**
     * 默认的创建Wifi热点的名字
     */
    private static final String WIFI_HOTSPOT_SSID = "TEST";
    /**
     * 默认的创建Wifi热点的密码
     */
    private static final String WIFI_HOTSPOT_PASSWORD = "1234567890";
    /**
     * 默认的端口
     */
    private static final int DEFAULT_PORT = 65500;


    /*---------------------------成员变量---------------------------*/
    /**
     * 系统的WiFi管理器
     */
    private final android.net.wifi.WifiManager systemWifiManager;
    /**
     * 记录当前是否已经初始化
     */
    private boolean isInit;
    /**
     * WiFi配置
     */
    private WifiConfiguration wifiConfiguration;
    /**
     * WiFi热点相关的回调
     */
    private OnWifiHotspotStateChangedListener onWifiHotspotStateChangedListener;

    /**
     * 接收到数据时进行的回调
     */
    private OnDataReceivedListener onDataReceivedListener;

    /**
     * WiFi热点被连接的相关回调
     */
    private OnWifiHotspotIsConnectedByOthersListener onWifiHotspotIsConnectedByOthersListener;

    /**
     * 线程工厂
     */
    private ThreadFactory threadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r);
        }
    };

    private int port = DEFAULT_PORT;

    /**
     * Handler
     */
    private Handler handler = new Handler();
    /**
     * 旧的已经连接的IP地址
     */
    private ArrayList<String> oldConnectedIP;
    /**
     * WiFi热点是否已经开启
     */
    private boolean wifiApEnabled;
    private ArrayList<ConnectThread> connectThreads = new ArrayList<>();
    private OnWifiStateChangedListener onWifiStateChangedListener = new OnWifiStateChangedListener() {
        @Override
        public void onWifiEnabled() {

        }

        @Override
        public void onWifiEnabling() {

        }

        @Override
        public void onWifiDisabled() {
            createHotspot();
            WifiManager.setWifiStateChangedListener(null);
        }

        @Override
        public void onWifiDisabling() {

        }

        @Override
        public void unknownWifiState(int wifiState) {

        }
    };

    /*---------------------------构造方法---------------------------*/

    /**
     * 构造方法
     */
    WifiHotspotController() {
        systemWifiManager = WifiManager.getSystemWifiManager();
    }

    /*---------------------------公开方法---------------------------*/


    /**
     * 初始化
     */
    public void init() {
        init(false, new DefaultOnWifiHotspotStateChangedListener(), new DefaultOnWifiHotspotIsConnectedByOthersListener());
    }


    /**
     * 初始化
     *
     * @param onWifiHotspotStateChangedListener WiFi连接的回调
     */
    public void init(@NonNull OnWifiHotspotStateChangedListener onWifiHotspotStateChangedListener) {
        init(false, onWifiHotspotStateChangedListener, new DefaultOnWifiHotspotIsConnectedByOthersListener());
    }

    /**
     * 初始化
     *
     * @param onWifiHotspotIsConnectedByOthersListener WiFi热点被连接时的回调
     */
    public void init(@NonNull OnWifiHotspotIsConnectedByOthersListener onWifiHotspotIsConnectedByOthersListener) {
        init(false, new DefaultOnWifiHotspotStateChangedListener(), onWifiHotspotIsConnectedByOthersListener);
    }

    /**
     * 初始化
     *
     * @param onWifiHotspotStateChangedListener        WiFi连接的回调
     * @param onWifiHotspotIsConnectedByOthersListener WiFi热点被连接时的回调
     */
    public void init(@NonNull OnWifiHotspotStateChangedListener onWifiHotspotStateChangedListener,
                     @NonNull OnWifiHotspotIsConnectedByOthersListener onWifiHotspotIsConnectedByOthersListener) {
        init(false, onWifiHotspotStateChangedListener, onWifiHotspotIsConnectedByOthersListener);
    }

    /**
     * 初始化
     *
     * @param hiddenSsid                               是否隐藏热点
     * @param onWifiHotspotStateChangedListener        WiFi连接的回调
     * @param onWifiHotspotIsConnectedByOthersListener WiFi热点被连接时的回调
     */
    @SuppressWarnings("WeakerAccess")
    public void init(boolean hiddenSsid, @NonNull OnWifiHotspotStateChangedListener onWifiHotspotStateChangedListener, @NonNull OnWifiHotspotIsConnectedByOthersListener onWifiHotspotIsConnectedByOthersListener) {
        init(WIFI_HOTSPOT_SSID, hiddenSsid, onWifiHotspotStateChangedListener, onWifiHotspotIsConnectedByOthersListener);
    }

    /**
     * 初始化
     *
     * @param ssidName                                 热点名称
     * @param hiddenSsid                               是否隐藏热点
     * @param onWifiHotspotStateChangedListener        WiFi连接的回调
     * @param onWifiHotspotIsConnectedByOthersListener WiFi热点被连接时的回调
     */
    @SuppressWarnings("WeakerAccess")
    public void init(String ssidName, boolean hiddenSsid, @NonNull OnWifiHotspotStateChangedListener onWifiHotspotStateChangedListener, @NonNull OnWifiHotspotIsConnectedByOthersListener onWifiHotspotIsConnectedByOthersListener) {
        init(ssidName, WIFI_HOTSPOT_PASSWORD, hiddenSsid, true, WifiConfiguration.KeyMgmt.NONE, onWifiHotspotStateChangedListener, onWifiHotspotIsConnectedByOthersListener);
    }

    /**
     * 初始化
     *
     * @param ssidName                                 热点名称
     * @param preSharedKey                             热点密码
     * @param keyMgmt                                  热点加密方式
     * @param onWifiHotspotStateChangedListener        WiFi连接的回调
     * @param onWifiHotspotIsConnectedByOthersListener WiFi热点被连接时的回调
     */
    @SuppressWarnings("WeakerAccess")
    public void init(String ssidName, String preSharedKey, int keyMgmt, @NonNull OnWifiHotspotStateChangedListener onWifiHotspotStateChangedListener,
                     @NonNull OnWifiHotspotIsConnectedByOthersListener onWifiHotspotIsConnectedByOthersListener) {
        init(ssidName, preSharedKey, false, false, keyMgmt, onWifiHotspotStateChangedListener, onWifiHotspotIsConnectedByOthersListener);
    }

    /**
     * 初始化
     *
     * @param ssidName                                 热点名称
     * @param preSharedKey                             热点密码
     * @param hiddenSsid                               是否隐藏热点
     * @param withoutPassword                          是否需要密码
     * @param keyMgmt                                  热点加密方式
     * @param onWifiHotspotStateChangedListener        WiFi连接的回调
     * @param onWifiHotspotIsConnectedByOthersListener WiFi热点被连接时的回调
     */
    @SuppressWarnings("WeakerAccess")
    public void init(String ssidName, String preSharedKey, boolean hiddenSsid, boolean withoutPassword,
                     int keyMgmt, @NonNull OnWifiHotspotStateChangedListener onWifiHotspotStateChangedListener,
                     @NonNull OnWifiHotspotIsConnectedByOthersListener onWifiHotspotIsConnectedByOthersListener) {
        wifiConfiguration = getWifiConfiguration(ssidName, preSharedKey, hiddenSsid, withoutPassword, keyMgmt);
        this.onWifiHotspotStateChangedListener = onWifiHotspotStateChangedListener;
        this.onWifiHotspotIsConnectedByOthersListener = onWifiHotspotIsConnectedByOthersListener;
        isInit = true;
    }

    /**
     * 设置系统热点状态
     */
    public void createHotspot() {
        checkInitStatus();
        if (wifiConfiguration == null) {
            performWifiHotspotCreateFailedListener();
            return;
        }
        if (systemWifiManager == null) {
            DebugUtil.errorOut(TAG, "Create failed!The systemWifiManager is null");
            performWifiHotspotCreateFailedListener();
            return;
        }
        if (WifiManager.isWifiEnabled()) {
            WifiManager.enableWifi(false);
            WifiManager.setWifiStateChangedListener(onWifiStateChangedListener);
            return;
        }
        try {
            Method method = systemWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean result = (boolean) method.invoke(systemWifiManager, wifiConfiguration, true);
            if (!result) {
                performWifiHotspotCreateFailedListener();
                return;
            }
            if (onWifiHotspotStateChangedListener != null) {
                onWifiHotspotStateChangedListener.onWifiHotspotCreating(wifiConfiguration);
            }
            threadFactory.newThread(new Runnable() {
                @Override
                public void run() {
                    boolean wifiApEnabled;
                    do {
                        wifiApEnabled = isWifiApEnabled();
                        if (wifiApEnabled) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (onWifiHotspotStateChangedListener != null) {
                                        onWifiHotspotStateChangedListener.onWifiHotspotCreated(wifiConfiguration);
                                        Thread thread = threadFactory.newThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                while (isWifiApEnabled()) {
                                                    ArrayList<String> connectedIP = getConnectedIpStringList();
                                                    if (oldConnectedIP == null || oldConnectedIP.size() != connectedIP.size()) {
                                                        oldConnectedIP = connectedIP;
                                                        if (onWifiHotspotIsConnectedByOthersListener != null) {
                                                            onWifiHotspotIsConnectedByOthersListener.onConnectedDevices(connectedIP);
                                                        }

                                                        for (int i = 0; i < connectedIP.size(); i++) {
                                                            String ip = connectedIP.get(i);
                                                            DebugUtil.warnOut(TAG, "创建Socket。ip = " + ip);
                                                            Socket socket = null;
                                                            try {
                                                                socket = new Socket(ip, port);
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                                DebugUtil.warnOut(TAG, "创建Socket失败");
                                                            }
                                                            if (socket == null) {
                                                                continue;
                                                            }
                                                            ConnectThread connectThread = new ConnectThread(socket, ip, WifiHotspotController.this);
                                                            if (!connectThreads.contains(connectThread)) {
                                                                connectThread.setOnDataReceivedListener(onDataReceivedListener);
                                                                connectThread.start();
                                                                connectThreads.add(connectThread);
                                                            }
                                                            if (onWifiHotspotIsConnectedByOthersListener != null) {
                                                                onWifiHotspotIsConnectedByOthersListener.onDeviceConnected(ip);
                                                            }
                                                        }
                                                    } else {
                                                        boolean changed = false;
                                                        for (int i = 0; i < connectedIP.size(); i++) {
                                                            String ip = connectedIP.get(i);
                                                            for (int j = 0; j < oldConnectedIP.size(); j++) {
                                                                boolean contains = oldConnectedIP.contains(ip);
                                                                if (!contains) {
                                                                    changed = true;
                                                                    Socket socket = null;
                                                                    try {
                                                                        socket = new Socket(ip, port);
                                                                    } catch (IOException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    if (socket == null) {
                                                                        continue;
                                                                    }
                                                                    ConnectThread connectThread = new ConnectThread(socket, ip, WifiHotspotController.this);
                                                                    if (!connectThreads.contains(connectThread)) {
                                                                        connectThread.setOnDataReceivedListener(onDataReceivedListener);
                                                                        connectThread.start();
                                                                        connectThreads.add(connectThread);
                                                                    }
                                                                    if (onWifiHotspotIsConnectedByOthersListener != null) {
                                                                        onWifiHotspotIsConnectedByOthersListener.onDeviceConnected(ip);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        for (int i = 0; i < oldConnectedIP.size(); i++) {
                                                            String ip = oldConnectedIP.get(i);
                                                            for (int j = 0; j < connectedIP.size(); j++) {
                                                                boolean contains = connectedIP.contains(ip);
                                                                if (!contains) {
                                                                    changed = true;
                                                                    Socket socket = null;
                                                                    try {
                                                                        socket = new Socket(ip, port);
                                                                    } catch (IOException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    if (socket == null) {
                                                                        continue;
                                                                    }
                                                                    ConnectThread connectThread = new ConnectThread(socket, ip, WifiHotspotController.this);
                                                                    int indexOf = connectThreads.indexOf(connectThread);
                                                                    if (indexOf >= 0) {
                                                                        ConnectThread connectThread1 = connectThreads.get(indexOf);
                                                                        connectThread1.setKeepRun(false);
                                                                        connectThreads.remove(indexOf);
                                                                    }
                                                                    if (onWifiHotspotIsConnectedByOthersListener != null) {
                                                                        onWifiHotspotIsConnectedByOthersListener.onDeviceDisconnected(ip);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        oldConnectedIP = connectedIP;
                                                        if (changed) {
                                                            if (onWifiHotspotIsConnectedByOthersListener != null) {
                                                                onWifiHotspotIsConnectedByOthersListener.onConnectedDevices(connectedIP);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                        thread.start();
                                    }
                                }
                            });
                        }
                    } while (!wifiApEnabled);
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            performWifiHotspotCreateFailedListener();
        }
    }

    /**
     * 打开系统界面的网络共享与热点设置页面
     */
    public void openSystemHotspotActivity(Context context) {
        Intent intent = new Intent();
        //直接打开热点设置页面（不同ROM有差异）
        ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$TetherSettingsActivity");
        //下面这个是打开网络共享与热点设置页面
        //ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
        intent.setComponent(comp);
        context.startActivity(intent);
    }

    /**
     * 读取热点配置信息
     *
     * @return WifiConfiguration热点配置信息
     */
    public WifiConfiguration getWifiAPConfig() {
        checkInitStatus();
        android.net.wifi.WifiManager systemWifiManager = WifiManager.getSystemWifiManager();
        try {
            if (systemWifiManager == null) {
                return null;
            }
            Method method = systemWifiManager.getClass().getMethod("getWifiApConfiguration");
            return (WifiConfiguration) method.invoke(systemWifiManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭热点
     */
    public void close() {
        checkInitStatus();
        if (!isWifiApEnabled()) {
            if (onWifiHotspotStateChangedListener != null) {
                onWifiHotspotStateChangedListener.onWifiHotspotCloseFailed(wifiConfiguration);
            }
            return;
        }

        try {
            Method method = systemWifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration config = (WifiConfiguration) method.invoke(systemWifiManager);
            Method method2 = systemWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean result = (boolean) method2.invoke(systemWifiManager, config, false);
            if (!result) {
                if (onWifiHotspotStateChangedListener != null) {
                    onWifiHotspotStateChangedListener.onWifiHotspotCloseFailed(wifiConfiguration);
                }
                return;
            }
            if (onWifiHotspotStateChangedListener != null) {
                onWifiHotspotStateChangedListener.onWifiHotspotClosing();
            }
            threadFactory.newThread(new Runnable() {
                @Override
                public void run() {
                    do {
                        wifiApEnabled = isWifiApEnabled();
                        if (!wifiApEnabled) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (onWifiHotspotStateChangedListener != null) {
                                        onWifiHotspotStateChangedListener.onWifiHotspotClosed(wifiConfiguration);
                                    }
                                }
                            });
                        }
                    } while (wifiApEnabled);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            if (onWifiHotspotStateChangedListener != null) {
                onWifiHotspotStateChangedListener.onWifiHotspotCloseFailed(wifiConfiguration);
            }
        }
    }

    /**
     * 热点开关是否打开
     *
     * @return 获取WiFi热点是否打开
     */
    public boolean isWifiApEnabled() {
        checkInitStatus();
        if (systemWifiManager == null) {
            DebugUtil.errorOut(TAG, "systemWifiManager is null!");
            return false;
        }

        try {
            Method method = systemWifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (boolean) method.invoke(systemWifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置收到数据的回调
     *
     * @param onDataReceivedListener 收到数据的回调
     */
    public void setOnDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
        this.onDataReceivedListener = onDataReceivedListener;
    }

    /**
     * 发送数据
     *
     * @param ip   目标IP
     * @param data 发送的数据
     */
    public boolean sendData(String ip, byte[] data) {
        for (int i = 0; i < connectThreads.size(); i++) {
            ConnectThread connectThread = connectThreads.get(i);
            String connectThreadIp = connectThread.getIp();
            if (connectThreadIp.equals(ip)) {
                return connectThread.sendData(new String(data));
            }
        }
        return false;
    }

    /**
     * 更改端口号
     */
    public void setPort(int port) {
        this.port = port;
    }

    /*---------------------------接口定义---------------------------*/

    /**
     * 获取热点配置
     *
     * @param ssidName        热点名称
     * @param preSharedKey    热点密码
     * @param hiddenSsid      是否隐藏热点
     * @param withoutPassword 是否需要密码
     * @return 热点配置
     */
    private WifiConfiguration getWifiConfiguration(String ssidName, String preSharedKey, boolean hiddenSsid, boolean withoutPassword, int keyMgmt) {
        WifiConfiguration config = new WifiConfiguration();
        //要创建的WiFi的名称
        config.SSID = ssidName;
        //要创建的WiFi的密码
        config.preSharedKey = preSharedKey;
        //是否隐藏WiFi（使WiFi不可被搜索到）
        config.hiddenSSID = hiddenSsid;
        //开放系统认证
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        if (withoutPassword) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else {
            if (keyMgmt == WifiConfiguration.KeyMgmt.NONE) {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            } else {
                config.allowedKeyManagement.set(keyMgmt);
            }
        }
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;
        return config;
    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 检查是否已经初始化
     */
    private void checkInitStatus() {
        if (!isInit) {
            throw new IllegalStateException("Please invoke method init(...) before invoke this method");
        }
    }

    /**
     * 获取连接到热点上的手机ip
     *
     * @return 连接到热点上的手机IP集合
     */
    private ArrayList<String> getConnectedIpStringList() {
        ArrayList<String> connectedIP = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(
                    "/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(" +");
                if (split.length >= 4) {
                    String ip = split[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //第一个不是IP地址，需要删除
        if (connectedIP.size() > 0) {
            connectedIP.remove(0);
        }
        return connectedIP;
    }

    private void performWifiHotspotCreateFailedListener() {
        WifiManager.getHANDLER().post(new Runnable() {
            @Override
            public void run() {
                if (onWifiHotspotStateChangedListener != null) {
                    onWifiHotspotStateChangedListener.onWifiHotspotCreateFailed(wifiConfiguration);
                }
            }
        });
    }

    /**
     * 当数据切换后的监听
     */
    public interface OnDataReceivedListener {
        /**
         * 接收到数据时的回调
         *
         * @param ip   IP地址
         * @param data 数据
         */
        void onDataReceived(String ip, byte[] data);
    }
}
