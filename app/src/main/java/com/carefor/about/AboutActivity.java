package com.carefor.about;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.carefor.BaseActivity;
import com.carefor.mainui.R;
import com.carefor.util.Tools;

public class AboutActivity extends BaseActivity {

    private final static String TAG = AboutActivity.class.getCanonicalName();
    private Toolbar mToolbar;

    private TextView mTxtVersion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTxtVersion = (TextView) findViewById(R.id.tv_version);
        mToolbar.setTitle("关于");

        //为activity窗口设置活动栏
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        //设置返回图标
        actionBar.setHomeAsUpIndicator(0);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String versionName = getVersionName();
        if(!Tools.isEmpty(versionName)){
            mTxtVersion.setText("当前版本："+versionName);
        }
        Log.d(TAG, "当前版本："+versionName);
    }

    /**
     * 获取版本号
     * @return
     */
    public int getVersionCode(){
        PackageManager manager = getPackageManager();//获取包管理器
        try {
            //通过当前的包名获取包的信息
            PackageInfo info = manager.getPackageInfo(getPackageName(),0);//获取包对象信息
            return  info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取坂本明
     * @return
     */
    public String getVersionName(){
        PackageManager manager = getPackageManager();
        try {
            //第二个参数PackageManager.GET_ACTIVITIES代表额外的信息，例如获取当前应用中的所有的Activity
            PackageInfo packageInfo = manager.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
//            ActivityInfo[] activities = packageInfo.activities;
//            showActivities(activities);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
