package com.jackiepenghe.wifisample.ui.activities;

import android.net.wifi.WifiConfiguration;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.jackiepenghe.baselibrary.activity.BaseAppCompatActivity;
import com.jackiepenghe.baselibrary.tools.Tool;
import com.jackiepenghe.wifilibrary.WifiHotspotController;
import com.jackiepenghe.wifilibrary.WifiManager;
import com.jackiepenghe.wifilibrary.intefaces.OnWifiHotspotStateChangedListener;
import com.jackiepenghe.wifisample.R;

/**
 * @author jackie
 */
public class WifiHotspotActivity extends BaseAppCompatActivity {

    private static final String TAG = WifiHotspotActivity.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * WiFi热点创建器
     */
    private WifiHotspotController wifiHotspotController;
    /**
     * 创建WiFi热点的按钮
     */
    private Button createHotspotButton;
    /**
     * 关闭WiFi热点的按钮
     */
    private Button closeHotspotButton;
    /**
     * 点击事件处理回调接口
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                //创建WiFi热点
                case R.id.create_hotspot:
                    createHotspot();
                    break;
                case R.id.close_hotspot:
                    closeHotspot();
                    break;
                default:
                    break;
            }
        }
    };
    private OnWifiHotspotStateChangedListener onWifiHotspotStateChangedListener = new OnWifiHotspotStateChangedListener() {
        @Override
        public void onWifiHotspotCreating(WifiConfiguration wifiConfiguration) {
            Tool.toastL(WifiHotspotActivity.this, "正在创建热点");
        }

        @Override
        public void onWifiHotspotCreated(WifiConfiguration wifiConfiguration) {
            Tool.toastL(WifiHotspotActivity.this, "创建热点成功");
        }

        @Override
        public void onWifiHotspotCreateFailed(WifiConfiguration wifiConfiguration) {
            Tool.toastL(WifiHotspotActivity.this, "创建热点失败");
        }

        @Override
        public void onWifiHotspotClosing() {
            Tool.toastL(WifiHotspotActivity.this, "正在关闭热点");
        }

        @Override
        public void onWifiHotspotClosed(WifiConfiguration wifiConfiguration) {
            Tool.toastL(WifiHotspotActivity.this, "热点关闭成功");
        }

        @Override
        public void onWifiHotspotCloseFailed(WifiConfiguration wifiConfiguration) {
            Tool.toastL(WifiHotspotActivity.this, "热点关闭失败");
        }
    };
    /**
     * WiFi热点收到数据的监听事件
     */
    private WifiHotspotController.OnDataReceivedListener onDataReceivedListener = new WifiHotspotController.OnDataReceivedListener() {
        @Override
        public void onDataReceived(String ip, byte[] data) {
            Tool.warnOut(TAG, "ip = " + ip + " , data = " + new String(data));
            boolean b = wifiHotspotController.sendData(ip, "回应数据".getBytes());
            if (b) {
                Tool.warnOut(TAG, "ip = " + ip + " , data = 回应数据");
            }
        }
    };

    /*---------------------------实现父类方法---------------------------*/

    /**
     * 标题栏的返回按钮被按下的时候回调此函数
     */
    @Override
    protected void titleBackClicked() {
        onBackPressed();
    }

    /**
     * 在设置布局之前需要进行的操作
     */
    @Override
    protected void doBeforeSetLayout() {

    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_wifi_hotspot;
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    @Override
    protected void doBeforeInitOthers() {
        initWifiHotspotCreator();
    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        createHotspotButton = findViewById(R.id.create_hotspot);
        closeHotspotButton = findViewById(R.id.close_hotspot);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {

    }

    /**
     * 初始化其他数据
     */
    @Override
    protected void initOtherData() {

    }

    /**
     * 初始化事件
     */
    @Override
    protected void initEvents() {
        createHotspotButton.setOnClickListener(onClickListener);
        closeHotspotButton.setOnClickListener(onClickListener);
    }

    /**
     * 在最后进行的操作
     */
    @Override
    protected void doAfterAll() {

    }

    /**
     * 设置菜单
     *
     * @param menu 菜单
     * @return 只是重写 public boolean onCreateOptionsMenu(Menu menu)
     */
    @Override
    protected boolean createOptionsMenu(Menu menu) {
        return false;
    }

    /**
     * 设置菜单监听
     *
     * @param item 菜单的item
     * @return true表示处理了监听事件
     */
    @Override
    protected boolean optionsItemSelected(MenuItem item) {
        return false;
    }

    /*---------------------------重写父类方法---------------------------*/

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (wifiHotspotController.isWifiApEnabled()) {
            wifiHotspotController.close();
        }
        WifiManager.releaseWifiHotspotCreator();
    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 初始化WiFi热点创建器
     */
    private void initWifiHotspotCreator() {
        wifiHotspotController = WifiManager.getWifiHotspotCreatorInstance();
        wifiHotspotController.setPort(65500);
        //这个监听函数所监听的数据来自于指定端口，该端口可通过wifiHotspotController.setPort(int port)进行设置（在热点创建成功之前，更改端口才可生效）
        wifiHotspotController.setOnDataReceivedListener(onDataReceivedListener);
        wifiHotspotController.init(onWifiHotspotStateChangedListener);
    }

    /**
     * 开始创建热点
     */
    private void createHotspot() {
        //如果WiFi已经打开了,要先关掉WiFi
        if (WifiManager.isWifiEnabled()) {
            boolean result = WifiManager.enableWifi(false);
            if (!result) {
                Tool.toastL(WifiHotspotActivity.this, "创建WiFi热点时需要关闭WiFi。请关闭WiFi后重试！");
                return;
            }
        }
        //创建WiFi热点
        wifiHotspotController.createHotspot();

    }

    /**
     * 关闭WiFi热点
     */
    private void closeHotspot() {
        //如果WiFi热点已经被创建了，才能关闭
        if (wifiHotspotController.isWifiApEnabled()) {
            wifiHotspotController.close();
        } else {
            Tool.toastL(WifiHotspotActivity.this, "WiFi热点未创建，不需要关闭");
        }
    }
}
