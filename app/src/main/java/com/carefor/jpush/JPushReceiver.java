package com.carefor.jpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

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
            JSONObject jsonObject = new JSONObject(message);
            Logger.d(TAG, "json : "+ jsonObject.toString());

            int msgType = 0;
            long time = System.currentTimeMillis();
            if(jsonObject.has(Parm.MESSAGE_TYPE)){
                msgType = jsonObject.getInt(Parm.MESSAGE_TYPE);
            }
            if(jsonObject.has(Parm.TIME)){
                time = Long.valueOf(jsonObject.getString(Parm.TIME));
            }

            switch (msgType){
                case 0:
                    break;
                case 1:
                    break;
                case Parm.MSG_TYPE_ASK_LOCATION://获取位置请求
                    JPushMessageProcess.processAskLocation(context, jsonObject, time);
                    break;
                case Parm.MSG_TYPE_LOCATION://得到位置反馈
                    JPushMessageProcess.processReceiveLocation(context, jsonObject, time);
                    break;
                case Parm.MSG_TYPE_DROP_ASK:
                    JPushMessageProcess.processAskDropSwitch(context, jsonObject, time);
                    break;
                case Parm.MSG_TYPE_DROP_SWITCH://监护人和被监护都可能会收到这个消息
                    JPushMessageProcess.processSetDrepSwitch(context, jsonObject, time);
                    break;
                default:
                    break;
            }

            if(msgType > Parm.MSG_TYPE_CUSTOM){

                if(jsonObject.has(Parm.Callback)){
                    String callbackId = jsonObject.getString(Parm.Callback);
                }
                Log.d(TAG, "收到自定义消息："+message);

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
