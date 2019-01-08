package com.jackiepenghe.wifilibrary;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 连接线程
 *
 * @author 坤
 * @date 2016/9/7
 */
public class ConnectThread extends Thread {

    private static final String TAG = ConnectThread.class.getSimpleName();

    private Socket socket;
    private OutputStream outputStream;
    private String ip;
    private WifiHotspotController.OnDataReceivedListener onDataReceivedListener;
    private WifiHotspotController wifiHotspotController;
    private boolean keepRun = true;
    private Handler handler = new Handler();

    ConnectThread(Socket socket, String ip, WifiHotspotController wifiHotspotController) {
        setName("ConnectThread" + ip);
        this.socket = socket;
        this.ip = ip;
        this.wifiHotspotController = wifiHotspotController;
        DebugUtil.warnOut(TAG, "ConnectThread create:ip = " + ip);
    }

    @Override
    public void run() {
/*        if(activeConnect){
//            socket.c
        }*/
        if (socket == null) {
            return;
        }
        InputStream inputStream = null;
        try {
            //获取数据流
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            byte[] buffer = new byte[1024];
            int bytes;
            while (wifiHotspotController != null && wifiHotspotController.isWifiApEnabled() && keepRun) {
                //读取数据
                bytes = inputStream.read(buffer);
                if (bytes > 0) {
                    final byte[] data = new byte[bytes];
                    System.arraycopy(buffer, 0, data, 0, bytes);

                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("MSG", new String(data));
                    message.setData(bundle);
                    DebugUtil.warnOut(TAG + " " + ip, "读取到数据" + new String(data));
                    if (onDataReceivedListener != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onDataReceivedListener.onDataReceived(ip, data);
                            }
                        });
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            socket = null;
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outputStream = null;
            }
            ip = null;
            onDataReceivedListener = null;
            wifiHotspotController = null;
            keepRun = false;
            handler = null;
        }
    }

    /**
     * 发送数据
     */
    @SuppressWarnings("WeakerAccess")
    public boolean sendData(String msg) {
        if (outputStream != null) {
            try {
                outputStream.write(msg.getBytes());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConnectThread that = (ConnectThread) o;
        return ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        return ip.hashCode();
    }

    /**
     * 设置收到数据时进行的回调
     *
     * @param onDataRecievedListener 收到数据时进行的回调
     */
    @SuppressWarnings("WeakerAccess")
    public void setOnDataReceivedListener(WifiHotspotController.OnDataReceivedListener onDataRecievedListener) {
        this.onDataReceivedListener = onDataRecievedListener;
    }

    @SuppressWarnings("WeakerAccess")
    public void setKeepRun(boolean keepRun) {
        this.keepRun = keepRun;
    }

    /**
     * 获取当前连接对应的IP
     *
     * @return 当前连接对应的IP
     */
    @SuppressWarnings("WeakerAccess")
    public String getIp() {
        return ip;
    }
}
