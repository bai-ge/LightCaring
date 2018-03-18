package com.carefor.drugalarm;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.carefor.data.entity.AlarmClock;
import com.carefor.data.entity.DrugAlarmConstant;
import com.carefor.util.AlarmUtil;

/**
 * Created by Ryoko on 2018/3/17.
 */

public class AlarmClockNapNotificationActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlarmClock alarmClock = getIntent().getParcelableExtra( DrugAlarmConstant.ALARM_CLOCK);
        // 关闭小睡
        AlarmUtil.cancelAlarmClock(this, -alarmClock.getId());
        finish();
    }
}
