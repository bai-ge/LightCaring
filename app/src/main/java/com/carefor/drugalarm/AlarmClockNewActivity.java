package com.carefor.drugalarm;

import android.support.v4.app.Fragment;

import com.carefor.mainui.R;

public class AlarmClockNewActivity extends SingleFragmentOrdinaryActivity {

    @Override
    protected Fragment createFragment() {
        return new AlarmClockNewFragment();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 按下返回键开启渐变缩小动画
        overridePendingTransition(0, R.anim.zoomout);
    }
}
