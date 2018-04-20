package com.carefor.drugalarm.alarm;

import com.carefor.BasePresenter;
import com.carefor.BaseView;
import com.carefor.data.entity.AlarmClock;


/**
 * Created by baige on 2018/4/20.
 */

public interface AlarmClockContract {

    interface Presenter extends BasePresenter {
        void finishEidit(AlarmClock alarmClock);
    }
    interface View extends BaseView<Presenter> {
        void showTip(String text);
        void showTitle(String title);

    }
}
