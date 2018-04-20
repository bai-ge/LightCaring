package com.carefor.drugalarm.alarm;

import com.carefor.data.entity.AlarmClock;
import com.carefor.data.source.Repository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/4/20.
 */

public class AlarmClockEditPresenter implements AlarmClockContract.Presenter{

    private Repository mRepository;
    private AlarmClockEditFragment mFragment;
    private boolean isNewAlarmClock;

    public AlarmClockEditPresenter(Repository instance, AlarmClockEditFragment fragment) {
        this.mRepository = checkNotNull(instance);
        this.mFragment = checkNotNull(fragment);
        fragment.setPresenter(this);
    }

    @Override
    public void start() {
        if(isNewAlarmClock){
            mFragment.showTitle("新建闹钟");
        }else{
            mFragment.showTitle("编辑闹钟");
        }
    }

    public void setNewAlarmClock(boolean newAlarmClock) {
        isNewAlarmClock = newAlarmClock;
    }

    @Override
    public void finishEidit(AlarmClock alarmClock) {
        if(alarmClock == null){
            return;
        }
        //TODO 新建闹钟实例，保存，加入系统计时
        if(isNewAlarmClock){

        }else{
            //TODO 更新闹钟实例，取消旧系统计时，重新加入系统计时
        }
    }
}
