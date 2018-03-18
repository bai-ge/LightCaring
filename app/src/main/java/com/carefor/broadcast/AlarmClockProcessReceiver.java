package com.carefor.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.carefor.drugalarm.Event.AlarmClockUpdateEvent;
import com.carefor.util.OttoAppConfig;

/**
 * 单次闹钟响起，通过此BroadcastReceiver来实现多进程通信，更新闹钟开关
 *
 * Created by Ryoko on 2018/3/18.
 */

public class AlarmClockProcessReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        OttoAppConfig.getInstance().post(new AlarmClockUpdateEvent());
    }

}
