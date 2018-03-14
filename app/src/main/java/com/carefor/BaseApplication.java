package com.carefor;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.util.Log;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.carefor.location.LocationService;
import com.carefor.mainui.DaemonService;
import com.carefor.telephone.TelePhone;
import com.carefor.util.Loggerx;
import com.carefor.util.Tools;

import cn.jpush.android.api.JPushInterface;

/**
 * 主Application，所有百度定位SDK的接口说明请参考线上文档：http://developer.baidu.com/map/loc_refer/index.html
 * <p>
 * 百度定位SDK官方网站：http://developer.baidu.com/map/index.php?title=android-locsdk
 * <p>
 * 直接拷贝com.baidu.location.service包到自己的工程下，简单配置即可获取定位结果，也可以根据demo内容自行封装
 */
public class BaseApplication extends Application {
    private final static String TAG = BaseApplication.class.getCanonicalName();

    public Vibrator mVibrator;

    @Override
    public void onCreate() {
        super.onCreate();

        if(checkCallingPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Loggerx.bWriteToFile = true;
        }
        //日志管理
        Loggerx.d(TAG, "打开应用");
        LocationService locationService = LocationService.getInstance(getApplicationContext());

        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);

        //初始化极光推送
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        String text = JPushInterface.getRegistrationID(this);
        Log.d("JPush", "JPush ID:"+text);

        //初始化电话
        TelePhone.getInstance().init(getApplicationContext());

        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);


        //守护服务
        Intent intent = new Intent(this, DaemonService.class);
        intent.putExtra(DaemonService.ALARM_START, true);
        startService(intent);

        //网络监测
        Tools.checkNetwork(getApplicationContext());
    }
}
