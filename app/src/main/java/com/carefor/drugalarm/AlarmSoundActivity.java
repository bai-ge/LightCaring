package com.carefor.drugalarm;

import android.support.v4.app.Fragment;


public class AlarmSoundActivity extends SingleFragmentDialogActivity {

    @Override
    protected Fragment createFragment() {
        return new AlarmSoundFragment();
    }

    @Override
    public void onBackPressed() {
        // 禁用back键
    }
}
