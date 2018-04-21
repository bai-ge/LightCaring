package com.carefor.jpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.carefor.broadcast.SendMessageBroadcast;
import com.carefor.connect.ConnectedByUDP;
import com.carefor.connect.NetServerManager;
import com.carefor.util.Tools;
import com.squareup.leakcanary.ServiceHeapDumpListener;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by baige on 2018/3/27.
 */

public class IPushReceiver extends BroadcastReceiver {
    private final static String TAG = IPushReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();
        Log.d(TAG, action+"接收到数据:"+printBundle(bundle));
        if(action.equals(SendMessageBroadcast.ACTION_RECEIVE_MSG)){
            if (bundle.containsKey(SendMessageBroadcast.KEY_RECEIVE_MSG)) {
                String msg = bundle.getString(SendMessageBroadcast.KEY_RECEIVE_MSG);
                if(!Tools.isEmpty(msg)){
                    try {
                        JSONObject jsonObject = new JSONObject(msg);
                        MessageProcess.receive(context, jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else if(action.equals(SendMessageBroadcast.ACTION_SEND_MSG_FIAL)){
            if (bundle.containsKey(SendMessageBroadcast.KEY_SEND_MSG)) {
                String msg = bundle.getString(SendMessageBroadcast.KEY_RECEIVE_MSG);
                String serverIP = bundle.getString(SendMessageBroadcast.KEY_SERVER_IP);
                if(!Tools.isEmpty(msg)){
                    ConnectedByUDP connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(serverIP, 12059);
                    connectedByUDP.sendString(msg);
                }
            }
        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
        }
        return sb.toString();
    }
}
