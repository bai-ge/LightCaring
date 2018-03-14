package com.carefor.mainui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.carefor.connect.Connector;
import com.carefor.data.entity.DeviceModel;
import com.carefor.data.entity.Transinformation;
import com.carefor.telephone.PhoneActivity;
import com.carefor.util.Loggerx;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by baige on 2018/2/13.
 */

/*
* 打开单独的线程，检查网络连接是否正常
* */

public class DaemonService extends Service {
    private final static String TAG = DaemonService.class.getCanonicalName();

    private AlarmManager am;

    public final static String  ALARM_START = "Alarm_start";

    private DaemonBroadcastReceiver mDaemonBroadcastReceiver;

    private long mSendHeartBeatTime;

    private Timer mTimer;

    private TimerTask mHeartBeatTask;



    @Override
    public void onCreate() {
        super.onCreate();
        Connector connector = Connector.getInstance();
        connector.RegistConnectorListener("service", mOnConnectorListener);
        if (!connector.isConnectServer()) {
            connector.afxConnectServer();
        }
        registerReceiver();
        Loggerx.d(TAG, "onCreate()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

        /*
AlarmManager.ELAPSED_REALTIME：使用相对时间，可以通过SystemClock.elapsedRealtime() 获取（从开机到现在的毫秒数，包括手机的睡眠时间），设备休眠时并不会唤醒设备。
AlarmManager.ELAPSED_REALTIME_WAKEUP：与ELAPSED_REALTIME基本功能一样，只是会在设备休眠时唤醒设备。
AlarmManager.RTC：使用绝对时间，可以通过 System.currentTimeMillis()获取，设备休眠时并不会唤醒设备。
AlarmManager.RTC_WAKEUP: 与RTC基本功能一样，只是会在设备休眠时唤醒设备。
    */


    /*
    *
    *
    *
    set(int type,long triggerAtTime,PendingIntent operation)：设置在triggerAtTime时间启动由operation参数指定的组件。
    setInexactRepeating(int type,long triggerAtTime,long interval, PendingIntent operation)：设置一个非精确的周期性任务。任务近似地以interval参数指定的时间间隔执行，如果果由于某些原因（如垃圾回收或其他后台活动）使得某一个任务延迟执行了，那么系统就会调整后续任务的执行时间，保证不会因为一个任务的提前或滞后而影响到所有任务的执行，这样看来，任务就没有精确地按照interval参数指定的间隔执行。
    setRepeating(int type,long triggerAtTime,long interval,PendingIntent operation)：设置一个周期性执行的定时任务，和上面的方法相比，这个方法执行的是精确的定时任务，系统会尽量保证时间间隔固定不变，如果某一个任务被延迟了，那么后续的任务也相应地被延迟。
    上面几个方法中几个参数含义如下：
    1. type 定时任务的类型，该参数可以接收如下值：
    ELAPSED_REALTIME：表示闹钟在手机睡眠状态下不可用，该状态下闹钟使用相对时间（相对于系统启动开始）。
    ELAPSED_REALTIME_WAKEUP： 表示闹钟在睡眠状态下会唤醒系统并执行提示功能，该状态下闹钟也使用相对时间。
    RTC：表示闹钟在手机睡眠状态下不可用，该状态下闹钟使用绝对时间（即系统时间）。当系统调用System.currentTimeMillis()方法的返回值与triggerAtTime相等时启动operation所对应的组件。
    RTC_WAKEUP：表示闹钟在睡眠状态下会唤醒系统并执行提示功能，该状态下闹钟也使用绝对时间。

    AlarmManager.ELAPSED_REALTIME：使用相对时间，可以通过SystemClock.elapsedRealtime() 获取（从开机到现在的毫秒数，包括手机的睡眠时间），设备休眠时并不会唤醒设备。
    AlarmManager.ELAPSED_REALTIME_WAKEUP：与ELAPSED_REALTIME基本功能一样，只是会在设备休眠时唤醒设备。
    AlarmManager.RTC：使用绝对时间，可以通过 System.currentTimeMillis()获取，设备休眠时并不会唤醒设备。
    AlarmManager.RTC_WAKEUP: 与RTC基本功能一样，只是会在设备休眠时唤醒设备。
    */

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        Loggerx.d(TAG, "onStartCommand()");

        boolean alarmStart = intent.getBooleanExtra(ALARM_START, false);
        checkWork();
        if(alarmStart){
            Loggerx.d(TAG, "onStartCommand() alarm start");
            am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent startintent = new Intent(this, DaemonService.class); //定期启动服务
            startintent.putExtra(ALARM_START, true);
            startintent.setAction(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, startintent, PendingIntent.FLAG_UPDATE_CURRENT);

            if(Build.VERSION.SDK_INT < 19){
                am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 60000, pendingIntent);
            }else{
                am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 60000, pendingIntent);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void checkWork(){
        //检查网络状态
        Loggerx.d(TAG, "checkWork()");
        Connector.getInstance().afxConnectServer();

        //检查心跳包发送是否正常
        if(System.currentTimeMillis() - mSendHeartBeatTime >= 35000){
            if(mHeartBeatTask != null ){
                mHeartBeatTask.cancel();
            }
            if(mTimer == null){
                mTimer = new Timer();
            }
            mHeartBeatTask = new TimerTask() {
                @Override
                public void run() {
                    Connector.getInstance().sendHeartBeat();
                    mSendHeartBeatTime = System.currentTimeMillis();
                }
            };
            mTimer.schedule(mHeartBeatTask, 0, 30000);
        }
    }

    private void registerReceiver() {

        final IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_SCREEN_OFF);// 屏幕灭屏广播

        filter.addAction(Intent.ACTION_SCREEN_ON);// 屏幕亮屏广播

        filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);//拨打电话广播

        filter.addAction("android.intent.action.PHONE_STATE");//监听通话状态

        filter.addAction(Intent.ACTION_MEDIA_BUTTON);

        mDaemonBroadcastReceiver = new DaemonBroadcastReceiver();

        registerReceiver(mDaemonBroadcastReceiver, filter);
    }

    public void unRegisterReceiver() {
        if(mDaemonBroadcastReceiver != null){
            unregisterReceiver(mDaemonBroadcastReceiver);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
        Connector.getInstance().unRegistConnectorListener("service");
        Loggerx.d(TAG, "onDestroy()");
    }

    private Connector.OnConnectorListener mOnConnectorListener = new Connector.OnConnectorListener() {
        @Override
        public void connectSuccess() {

        }

        @Override
        public void loginSuccess() {

        }

        @Override
        public void disconnected(String mid) {

        }

        @Override
        public void receviceP2P(String device_id, String ip, int port) {

        }

        @Override
        public void beCalled(DeviceModel deviceModel) {
            //TODO 设置电话的状态
            Log.d(TAG, "服务器接收到呼叫信息");
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(getApplicationContext(), PhoneActivity.class);
            startActivity(intent);
        }

        @Override
        public void pickUp(Transinformation tranfor) {
            //TODO 设置电话的状态
        }

        @Override
        public void handUp(Transinformation tranfor) {
            //TODO 设置电话的状态
        }
    };

    class DaemonBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Loggerx.d(TAG, "DaemonBroadcastReceiver :"+action);
            checkWork();
        }
    }
}
