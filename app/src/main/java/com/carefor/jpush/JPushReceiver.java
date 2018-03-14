package com.carefor.jpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.model.LatLng;
import com.carefor.callback.BaseCallBack;
import com.carefor.callback.CallbackManager;
import com.carefor.callback.SeniorCallBack;
import com.carefor.data.entity.Location;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.data.source.remote.Parm;
import com.carefor.location.LocationActivity;
import com.carefor.location.LocationService;
import com.example.jpushdemo.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by baige on 2018/2/18.
 */

public class JPushReceiver extends BroadcastReceiver {
    private static final String TAG = JPushReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            Logger.d(TAG, "[JPushReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));

            if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
                String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
                Logger.d(TAG, "[JPushReceiver] 接收Registration Id : " + regId);
                //send the Registration Id to your server...

            } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
                Logger.d(TAG, "[JPushReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
                processCustomMessage(context, bundle);

            } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
                Logger.d(TAG, "[JPushReceiver] 接收到推送下来的通知");
                int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
                Logger.d(TAG, "[JPushReceiver] 接收到推送下来的通知的ID: " + notifactionId);

            } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
                Logger.d(TAG, "[JPushReceiver] 用户点击打开了通知");

                //打开自定义的Activity
                Intent i = new Intent(context, LocationActivity.class);
                i.putExtras(bundle);
                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
                context.startActivity(i);

            } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
                Logger.d(TAG, "[JPushReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
                //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

            } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
                boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
                Logger.w(TAG, "[JPushReceiver]" + intent.getAction() +" connected state change to "+connected);
            } else {
                Logger.d(TAG, "[JPushReceiver] Unhandled intent - " + intent.getAction());
            }
        } catch (Exception e){

        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            }else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
                if (TextUtils.isEmpty(bundle.getString(JPushInterface.EXTRA_EXTRA))) {
                    Logger.i(TAG, "This message has no Extra data");
                    continue;
                }

                try {
                    JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
                    Iterator<String> it =  json.keys();

                    while (it.hasNext()) {
                        String myKey = it.next();
                        sb.append("\nkey:" + key + ", value: [" +
                                myKey + " - " +json.optString(myKey) + "]");
                    }
                } catch (JSONException e) {
                    Logger.e(TAG, "Get message extra JSON error!");
                }

            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }

    //send location to server
    private void processCustomMessage(final Context context, Bundle bundle) {
        final LocationService locationService = LocationService.getInstance(context);
        String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
        try {
            final JSONObject jsonObject = new JSONObject(message);
            Logger.d(TAG, "json : "+ jsonObject.toString());

            int msgType = 0;
            long time = 0;
            if(jsonObject.has(Parm.MESSAGE_TYPE)){
                msgType = jsonObject.getInt(Parm.MESSAGE_TYPE);
            }
            if(jsonObject.has(Parm.TIME)){
                time = Long.valueOf(jsonObject.getString(Parm.TIME));
            }
            if(msgType > Parm.MSG_TYPE_CUSTOM){

                if(jsonObject.has(Parm.Callback)){
                    String callbackId = jsonObject.getString(Parm.Callback);
                }
                Log.d(TAG, "收到自定义消息："+message);

            }
            switch (msgType){
                case 0:
                    break;
                case 1:
                    break;
                case 2://获取位置请求
                    final int receiveUid = jsonObject.getInt(Parm.ROUTER);
                    locationService.registerListener(TAG, new BDLocationListener() {
                        @Override
                        public void onReceiveLocation(BDLocation bdLocation) {
                            Log.d(TAG, "接收到位置信息"+bdLocation);
                            String address = bdLocation.getAddrStr();
                            double  lat = bdLocation.getLatitude();
                            double lng = bdLocation.getLongitude();
                            String title = bdLocation.getLocationDescribe();
                            JSONObject jsonContent = new JSONObject();

                            try {
                                jsonContent.put(Parm.MESSAGE_TYPE, String.valueOf(3));
                                jsonContent.put(Parm.LOCATION, address);
                                jsonContent.put(Parm.JWD, lng +","+ lat );
                                jsonContent.put(Parm.TITLE, title);
                                jsonContent.put(Parm.TIME, String.valueOf(System.currentTimeMillis()));
                                if(jsonObject.has(Parm.Callback)){
                                    jsonContent.put(Parm.Callback, jsonObject.get(Parm.Callback));
                                }
                                Repository repository = Repository.getInstance(LocalRepository.getInstance(context));
                                CacheRepository cacheRepository = CacheRepository.getInstance();
                                SeniorCallBack seniorCallBack = new SeniorCallBack(){
                                    @Override
                                    public void success() {
                                        super.success();
                                        Log.d(TAG, "success()");
                                        locationService.unregisterListener(TAG);
                                    }

                                    @Override
                                    public void fail() {
                                        super.fail();
                                        Log.d(TAG, "fail()");
                                    }

                                    @Override
                                    public void unknown() {
                                        super.unknown();
                                        Log.d(TAG, "unknown()");
                                    }
                                };
                                repository.asynReplyLocation(0, "我要告诉监护人，我的位置在哪", cacheRepository.who().getUid(), receiveUid, jsonContent.toString(), seniorCallBack );

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    locationService.requestLocation();
                    locationService.autoPostLocation(context, 9 * 60 * 1000);
                    break;
                case 3://得到位置反馈
                    if(jsonObject.has(Parm.Callback)){
                        BaseCallBack baseCallBack = CallbackManager.getInstance().get(jsonObject.getString(Parm.Callback));
                        if(baseCallBack != null && jsonObject.has(Parm.JWD)){
                            String jwd = jsonObject.getString(Parm.JWD);
                            String [] list = jwd.split(",");
                            double lng = Double.valueOf(list[0]);
                            double  lat = Double.valueOf(list[1]);

                            StringBuffer des = new StringBuffer();

                            if(jsonObject.has(Parm.LOCATION)){
                                des.append(jsonObject.get(Parm.LOCATION)+"\n");
                            }
                            if(jsonObject.has(Parm.TITLE)){
                                des.append(jsonObject.get(Parm.TITLE));
                            }

                            Location location = new Location();
                            location.setTime(time);
                            location.setLatLng(new LatLng(lat, lng));
                            location.setDescription(des.toString());
                            baseCallBack.loadLocation(location);
                        }

                    }

                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private BDLocationListener mLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

        }
    };
}
