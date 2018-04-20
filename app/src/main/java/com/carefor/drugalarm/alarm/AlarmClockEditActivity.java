package com.carefor.drugalarm.alarm;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.carefor.BaseActivity;
import com.carefor.data.entity.DrugAlarmConstant;
import com.carefor.data.source.Repository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.mainui.R;
import com.carefor.util.ActivityUtils;
import com.carefor.util.Tools;

/**
 * Created by Ryoko on 2018/3/14.
 */

public class AlarmClockEditActivity extends BaseActivity {

    private final static String TAG = AlarmClockEditActivity.class.getCanonicalName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_common);
        AlarmClockEditFragment fragment = (AlarmClockEditFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if(fragment == null){
            fragment =  AlarmClockEditFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment, R.id.contentFrame);
        }
        AlarmClockEditPresenter alarmClockEditPresenter = new AlarmClockEditPresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), fragment);

        Bundle bundle = getIntent().getExtras();
        Log.d(TAG, Tools.printBundle(bundle));
        //TODO 优化，当有AlarmClock实例时，表示编辑，否则表示新建
        if(bundle.containsKey(DrugAlarmConstant.IS_NEW_ALARM_CLOCK)){
            alarmClockEditPresenter.setNewAlarmClock(bundle.getBoolean(DrugAlarmConstant.IS_NEW_ALARM_CLOCK));
        }else{
            alarmClockEditPresenter.setNewAlarmClock(true);
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 按下返回键开启移动退出动画
        overridePendingTransition(0, R.anim.move_out_bottom);
    }
}
