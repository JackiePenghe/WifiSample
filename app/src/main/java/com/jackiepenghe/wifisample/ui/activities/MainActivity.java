package com.jackiepenghe.wifisample.ui.activities;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.jackiepenghe.baselibrary.activity.BaseAppCompatActivity;
import com.jackiepenghe.wifilibrary.WifiManager;
import com.jackiepenghe.wifisample.R;

/**
 * @author jackie
 */
public class MainActivity extends BaseAppCompatActivity {

    /*---------------------------成员变量---------------------------*/

    /**
     * WiFi热点按钮
     */
    private Button hotspotButton;
    /**
     * 标准WiFi使用
     */
    private Button wifiButton;
    /**
     * 点击事件处理回调接口
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId()) {
                //创建WiFi热点
                case R.id.wifi_hotspot:
                    intent = new Intent(MainActivity.this, WifiHotspotActivity.class);
                    startActivity(intent);
                    break;
                case R.id.wifi:
                    intent = new Intent(MainActivity.this, WifiActivity.class);
                    startActivity(intent);
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
        return R.layout.activity_main;
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    @Override
    protected void doBeforeInitOthers() {
        hideTitleBackButton();
    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        hotspotButton = findViewById(R.id.wifi_hotspot);
        wifiButton = findViewById(R.id.wifi);
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
        hotspotButton.setOnClickListener(onClickListener);
        wifiButton.setOnClickListener(onClickListener);
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

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        WifiManager.releaseAll();
    }
}
