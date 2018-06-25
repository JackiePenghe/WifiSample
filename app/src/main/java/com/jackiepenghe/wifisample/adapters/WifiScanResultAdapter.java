package com.jackiepenghe.wifisample.adapters;

import android.net.wifi.ScanResult;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.jackiepenghe.wifilibrary.WifiOperatingTools;
import com.jackiepenghe.wifisample.R;

import java.util.List;

/**
 * @author jackie
 */
public class WifiScanResultAdapter extends BaseQuickAdapter<ScanResult, BaseViewHolder> {

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public WifiScanResultAdapter(@Nullable List<ScanResult> data) {
        super(R.layout.adapter_wifi_scan_result, data);
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param helper A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    @Override
    protected void convert(BaseViewHolder helper, ScanResult item) {
        String levelText;
        String ssid = item.SSID;
        if (ssid == null || "".equals(ssid)) {
            ssid = "隐藏网络";
        }

        int level1 = -50;
        int level2 = -60;
        int level3 = -70;
        int level4 =  -80;

        int level = item.level;
        if (level >= level1) {
            levelText = "很强";
        } else if (level >= level2) {
            levelText = "强";
        } else if (level >= level3) {
            levelText = "中";
        } else if (level >= level4) {
            levelText = "弱";
        } else {
            levelText = "很弱";
        }

        String encryptionWay = WifiOperatingTools.getEncryptionWayString(mContext, item);

        helper.setText(R.id.text1, ssid)
                .setText(R.id.text2, encryptionWay)
                .setText(R.id.level, levelText);
    }
}
