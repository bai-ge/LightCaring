package com.carefor.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.carefor.util.Tools;

/**
 * Created by baige on 2018/4/15.
 */

public class BatteryReceiver extends BroadcastReceiver {
    private final static String TAG = BatteryReceiver.class.getCanonicalName();
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "action ="+action);
        Log.d(TAG, Tools.printBundle(bundle));
    }
}
