package com.sscl.x.wifisample.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.sscl.baselibrary.activity.BaseAppCompatActivity;
import com.sscl.baselibrary.utils.DebugUtil;
import com.sscl.baselibrary.utils.ToastUtil;
import com.sscl.wifilibrary.WifiHotspotController;
import com.sscl.wifilibrary.WifiManager;
import com.sscl.wifilibrary.intefaces.OnWifiHotspotStateChangedListener;
import com.sscl.x.wifisample.R;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

/**
 * @author jackie
 */
@SuppressWarnings("deprecation")
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
    private OnWifiHotspotStateChangedListener onWifiHotspotStateChangedListener = new OnWifiHotspotStateChangedListener() {
        @Override
        public void onWifiHotspotCreating(WifiConfiguration wifiConfiguration) {
            ToastUtil.toastLong(WifiHotspotActivity.this, "正在创建热点");
        }

        @Override
        public void onWifiHotspotCreated(WifiConfiguration wifiConfiguration) {
            ToastUtil.toastLong(WifiHotspotActivity.this, "创建热点成功");
        }

        @Override
        public void onWifiHotspotCreateFailed(WifiConfiguration wifiConfiguration) {
            ToastUtil.toastLong(WifiHotspotActivity.this, "创建热点失败");
        }

        @Override
        public void onWifiHotspotClosing() {
            ToastUtil.toastLong(WifiHotspotActivity.this, "正在关闭热点");
        }

        @Override
        public void onWifiHotspotClosed(WifiConfiguration wifiConfiguration) {
            ToastUtil.toastLong(WifiHotspotActivity.this, "热点关闭成功");
        }

        @Override
        public void onWifiHotspotCloseFailed(WifiConfiguration wifiConfiguration) {
            ToastUtil.toastLong(WifiHotspotActivity.this, "热点关闭失败");
        }
    };
    /**
     * WiFi热点收到数据的监听事件
     */
    private WifiHotspotController.OnDataReceivedListener onDataReceivedListener = new WifiHotspotController.OnDataReceivedListener() {
        @Override
        public void onDataReceived(String ip, byte[] data) {
            DebugUtil.warnOut(TAG, "ip = " + ip + " , data = " + new String(data));
            boolean b = wifiHotspotController.sendData(ip, "回应数据".getBytes());
            if (b) {
                DebugUtil.warnOut(TAG, "ip = " + ip + " , data = 回应数据");
            }
        }
    };
    private Action<Void> onGrantedListener = new Action<Void>() {
        @Override
        public void onAction(Void data) {
            //创建WiFi热点
            wifiHotspotController.createHotspot();
        }
    };
    private Action<Void> onDeniedListener = new Action<Void>() {
        @Override
        public void onAction(Void data) {
            ToastUtil.toastLong(WifiHotspotActivity.this, "没有权限，热点创建失败！");
        }
    };
    private Rationale<Void> rationaleListener = new Rationale<Void>() {
        @Override
        public void showRationale(Context context, Void data, final RequestExecutor executor) {
            new AlertDialog.Builder(WifiHotspotActivity.this)
                    .setTitle(R.string.no_write_setting_permission)
                    .setMessage(R.string.no_write_setting_permission_message)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executor.execute();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executor.cancel();
                        }
                    })
                    .setCancelable(false)
                    .show();

        }
    };
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
        if (wifiHotspotController.isWifiApEnabled()) {
            wifiHotspotController.close();
        }
        WifiManager.releaseWifiHotspotController();
    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 初始化WiFi热点创建器
     */
    private void initWifiHotspotCreator() {
        wifiHotspotController = WifiManager.getWifiHotspotControllerInstance();
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
                ToastUtil.toastLong(WifiHotspotActivity.this, "创建WiFi热点时需要关闭WiFi。请关闭WiFi后重试！");
                return;
            }
        }
        AndPermission.with(this)
                .setting()
                .write()
                .onGranted(onGrantedListener)
                .onDenied(onDeniedListener)
                .rationale(rationaleListener)
                .start();

    }

    /**
     * 关闭WiFi热点
     */
    private void closeHotspot() {
        //如果WiFi热点已经被创建了，才能关闭
        if (wifiHotspotController.isWifiApEnabled()) {
            wifiHotspotController.close();
        } else {
            ToastUtil.toastLong(WifiHotspotActivity.this, "WiFi热点未创建，不需要关闭");
        }
    }
}
