package com.sscl.x.wifisample.ui.activities;

import android.annotation.SuppressLint;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sscl.baselibrary.activity.BaseAppCompatActivity;
import com.sscl.wifilibrary.DataTransmitter;
import com.sscl.wifilibrary.WifiManager;
import com.sscl.wifilibrary.intefaces.OnP2pTransmitterStateChangedListener;
import com.sscl.x.wifisample.R;

/**
 * 发送文件
 *
 * @author jackie
 */
public class SendFileActivity extends BaseAppCompatActivity {

    private DataTransmitter dataTransmitter;
    private TextView stateTextView;
    private OnP2pTransmitterStateChangedListener onP2pTransmitterStateChangedListener = new OnP2pTransmitterStateChangedListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void connecting() {
            stateTextView.setText("connecting");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void connected() {
            stateTextView.setText("connected");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void disconnected() {
            stateTextView.setText("disconnected");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void authenticating() {
            stateTextView.setText("authenticating");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void obtainingIpAddress() {
            stateTextView.setText("obtainingIpAddress");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void connectFailed() {
            stateTextView.setText("connectFailed");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void disconnecting() {
            stateTextView.setText("disconnecting");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void unknownState() {
            stateTextView.setText("unknownState");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void cancelConnect() {
            stateTextView.setText("cancelConnect");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void connectTime() {
            stateTextView.setText("connectTime");
        }

        @Override
        public void searchedNothing() {
            stateTextView.setText("searchedNothing");
        }
    };

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
        initP2pTransmitter();
    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_send_file;
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

    }

    /**
     * 在最后进行的操作
     */
    @Override
    protected void doAfterAll() {
        dataTransmitter.connect();
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

    private void initP2pTransmitter() {
        dataTransmitter = WifiManager.getDataTransmitterInstance();
        dataTransmitter.setOnP2pTransmitterStateChangedListener(onP2pTransmitterStateChangedListener);
    }
}
