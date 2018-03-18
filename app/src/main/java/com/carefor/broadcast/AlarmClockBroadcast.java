package com.carefor.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.carefor.data.entity.AlarmClock;
import com.carefor.data.entity.DrugAlarmConstant;
import com.carefor.data.entity.DrugAlarmStatus;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.drugalarm.AlarmSoundActivity;
import com.carefor.util.AlarmUtil;

/**
 * Created by Ryoko on 2018/3/7.
 */

public class AlarmClockBroadcast extends BroadcastReceiver {

    /**
     * Log tag ：AlarmClockBroadcast
     */
    private static final String LOG_TAG = "AlarmClockBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmClock alarmClock = intent.getParcelableExtra(DrugAlarmConstant.ALARM_CLOCK);
        if (alarmClock != null) {
            // 单次响铃
            if (alarmClock.getWeeks() == null) {

                alarmClock.setOnOff(0);
                LocalRepository.getInstance(context).updateAlarmClock(alarmClock);

                Intent i = new Intent("com.carefor.AlarmClockOff");
                context.sendBroadcast(i);
            } else {
                // 重复周期闹钟
                AlarmUtil.startAlarmClock(context, alarmClock);
            }
        }

        // 小睡已执行次数
        int napTimesRan = intent.getIntExtra(DrugAlarmConstant.NAP_RAN_TIMES, 0);
        // 当前时间
        long now = SystemClock.elapsedRealtime();
        // 当上一次闹钟响起时间等于0
        if (DrugAlarmStatus.sLastStartTime == 0) {
            // 上一次闹钟响起时间等于当前时间
            DrugAlarmStatus.sLastStartTime = now;
            // 当上一次响起任务距离现在小于3秒时
        } else if ((now - DrugAlarmStatus.sLastStartTime) <= 3000) {

            /*LogUtil.d(LOG_TAG, "进入3秒以内再次响铃 小睡次数：" + napTimesRan + "距离时间毫秒数："
                    + (now - DrugAlarmStatus.sLastStartTime));
            LogUtil.d(LOG_TAG, "DrugAlarmStatus.strikerLevel："
                    + DrugAlarmStatus.sStrikerLevel);*/
//            LogUtil.d(LOG_TAG, "闹钟名：" + alarmClock.getTag());

            // 当是新闹钟任务并且上一次响起也为新闹钟任务时，开启了时间相同的多次闹钟，只保留一个其他关闭
            if ((napTimesRan == 0) & (DrugAlarmStatus.sStrikerLevel == 1)) {
                return;
            }
        } else {
            // 上一次闹钟响起时间等于当前时间
            DrugAlarmStatus.sLastStartTime = now;
        }

        Intent it = new Intent(context, AlarmSoundActivity.class);

        // 新闹钟任务
        if (napTimesRan == 0) {
            // 设置响起级别为闹钟
            DrugAlarmStatus.sStrikerLevel = 1;
            // 小睡任务
        } else {
            // 设置响起级别为小睡
            DrugAlarmStatus.sStrikerLevel = 2;
            // 小睡已执行次数
            it.putExtra(DrugAlarmConstant.NAP_RAN_TIMES, napTimesRan);
        }
        it.putExtra(DrugAlarmConstant.ALARM_CLOCK, alarmClock);
        // 清除栈顶的Activity
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(it);

    }

}
