package com.jackiepenghe.wifisample.ui.activities;

import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jackiepenghe.baselibrary.BaseAppCompatActivity;
import com.jackiepenghe.baselibrary.DefaultItemDecoration;
import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.wifilibrary.WifiDevice;
import com.jackiepenghe.wifilibrary.WifiManager;
import com.jackiepenghe.wifilibrary.WifiOperatingTools;
import com.jackiepenghe.wifisample.R;
import com.jackiepenghe.wifisample.adapters.WifiDeviceAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jackie
 */
public class WifiSearchActivity extends BaseAppCompatActivity {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = WifiSearchActivity.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

    /**
     * 用于显示WiFi列表的控件
     */
    private RecyclerView recyclerView;

    /**
     * 用来显示当前的连接状态
     */
    private TextView textView;

    /**
     * 开始搜索按钮
     */
    private Button searchButton;

    /**
     * WiFi扫描器
     */
    private WifiOperatingTools wifiOperatingTools;

    /**
     * 适配器的数据源
     */
    private List<WifiDevice> adapterList = new ArrayList<>();
//    private List<ScanResult> adapterList = new ArrayList<>();

    /**
     * 适配器
     */
    private WifiDeviceAdapter wifiScanResultAdapter = new WifiDeviceAdapter(adapterList);
//    private WifiScanResultAdapter wifiScanResultAdapter = new WifiScanResultAdapter(adapterList);

    /**
     * WiFi扫描器的扫描回调
     */
    private WifiOperatingTools.WifiScanCallback wifiScanCallback = new WifiOperatingTools.WifiScanCallback() {
        @Override
        public void startScanFailed() {
            Tool.toastL(WifiSearchActivity.this, "扫描开启失败");
        }

        @Override
        public void isScanning() {
            Tool.toastL(WifiSearchActivity.this, "扫描已开启，正在扫描");
        }
    };

    /**
     * WiFi连接的扫描回调
     */
    private WifiOperatingTools.WifiConnectCallback wifiConnectCallback = new WifiOperatingTools.WifiConnectCallback() {

        private AlertDialog alertDialog;

        /**
         * 正在连接
         * @param ssid WiFi的名称
         */
        @Override
        public void connecting(String ssid) {
            if (alertDialog != null) {
                return;
            }
            View view = View.inflate(WifiSearchActivity.this, R.layout.connecting, null);
            try {
                alertDialog = new AlertDialog.Builder(WifiSearchActivity.this)
                        .setView(view)
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 已连接
         * @param ssid WiFi的名称
         */
        @Override
        public void connected(String ssid) {
            Tool.warnOut(TAG, "已连接");
            textView.setText(R.string.connected);
            String text = getString(R.string.connect_success, ssid);
            Tool.toastL(WifiSearchActivity.this, text);
            if (alertDialog == null) {
                return;
            }
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
                alertDialog = null;
            }
        }

        /**
         * 已断开连接
         */
        @Override
        public void disconnected() {
            Tool.warnOut(TAG, "已断开连接");
        }

        /**
         * 正在进行身份授权
         *
         * @param ssid WiFi的名称
         */
        @Override
        public void authenticating(String ssid) {
            textView.setText(R.string.authenticating);
        }

        /**
         * 正在获取IP地址
         *
         * @param ssid WiFi的名称
         */
        @Override
        public void obtainingIpAddress(String ssid) {
            textView.setText(R.string.obtaining_ip_address);
        }

        /**
         * 连接失败
         *
         * @param ssid WiFi的名称
         */
        @Override
        public void connectFailed(String ssid) {
            textView.setText(R.string.connect_failed);
        }

        /**
         * 正在断开连接
         */
        @Override
        public void disconnecting() {
            Tool.warnOut(TAG, "正在断开连接");
        }

        /**
         * 未知状态
         */
        @Override
        public void unknownStatus() {
            Tool.warnOut(TAG, "未知连接状态");
            textView.setText("未知连接状态");
        }

        /**
         * 用户取消了连接动作
         */
        @Override
        public void cancelConnect(String ssid) {
            Tool.warnOut(TAG, "用户取消了本次连接");
            Tool.toastL(WifiSearchActivity.this, "取消连接 SSID:" + ssid);
        }
    };

    /**
     * 获取到WiFi的扫描结果后进行的回调
     */
    private WifiOperatingTools.WifiScanResultObtainedListener wifiScanResultObtainedListener = new WifiOperatingTools.WifiScanResultObtainedListener() {
        @Override
        public void wifiScanResultObtained(ArrayList<WifiDevice> wifiDevices) {
            Tool.warnOut(TAG, "扫描结果已获取");
            Tool.toastL(WifiSearchActivity.this,R.string.search_finished);
            int size = wifiDevices.size();
            if (size == 0) {
                Tool.toastL(WifiSearchActivity.this, "没有搜索到任何WiFi");
                return;
            }

            for (int i = 0; i < size; i++) {
                WifiDevice wifiDevice = wifiDevices.get(i);
                String ssid =  wifiDevice.getSSID();
                int level = wifiDevice.getIntLevel();
                Tool.warnOut(TAG, "设备 " + (i + 1) + " :ssid = " + ssid + ",level = " + level);
            }

            adapterList.clear();
            adapterList.addAll(wifiDevices);
            wifiScanResultAdapter.notifyDataSetChanged();
        }
    };
    /**
     * 点击事件的处理
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.start_search:
                    startScan();
                    break;
                default:
                    break;
            }
        }
    };

    private BaseQuickAdapter.OnItemClickListener onItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            startConnect(adapterList.get(position));
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
        return R.layout.activity_wifi_search;
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    @Override
    protected void doBeforeInitOthers() {
        initWifiScanner();
    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        searchButton = findViewById(R.id.start_search);
        recyclerView = findViewById(R.id.recycler_view);
        textView = findViewById(R.id.connect_status);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {
        initRecyclerView();
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
        searchButton.setOnClickListener(onClickListener);
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
        wifiOperatingTools.close();
        WifiManager.releaseWifiOperatingTools();
    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 初始化WiFi扫描器
     */
    private void initWifiScanner() {
        wifiOperatingTools = WifiManager.getWifiOperatingToolsInstance();
        wifiOperatingTools.init(wifiScanCallback, wifiConnectCallback);
        wifiOperatingTools.setWifiScanResultObtainedListener(wifiScanResultObtainedListener);
    }

    /**
     * 开始扫描（搜索WiFi）
     */
    private void startScan() {
        wifiOperatingTools.startScan();
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WifiSearchActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DefaultItemDecoration defaultItemDecoration = new DefaultItemDecoration(Color.GRAY, ViewGroup.LayoutParams.MATCH_PARENT, 1, -1);
        recyclerView.addItemDecoration(defaultItemDecoration);
        wifiScanResultAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(wifiScanResultAdapter);
    }

    /**
     * 开始连接
     *
     * @param wifiDevice 扫描结果
     */
    private void startConnect(WifiDevice wifiDevice) {
        if (wifiDevice == null) {
            return;
        }
        wifiOperatingTools.startConnect(WifiSearchActivity.this, wifiDevice);
    }
}
