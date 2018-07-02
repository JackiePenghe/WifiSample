package com.jackiepenghe.wifisample.adapters;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.jackiepenghe.wifilibrary.WifiDevice;
import com.jackiepenghe.wifisample.R;

import java.util.List;

/**
 * @author jackie
 */
public class WifiScanResultAdapter extends BaseQuickAdapter<WifiDevice, BaseViewHolder> {

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public WifiScanResultAdapter(@Nullable List<WifiDevice> data) {
        super(R.layout.adapter_wifi_scan_result, data);
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param helper A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    @Override
    protected void convert(BaseViewHolder helper, WifiDevice item) {

        String encryptionWay = item.getEncryptionWayString();
        helper.setImageResource(R.id.signal,item.getLevelDrawableResId())
                .setText(R.id.text1, item.getSSID())
                .setText(R.id.text2, encryptionWay)
                .setText(R.id.level, item.getStringLevel() + "(" + item.getIntLevel() + ")");
    }
}
