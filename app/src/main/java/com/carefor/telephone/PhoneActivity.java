package com.carefor.telephone;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.carefor.BaseActivity;
import com.carefor.data.source.Repository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.mainui.R;
import com.carefor.util.ActivityUtils;


/**
 * Created by baige on 2017/10/29.
 */

public class PhoneActivity extends BaseActivity {
    private Toast mToast;

    private PowerManager.WakeLock mWakelock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //显示在锁屏界面上
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.act_common);
        mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);



        PhoneFragment phoneFragment =
                (PhoneFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (phoneFragment == null) {
            // Create the fragment
            phoneFragment = PhoneFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), phoneFragment, R.id.contentFrame);
        }
        PhonePresenter presenter = new PhonePresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), phoneFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.SCREEN_DIM_WAKE_LOCK, "Telephone");
        mWakelock.acquire();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mWakelock != null){
            mWakelock.release();
        }

    }
}
