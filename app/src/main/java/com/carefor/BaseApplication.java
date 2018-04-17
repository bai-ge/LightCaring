package com.carefor;


import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.carefor.broadcast.SendMessageBroadcast;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.data.source.remote.Parm;
import com.carefor.location.LocationService;
import com.carefor.service.DaemonService;
import com.carefor.telephone.TelePhone;
import com.carefor.util.Loggerx;
import com.carefor.util.Tools;
import com.coolerfall.daemon.Daemon;

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

        if(Tools.checkPermissionWriteExternalStorage(getApplicationContext())){
            Loggerx.bWriteToFile = true;
        }
       if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1){
           Loggerx.bWriteToFile = false;
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

        //初始化本地发送广播
        SendMessageBroadcast.getInstance().init(getApplicationContext());

        //初始化电话
        TelePhone.getInstance().init(getApplicationContext());

        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);


        //守护服务
//        Intent intent = new Intent(this, DaemonService.class);
//        intent.putExtra(DaemonService.ALARM_START, true);
//        startService(intent);
        //守护服务

        Intent intent = new Intent(this, DaemonService.class);
        if(!Tools.isEmpty(text)){
            intent.putExtra(Parm.DEVICE_ID, text);
        }
        startService(new Intent(this, DaemonService.class));
        registerReceiver();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            Log.d(TAG, "action ="+action);
            Log.d(TAG, Tools.printBundle(bundle));
            if(action.equals(Intent.ACTION_BATTERY_CHANGED)){
                int level = intent.getIntExtra( "level" , 0 );//电量（0-100）
                int status = intent.getIntExtra( "status" , 0 );
                int health = intent.getIntExtra( "health" , 1 );
                boolean present = intent.getBooleanExtra( "present" , false );
                int scale = intent.getIntExtra( "scale" , 0 );
                int plugged = intent.getIntExtra( "plugged" , 0 );//
                int voltage = intent.getIntExtra( "voltage" , 0 );//电压
                int temperature = intent.getIntExtra( "temperature" , 0 ); // 温度的单位是10℃
                String technology = intent.getStringExtra( "technology" );
                if(scale != 0){
                   CacheRepository.getInstance().setBatteryPercent((float) (level * 1.0 / scale));
                    Log.d(TAG, ""+CacheRepository.getInstance().getBatteryPercent());
                    Log.d(TAG, "batteryPercent = "+(level * 1.0 / scale));
                }else{
                    CacheRepository.getInstance().setBatteryPercent(0);
                }
            }
        }
    };

    private void registerReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mReceiver, filter);
    }
    private void unRegisterReceiver(){
        unregisterReceiver(mReceiver);
    }
}
