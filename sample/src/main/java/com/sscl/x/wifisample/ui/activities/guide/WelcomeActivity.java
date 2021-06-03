package com.sscl.x.wifisample.ui.activities.guide;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.sscl.baselibrary.activity.BaseWelcomeActivity;
import com.sscl.baselibrary.utils.CrashHandler;
import com.sscl.baselibrary.utils.PermissionUtil;
import com.sscl.baselibrary.utils.ToastUtil;
import com.sscl.x.wifisample.R;
import com.sscl.x.wifisample.ui.activities.MainActivity;

/**
 * @author jackie
 */
public class WelcomeActivity extends BaseWelcomeActivity {

    private static final String TAG = WelcomeActivity.class.getSimpleName();

    private static final int REQUEST_CODE_SETTING = 1;

    /*---------------------------实现父类方法---------------------------*/

    /**
     * 当动画执行完成后调用这个函数
     */
    @Override
    protected void doAfterAnimation() {
        checkPermissions();
    }

    /**
     * 设置ImageView的图片资源
     *
     * @return 图片资源ID
     */
    @Override
    protected int setImageViewSource() {
        return 0;
    }

    /*---------------------------重写方法---------------------------*/

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETTING) {
            checkPermissions();
        }
    }

    /*---------------------------私有方法---------------------------*/

    /**
     * 正常情况下应该执行的操作
     */
    private void checkPermissions() {

        boolean hasPermissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            hasPermissions = PermissionUtil.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        } else {
            hasPermissions = PermissionUtil.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (hasPermissions) {
            toNext();
        } else {
            showNoPermissionDialog();
        }
    }

    /**
     * 显示没有权限的对话框
     */
    private void showNoPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.no_permission)
                .setMessage(R.string.no_permission_message)
                .setPositiveButton(R.string.settings, (dialog, which) -> {
                    PermissionUtil.toSettingActivity(WelcomeActivity.this, REQUEST_CODE_SETTING);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    ToastUtil.toastLong(WelcomeActivity.this, R.string.no_permission_exit);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 跳转到下一个界面
     */
    private void toNext() {
        CrashHandler.getInstance().init(this);
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
