package com.sscl.supprot.wifisample.ui.activities;

import android.graphics.Color;
import android.support.annotation.Nullable;
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
import com.sscl.baselibrary.activity.BaseAppCompatActivity;
import com.sscl.baselibrary.utils.DebugUtil;
import com.sscl.baselibrary.utils.ToastUtil;
import com.sscl.baselibrary.view.utils.DefaultItemDecoration;
import com.sscl.supprot.wifisample.R;
import com.sscl.supprot.wifisample.adapters.WifiDeviceAdapter;
import com.sscl.wifilibrary.WifiConnector;
import com.sscl.wifilibrary.WifiManager;
import com.sscl.wifilibrary.WifiScanner;
import com.sscl.wifilibrary.bean.WifiDevice;
import com.sscl.wifilibrary.intefaces.OnWifiConnectStateChangedListener;
import com.sscl.wifilibrary.intefaces.OnWifiScanStateChangedListener;

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

    private WifiConnector wifiConnector;

    /**
     * 用来显示当前的连接状态
     */
    private TextView textView;

    /**
     * 开始搜索按钮
     */
    private Button searchButton;

    private WifiScanner wifiScanner;

    /**
     * 适配器的数据源
     */
    private List<WifiDevice> adapterList = new ArrayList<>();

    /**
     * 适配器
     */
    private WifiDeviceAdapter wifiScanResultAdapter = new WifiDeviceAdapter(adapterList);


    /**
     * WiFi连接的扫描回调
     */
    private OnWifiConnectStateChangedListener onWifiConnectStateChangedListener = new OnWifiConnectStateChangedListener() {

        private AlertDialog alertDialog;

        /**
         * 正在连接
         * @param ssid WiFi的名称
         */
        @Override
        public void connecting(String ssid) {
            DebugUtil.warnOut(TAG, "connecting ssid = " + ssid);
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
            DebugUtil.warnOut(TAG, "connected ssid = " + ssid);
            DebugUtil.warnOut(TAG, "已连接");
            textView.setText(R.string.connected);
            String text = getString(R.string.connect_success, ssid);
            ToastUtil.toastL(WifiSearchActivity.this, text);
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
            DebugUtil.warnOut(TAG, "disconnected");
            DebugUtil.warnOut(TAG, "已断开连接");
        }

        /**
         * 正在进行身份授权
         *
         * @param ssid WiFi的名称
         */
        @Override
        public void authenticating(String ssid) {
            DebugUtil.warnOut(TAG, "authenticating ssid = " + ssid);
            textView.setText(R.string.authenticating);
        }

        /**
         * 正在获取IP地址
         *
         * @param ssid WiFi的名称
         */
        @Override
        public void obtainingIpAddress(String ssid) {
            DebugUtil.warnOut(TAG, "obtainingIpAddress ssid = " + ssid);
            textView.setText(R.string.obtaining_ip_address);
        }

        /**
         * 连接失败
         *
         * @param ssid WiFi的名称
         */
        @Override
        public void connectFailed(String ssid) {
            DebugUtil.warnOut(TAG, "connectFailed ssid = " + ssid);
            textView.setText(R.string.connect_failed);
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
                alertDialog = null;
            }
        }

        /**
         * 正在断开连接
         */
        @Override
        public void disconnecting() {
            DebugUtil.warnOut(TAG, "disconnecting");
            DebugUtil.warnOut(TAG, "正在断开连接");
        }

        /**
         * 未知状态
         */
        @Override
        public void unknownStatus() {
            DebugUtil.warnOut(TAG, "未知连接状态");
            textView.setText("未知连接状态");
        }

        /**
         * 用户取消了连接动作
         */
        @Override
        public void cancelConnect(String ssid) {
            DebugUtil.warnOut(TAG, "用户取消了本次连接");
            ToastUtil.toastL(WifiSearchActivity.this, "取消连接 SSID:" + ssid);

        }

        /**
         * 连接超时
         */
        @Override
        public void connectTimeOut() {
            textView.setText(R.string.connect_time_out);
            if (alertDialog == null) {
                return;
            }
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
                alertDialog = null;
            }
        }
    };
    /**
     * 点击事件的处理
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //noinspection SwitchStatementWithTooFewBranches
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
            WifiDevice wifiDevice = adapterList.get(position);
            startConnect(wifiDevice);
        }
    };

    private OnWifiScanStateChangedListener onWifiScanStateChangedListener = new OnWifiScanStateChangedListener() {
        @Override
        public void startScanFailed() {
            ToastUtil.toastL(WifiSearchActivity.this, "扫描开启失败");
        }

        @Override
        public void isScanning() {
            ToastUtil.toastL(WifiSearchActivity.this, "扫描已开启，正在扫描");
        }

        @Override
        public void wifiScanResultObtained(@Nullable ArrayList<WifiDevice> wifiDevices) {
            if (wifiDevices == null) {
                return;
            }
            DebugUtil.warnOut(TAG, "扫描结果已获取");
            ToastUtil.toastL(WifiSearchActivity.this, R.string.search_finished);
            int size = wifiDevices.size();
            if (size == 0) {
                DebugUtil.warnOut(TAG, "没有搜索到任何WiFi");
                ToastUtil.toastL(WifiSearchActivity.this, "没有搜索到任何WiFi");
                return;
            }

            for (int i = 0; i < size; i++) {
                WifiDevice wifiDevice = wifiDevices.get(i);
                String ssid = wifiDevice.getSSID();
                int level = wifiDevice.getIntLevel();
                DebugUtil.warnOut(TAG, "设备 " + (i + 1) + " :ssid = " + ssid + ",level = " + level);
            }

            adapterList.clear();
            adapterList.addAll(wifiDevices);
            wifiScanResultAdapter.notifyDataSetChanged();
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
        initWifiConnector();
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
        wifiScanner.close();
        WifiManager.releaseWifiScanner();
    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 初始化WiFi扫描器
     */
    private void initWifiScanner() {
        wifiScanner = WifiManager.getWifiScannerInstance();
        wifiScanner.setOnWifiScanStateChangedListener(onWifiScanStateChangedListener);
    }

    private void initWifiConnector() {
        wifiConnector = WifiManager.getWifiConnectorInstance();
        wifiConnector.addOnWifiConnectStateChangedListener(onWifiConnectStateChangedListener);
    }

    /**
     * 开始扫描（搜索WiFi）
     */
    private void startScan() {
        wifiScanner.startScan();
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

    private void startConnect(WifiDevice wifiDevice) {
        wifiConnector.startConnect(WifiSearchActivity.this, wifiDevice);
    }
}
