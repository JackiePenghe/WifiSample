package com.sscl.wifilibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


/**
 * @author jackie
 */
@TargetApi(Build.VERSION_CODES.M)
public class PermissionActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_WRITE_SETTINGS = 10;
    private static WifiManager.RequestPermissionResult requestPermissionResult;

    public static void requestPermission(Context context, WifiManager.RequestPermissionResult requestPermissionResult) {

        PermissionActivity.requestPermissionResult = requestPermissionResult;
        Intent intent = new Intent(context, PermissionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

    /**
     * Set the content layout full the StatusBar, but do not hide StatusBar.
     * 设置整个布局填充到状态栏，但又不阻挡状态栏的显示。
     */
    private static void invasionStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        invasionStatusBar(this);
        requestPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (WifiManager.hasWifiHotspotPermission()) {
                if (requestPermissionResult != null) {
                    requestPermissionResult.requestSuccess();
                }
            } else {
                if (requestPermissionResult != null) {
                    requestPermissionResult.requestFailed();
                }
            }
        }
        requestPermissionResult = null;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 100);
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @SuppressWarnings("JavadocReference")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (WifiManager.hasWifiHotspotPermission()) {
                if (requestPermissionResult != null) {
                    requestPermissionResult.requestSuccess();
                }
            } else {
                if (requestPermissionResult != null) {
                    requestPermissionResult.requestFailed();
                }
            }
        }
        requestPermissionResult = null;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 100);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    /**
     * 请求权限
     */
    private void requestPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
//        ActivityCompat.requestPermissions(PermissionActivity.this, new String[]{Manifest.permission.WRITE_SETTINGS},REQUEST_CODE_WRITE_SETTINGS);
    }
}
