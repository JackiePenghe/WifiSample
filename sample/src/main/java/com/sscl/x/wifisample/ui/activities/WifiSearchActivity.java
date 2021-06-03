package com.sscl.x.wifisample.ui.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.sscl.baselibrary.activity.BaseAppCompatActivity;
import com.sscl.baselibrary.utils.DebugUtil;
import com.sscl.baselibrary.utils.DefaultItemDecoration;
import com.sscl.baselibrary.utils.ToastUtil;
import com.sscl.wifilibrary.WifiConnector;
import com.sscl.wifilibrary.WifiManager;
import com.sscl.wifilibrary.WifiScanner;
import com.sscl.wifilibrary.bean.WifiDevice;
import com.sscl.wifilibrary.enums.EncryptWay;
import com.sscl.wifilibrary.intefaces.OnWifiConnectStateChangedListener;
import com.sscl.wifilibrary.intefaces.OnWifiScanStateChangedListener;
import com.sscl.x.wifisample.R;
import com.sscl.x.wifisample.adapters.WifiDeviceAdapter;
import com.yanzhenjie.kalle.JsonBody;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

import java.util.ArrayList;
import java.util.List;

//import com.yanzhenjie.nohttp.NoHttp;
//import com.yanzhenjie.nohttp.RequestMethod;
//import com.yanzhenjie.nohttp.rest.OnResponseListener;
//import com.yanzhenjie.nohttp.rest.Request;
//import com.yanzhenjie.nohttp.rest.Response;
//import com.yanzhenjie.nohttp.rest.SimpleResponseListener;

/**
 * @author jackie
 */
public class WifiSearchActivity extends BaseAppCompatActivity {

    /*---------------------------静态常量---------------------------*/

    private static final String TAG = WifiSearchActivity.class.getSimpleName();
    private static final int WIFI_PASSWORD_MIN_LENGTH = 8;

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
     * 配置WiFi设备的IP地址
     */
    private static final String DEVICE_CONFIG_URL = "http://192.168.4.1";
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

