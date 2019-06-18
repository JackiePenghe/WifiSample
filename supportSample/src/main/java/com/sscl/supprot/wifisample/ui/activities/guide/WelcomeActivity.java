package com.sscl.supprot.wifisample.ui.activities.guide;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.sscl.baselibrary.activity.BaseWelcomeActivity;
import com.sscl.baselibrary.utils.CrashHandler;
import com.sscl.baselibrary.utils.ToastUtil;
import com.sscl.supprot.wifisample.R;
import com.sscl.supprot.wifisample.ui.activities.MainActivity;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.List;

/**
 * @author jackie
 */
public class WelcomeActivity extends BaseWelcomeActivity {

    private static final String TAG = WelcomeActivity.class.getSimpleName();

    /*---------------------------成员变量---------------------------*/

//    private WifiManager.RequestPermissionResult requestPermissionResult = new WifiManager.RequestPermissionResult() {
//
//        /**
//         * 请求成功
//         */
//        @Override
//        public void requestSuccess() {
//            rqeustPermissions();
//            DebugUtil.warnOut(TAG,"requestSuccess");
//        }
//
//        /**
//         * 请求失败
//         */
//        @Override
//        public void requestFailed() {
//            WifiManager.requestSystemSettingsPermissionRational(WelcomeActivity.this, requestPermissionResult);
//            DebugUtil.warnOut(TAG,"requestFailed");
//        }
//
//        /**
//         * 取消二次权限请求
//         */
//        @Override
//        public void requestRationalCanceled() {
//            rqeustPermissions();
//            DebugUtil.warnOut(TAG,"requestRationalCanceled");
//        }
//    };

    /**
     * 权限请求被拒绝后执行的操作
     */
    private Action<List<String>> deniedAction = new Action<List<String>>() {
        @Override
        public void onAction(List<String> permissions) {
            ToastUtil.toastL(WelcomeActivity.this, R.string.permission_denied);
            toNext();
        }
    };

    /**
     * 权限请求通过后进行的操作
     */
    private Action<List<String>> grantedAction = new Action<List<String>>() {
        @Override
        public void onAction(List<String> permissions) {
            toNext();
        }
    };

    /**
     * 用户第一次拒绝了权限，再次请求此权限时，提醒用户为什么需要该权限，以免用户反感
     */
    private Rationale<List<String>> rationale = new Rationale<List<String>>() {
        @Override
        public void showRationale(Context context, List<String> permissions, final RequestExecutor executor) {
            new AlertDialog.Builder(WelcomeActivity.this)
                    .setTitle(R.string.request_permission)
                    .setMessage(R.string.request_permission_message)
                    .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executor.execute();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executor.cancel();
                            toNext();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    };

    /*---------------------------实现父类方法---------------------------*/

    /**
     * 当动画执行完成后调用这个函数
     */
    @Override
    protected void doAfterAnimation() {
        rqeustPermissions();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            //6.0以上系统需要判断程序是否拥有更改系统设置的权限，用于创建热点
//            if (WifiManager.hasWifiHotspotPermission()) {
//                //如果已经有更改系统设置的权限
//                rqeustPermissions();
//            } else {
//                //请求更改系统设置的权限
////                WifiManager.requestSystemSettingsPermission(WelcomeActivity.this, requestPermissionResult);
//            }
//        } else {
//            rqeustPermissions();
//        }
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

    /*---------------------------私有方法---------------------------*/

    /**
     * 正常情况下应该执行的操作
     */
    private void rqeustPermissions() {
        //权限请求
        AndPermission.with(WelcomeActivity.this)
                .runtime()
                .permission(Permission.Group.LOCATION, Permission.Group.STORAGE)
                .onDenied(deniedAction)
                .onGranted(grantedAction)
                .rationale(rationale)
                .start();
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
