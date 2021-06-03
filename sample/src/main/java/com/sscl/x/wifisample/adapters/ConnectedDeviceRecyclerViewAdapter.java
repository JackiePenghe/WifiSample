package com.sscl.x.wifisample.adapters;


import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.sscl.wifilibrary.bean.ConnectedDevice;

import java.util.List;

public class ConnectedDeviceRecyclerViewAdapter extends BaseQuickAdapter<ConnectedDevice, BaseViewHolder> {
    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public ConnectedDeviceRecyclerViewAdapter(@Nullable List<ConnectedDevice> data) {
        super(android.R.layout.simple_list_item_2, data);
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param helper A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    @Override
    protected void convert(BaseViewHolder helper, ConnectedDevice item) {

    }
}