    private OnWifiScanStateChangedListener onWifiScanStateChangedListener = new OnWifiScanStateChangedListener() {
        @Override
        public void startScanFailed() {
            ToastUtil.toastLong(WifiSearchActivity.this, "扫描开启失败");
        }

        @Override
        public void isScanning() {
            ToastUtil.toastLong(WifiSearchActivity.this, "扫描已开启，正在扫描");
        }

        @Override
        public void wifiScanResultObtained(@Nullable ArrayList<WifiDevice> wifiDevices) {
            if (wifiDevices == null) {
                return;
            }
            DebugUtil.warnOut(TAG, "扫描结果已获取");
            ToastUtil.toastLong(WifiSearchActivity.this, R.string.search_finished);
            int size = wifiDevices.size();
            if (size == 0) {
                DebugUtil.warnOut(TAG, "没有搜索到任何WiFi");
                ToastUtil.toastLong(WifiSearchActivity.this, "没有搜索到任何WiFi");
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
    private OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
            WifiDevice wifiDevice = adapterList.get(position);
            startConnect(wifiDevice);
        }
    };
    private OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
            String ssid = adapterList.get(position).getSSID();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                wifiConnector.forgetNetwork(ssid);
                return true;
            }
            return false;
        }
    };

    /*---------------------------实现父类方法---------------------------*/

    /**
     * 标题栏的返回按钮被按下的时候回调此函数
     */
    @Override
    protected boolean titleBackClicked() {
        return false;
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
    protected boolean createOptionsMenu(@NonNull Menu menu) {
        return false;
    }

    /**
     * 设置菜单监听
     *
     * @param item 菜单的item
     * @return true表示处理了监听事件
     */
    @Override
    protected boolean optionsItemSelected(@NonNull MenuItem item) {
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
        DefaultItemDecoration defaultItemDecoration = DefaultItemDecoration.newLine(Color.GRAY);
        recyclerView.addItemDecoration(defaultItemDecoration);
        wifiScanResultAdapter.setOnItemClickListener(onItemClickListener);
        wifiScanResultAdapter.setOnItemLongClickListener(onItemLongClickListener);
        recyclerView.setAdapter(wifiScanResultAdapter);
    }

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
            ToastUtil.toastLong(WifiSearchActivity.this, text);
            if (alertDialog == null) {
                return;
            }
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
                alertDialog = null;
            }
            configDeviceTcpServer();
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
            ToastUtil.toastLong(WifiSearchActivity.this, "取消连接 SSID:" + ssid);

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

    private void showSetPasswordDialog(final WifiDevice wifiDevice) {
        final EditText editText = (EditText) View.inflate(this, R.layout.password_edit_text, null);
        new AlertDialog.Builder(this)
                .setTitle(R.string.set_password)
                .setView(editText)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        String password = editText.getText().toString();
                        if (password.length() < WIFI_PASSWORD_MIN_LENGTH) {
                            ToastUtil.toastLong(WifiSearchActivity.this, R.string.password_too_short);
                            showSetPasswordDialog(wifiDevice);
                            return;
                        }
                        wifiConnector.connect(wifiDevice.getSSID(), password, false, wifiDevice.getEncryptWay(), false);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void startConnect(WifiDevice wifiDevice) {
//        if (wifiConnector.isContainswifi(wifiDevice.getSSID())) {
//            wifiConnector.connectExistsWifi(wifiDevice.getSSID(), true);
//        } else {
        EncryptWay encryptWay = wifiDevice.getEncryptWay();
        if (encryptWay == EncryptWay.NO_ENCRYPT) {
            wifiConnector.connect(wifiDevice.getSSID(), null, false, encryptWay, true);
        } else {
            showSetPasswordDialog(wifiDevice);
        }
//        }
    }

    /**
     * 配置设备的TCP服务器IP
     */
    private void configDeviceTcpServer() {
//        NoHttp.initialize(getApplicationContext());
//        ConfigDeviceTcpServivceInfoBean configDeviceTcpServivceInfoBean = new ConfigDeviceTcpServivceInfoBean();
//        ConfigDeviceTcpServivceInfoBean.RequestBean requestBean = new ConfigDeviceTcpServivceInfoBean.RequestBean();
//        ConfigDeviceTcpServivceInfoBean.RequestBean.Tcp tcp = new ConfigDeviceTcpServivceInfoBean.RequestBean.Tcp();
//        tcp.setDomain(tcpServiceDomain);
//        tcp.setIp(tcpServiceIp);
//        tcp.setPort(tcpServicePort);
//        tcp.setToken(tcpServiceToken);
//        tcp.setToken(null);
//        requestBean.setTcp(tcp);
//        configDeviceTcpServivceInfoBean.setRequest(requestBean);
//        String json = GSON.toJson(configDeviceTcpServivceInfoBean);
//        DebugUtil.warnOut(TAG, "configDeviceTcpServer json = " + json);
        Kalle.post(DEVICE_CONFIG_URL)
                .path("config")
                .urlParam("command", "cloud_server")
                .body(new JsonBody("{\"request\":{\"tcp\":{\"ip\":\"115.29.202.58\",\"port\":8000}}}"))
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        if (response.isSucceed()) {
                            String succeed = response.succeed();
                            DebugUtil.warnOut(TAG, "succeed = " + succeed);
                        } else {
                            String failed = response.failed();
                            DebugUtil.warnOut(TAG, "failed = " + failed);
                        }
                    }
                });

//        Request<String> stringRequest = NoHttp.createStringRequest(DEVICE_CONFIG_URL, RequestMethod.POST);
//        stringRequest.path("config")
//                .add("command", "cloud_server")
//                .setDefineRequestBodyForJson();
//        NoHttp.getRequestQueueInstance().add(1, stringRequest, new OnResponseListener<String>() {
//            @Override
//            public void onStart(int what) {
//                DebugUtil.warnOut(TAG,"onStart");
//            }
//
//            @Override
//            public void onSucceed(int what, Response<String> response) {
//                DebugUtil.warnOut(TAG,"onSucceed");
//
//            }
//
//            @Override
//            public void onFailed(int what, Response<String> response) {
//                DebugUtil.warnOut(TAG,"onFailed");
//
//            }
//
//            @Override
//            public void onFinish(int what) {
//                DebugUtil.warnOut(TAG,"onFinish");
//
//            }
//        });
    }
}
