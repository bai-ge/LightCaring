package com.carefor.dropdetection;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.carefor.BaseActivity;
import com.carefor.callback.SeniorCallBack;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.mainui.R;
import com.carefor.util.AudioPlayer;
import com.carefor.util.Tools;
import com.carefor.view.CircleProgressbar;
import com.carefor.view.MySlidingView;

/**
 * Created by baige on 2018/4/21.
 */

public class DropSoundActivity extends BaseActivity{

    private static final String TAG = DropSoundActivity.class.getCanonicalName();

    //屏幕控制
    private PowerManager.WakeLock mWakelock;

    /**
     * 声音管理
     */
    private AudioManager mAudioManager;

    private int mCurrentVolume;

    private TextView mTxtSliding;

    private CircleProgressbar mCircleProgressbar;

    private Repository mRepository;

    //滑动界面
    private MySlidingView mySlidingView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
//        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        // 画面出现在解锁屏幕上,显示,常亮
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        |WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.SCREEN_DIM_WAKE_LOCK, "DropSoundActivity");
        //TODO 根据intent读取必要数据
        Bundle bundle = getIntent().getExtras();

        mRepository = Repository.getInstance(LocalRepository.getInstance(getApplicationContext()));

        setContentView(R.layout.fragment_dropsound);
        initView();
        playRing();
    }

    private void initView() {
        mySlidingView = (MySlidingView) findViewById(R.id.my_sliding_view);
        mCircleProgressbar = (CircleProgressbar) findViewById(R.id.count_drown_progressbar);
        mTxtSliding = (TextView) findViewById(R.id.sliding_tip_tv);

        mySlidingView.setSlidingTipListener(new MySlidingView.SlidingTipListener() {
            @Override
            public void onSlidFinish() {
                // 执行关闭操作
               close();
            }
        });
        final AnimationDrawable animationDrawable = (AnimationDrawable) mTxtSliding.getCompoundDrawables()[0];
        // 直接启动动画，测试4.0模拟器没有动画效果
        mTxtSliding.post(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        });

//        mCircleProgressbar.setOutLineColor(Color.TRANSPARENT);
//        mCircleProgressbar.setInCircleColor(Color.parseColor("#656565"));
//        mCircleProgressbar.setProgressColor(Color.parseColor("#f9cf72"));
//        mCircleProgressbar.setProgressLineWidth(5);
//        mCircleProgressbar.setProgressType(CircleProgressbar.ProgressType.COUNT);
        mCircleProgressbar.setTimeMillis(12000);
        mCircleProgressbar.setProgressType(CircleProgressbar.ProgressType.COUNT_BACK);
        mCircleProgressbar.reStart();

        mCircleProgressbar.setCountdownProgressListener(1,mOnCountdownProgressListener);
        mCircleProgressbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDropInform();
                close();
            }
        });
    }

    /**
     * 播放铃声
     */
    private void playRing() {

        //TODO 根据设置播放url
        String uri = CacheRepository.getInstance().getAlertUri();
        boolean bVibrate = CacheRepository.getInstance().isAlertVibrate();
        if(Tools.isEmpty(uri)){
            AudioPlayer.getInstance(getApplicationContext()).playRaw(AudioPlayer.STREAM_ALARM,
                    R.raw.ring_weac_alarm_clock_default, true, bVibrate);
        }else{
            AudioPlayer.getInstance(getApplicationContext()).play(AudioPlayer.STREAM_ALARM, uri, true, bVibrate);
        }
    }

    private CircleProgressbar.OnCountdownProgressListener mOnCountdownProgressListener = new CircleProgressbar.OnCountdownProgressListener() {
        @Override
        public void onProgress(int what, int progress) {

        }

        @Override
        public void onFinish() {
            sendDropInform();
            close();
        }
    };

    private void sendDropInform(){
        if(CacheRepository.getInstance().who().getType() == 2){
            mRepository.asynInformTumble(CacheRepository.getInstance().who().getUid(), new SeniorCallBack(){
                @Override
                public void success() {
                    super.success();
                    showTip("跌倒信息发送成功");
                }

                @Override
                public void fail() {
                    super.fail();
                    showTip("跌倒信息发送失败");
                }
            });
        }
    }

    private long clickBackTime = 0;
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode== KeyEvent.KEYCODE_BACK){
            long now = System.currentTimeMillis();
            if(now - clickBackTime < 500){
                close();
            }else{
                clickBackTime = now;
                showTip("再按一次返回键退出并取消发送");
            }
            return true;//不执行父类点击事件
        }
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWakelock.acquire();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mWakelock != null){
            mWakelock.release();
        }
    }

    private void close(){
        mCircleProgressbar.stop();
        AudioPlayer.getInstance(getApplicationContext()).stop();
        FallService.getInstance(getApplicationContext()).start();
        finish();
    }
}
