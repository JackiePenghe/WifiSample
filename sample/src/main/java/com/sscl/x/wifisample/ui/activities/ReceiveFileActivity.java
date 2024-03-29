package com.sscl.x.wifisample.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.wifi.WifiConfiguration;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sscl.baselibrary.activity.BaseAppCompatActivity;
import com.sscl.baselibrary.utils.DefaultItemDecoration;
import com.sscl.wifilibrary.DataReceiver;
import com.sscl.wifilibrary.WifiManager;
import com.sscl.wifilibrary.bean.ConnectedDevice;
import com.sscl.wifilibrary.intefaces.OnP2pReceiverStateChangedListener;
import com.sscl.x.wifisample.R;
import com.sscl.x.wifisample.adapters.ConnectedDeviceRecyclerViewAdapter;

import java.util.ArrayList;

/**
 * 接收文件的Activity
 *
 * @author jackie
 */
@SuppressWarnings("deprecation")
public class ReceiveFileActivity extends BaseAppCompatActivity {

    private ArrayList<ConnectedDevice> connectedDevices = new ArrayList<>();
    private ConnectedDeviceRecyclerViewAdapter connectedDeviceRecyclerViewAdapter = new ConnectedDeviceRecyclerViewAdapter(connectedDevices);
    private DefaultItemDecoration defaultItemDecoration = DefaultItemDecoration.newLine(Color.GRAY);
    /**
     * 显示当前状态的文本框
     */
    private TextView stateTextView;
    /**
     * p2p接收状态改变时的回调
     */
    private OnP2pReceiverStateChangedListener onP2PReceiverStateChangedListener = new OnP2pReceiverStateChangedListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onHotspotCreating(WifiConfiguration wifiConfiguration) {
            stateTextView.setText("onHotspotCreating");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onHotspotCreated(WifiConfiguration wifiConfiguration) {
            stateTextView.setText("onHotspotCreated");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onHotspotCreateFailed(WifiConfiguration wifiConfiguration) {
            stateTextView.setText("onHotspotCreateFailed");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onHotspotClosing() {
            stateTextView.setText("onHotspotClosing");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onHotspotClosed(WifiConfiguration wifiConfiguration) {
            stateTextView.setText("onHotspotClosed");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onHotspotCloseFailed(WifiConfiguration wifiConfiguration) {
            stateTextView.setText("onHotspotCloseFailed");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onConnectedDevices(ArrayList<ConnectedDevice> connectedDevices) {
            stateTextView.setText("onConnectedDevices");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onDeviceConnected(ConnectedDevice connectedDevice) {
            stateTextView.setText("onDeviceConnected");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onDeviceDisconnected(ConnectedDevice connectedDevice) {
            stateTextView.setText("onDeviceDisconnected");
        }
    };
    /**
     * P2P接收器
     */
    private DataReceiver dataReceiver;
    /**
     * 显示已经连接的设备的列表
     */
    private RecyclerView recyclerView;

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
        initP2pReceiver();
    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_receive_file;
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    @Override
    protected void doBeforeInitOthers() {

    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        stateTextView = findViewById(R.id.state_tv);
        recyclerView = findViewById(R.id.recycler_view);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {
        initRecyclerViewData();
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

    }

    /**
     * 在最后进行的操作
     */
    @Override
    protected void doAfterAll() {
        dataReceiver.startReceiverListener();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataReceiver.close();
    }

    private void initP2pReceiver() {
        dataReceiver = WifiManager.getDataReceiverInstance();
        dataReceiver.setOnP2PReceiverStateChangedListener(onP2PReceiverStateChangedListener);
    }

    private void initRecyclerViewData() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(defaultItemDecoration);
        recyclerView.setAdapter(connectedDeviceRecyclerViewAdapter);
    }
}
