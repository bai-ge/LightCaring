package com.carefor.connect;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.carefor.data.entity.DeviceModel;
import com.carefor.data.entity.Transinformation;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.telephone.PhoneActivity;

/**
 * Created by baige on 2017/12/24.
 */

public class ConnectService extends Service {
    private final static String TAG = ConnectService.class.getCanonicalName();

    private ConnectBinder mBinder = new ConnectBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "启动服务");
        Connector connector = Connector.getInstance();
        connector.RegistConnectorListener("service",mOnConnectorListener);
        if(!connector.isConnectServer()){
            connector.afxConnectServer();
        }
        return super.onStartCommand(intent, flags, startId);
    }

   private Connector.OnConnectorListener mOnConnectorListener = new Connector.OnConnectorListener() {
       @Override
       public void connectSuccess() {

       }

       @Override
       public void loginSuccess() {

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
           intent.setClass(getApplicationContext(),PhoneActivity.class);
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
    public class ConnectBinder extends Binder{

    }

    @Override
    public void onDestroy() {
        Connector.getInstance().unRegistConnectorListener("service");
        super.onDestroy();
    }
}
