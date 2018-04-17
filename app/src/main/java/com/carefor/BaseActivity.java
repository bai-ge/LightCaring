package com.carefor;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by baige on 2017/12/24.
 */

public class BaseActivity extends AppCompatActivity {

    public static List<BaseActivity> activities = new ArrayList<>();

    private PermissionListener mlistener;

    private Toast mToast;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activities.add(this);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        Log.d("finishAll", "添加" + this.getClass().getCanonicalName() + "活动：" + activities.size());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activities.remove(this);
        Log.d("finishAll", "删除" + this.getClass().getCanonicalName() + "活动：" + activities.size());
    }

    public void finishAll() {
        for (BaseActivity activity : activities) {
            activity.finish();
        }
    }

    protected void showTip(final String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.setText(text);
                mToast.show();
            }
        });
    }


    /**
     * 权限申请
     *
     * @param permissions 待申请的权限集合
     * @param listener    申请结果监听事件
     */
    protected void requestRunTimePermission(String[] permissions, PermissionListener listener) {
        this.mlistener = listener;

        //用于存放为授权的权限
        List<String> permissionList = new ArrayList<>();
        //遍历传递过来的权限集合
        for (String permission : permissions) {
            //判断是否已经授权
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                //未授权，则加入待授权的权限集合中
                permissionList.add(permission);
            }
        }

        //判断集合
        if (!permissionList.isEmpty()) {  //如果集合不为空，则需要去授权
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 1);
        } else {  //为空，则已经全部授权
            if(listener != null){
                listener.onGranted();
            }

        }
    }

    /**
     * 权限申请结果
     *
     * @param requestCode  请求码
     * @param permissions  所有的权限集合
     * @param grantResults 授权结果集合
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    //被用户拒绝的权限集合
                    List<String> deniedPermissions = new ArrayList<>();
                    //用户通过的权限集合
                    List<String> grantedPermissions = new ArrayList<>();
                    for (int i = 0; i < grantResults.length; i++) {
                        //获取授权结果，这是一个int类型的值
                        int grantResult = grantResults[i];

                        if (grantResult != PackageManager.PERMISSION_GRANTED) { //用户拒绝授权的权限
                            String permission = permissions[i];
                            deniedPermissions.add(permission);
                        } else {  //用户同意的权限
                            String permission = permissions[i];
                            grantedPermissions.add(permission);
                        }
                    }

                    if (deniedPermissions.isEmpty()) {  //用户拒绝权限为空
                        if(mlistener != null){
                            mlistener.onGranted();
                        }
                    } else {  //不为空
                        if(mlistener != null){
                            //回调授权成功的接口
                            mlistener.onDenied(deniedPermissions);
                            //回调授权失败的接口
                            mlistener.onGranted(grantedPermissions);
                        }

                    }
                }
                break;
            default:
                break;
        }
    }


    public interface PermissionListener {

        //授权成功
        void onGranted();

        //授权部分
        void onGranted(List<String> grantedPermission);

        //拒绝授权
        void onDenied(List<String> deniedPermission);
    }

}
