package com.carefor.drugalarm;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.carefor.broadcast.AlarmClockBroadcast;
import com.carefor.data.entity.AlarmClock;
import com.carefor.data.entity.DrugAlarmConstant;
import com.carefor.data.entity.DrugAlarmStatus;
import com.carefor.mainui.R;
import com.carefor.util.AudioPlayer;
import com.carefor.view.MySlidingView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class AlarmSoundFragment extends Fragment implements OnClickListener {

    /**
     * 当前时间
     */
    private TextView mTimeTv;

    /**
     * 闹钟实例
     */
    private AlarmClock mAlarmClock;

    /**
     * 线程运行flag
     */
    private boolean mIsRun = true;

    /**
     * 线程标记
     */
    private static final int UPDATE_TIME = 1;

    /**
     * 通知消息管理
     */
    private NotificationManagerCompat mNotificationManager;

    /**
     * 休息间隔
     */
    private int mNapInterval;

    /**
     * 休息次数
     */
    private int mNapTimes;

    /**
     * 是否点击按钮
     */
    private boolean mIsOnclick = false;

    /**
     * 休息已执行次数
     */
    private int mNapTimesRan;

    /**
     * 声音管理
     */
    private AudioManager mAudioManager;

    /**
     * 当前音量
     */
    private int mCurrentVolume;

    /**
     * 显示当前时间Handler
     */
    private ShowTimeHandler mShowTimeHandler;

    private String mCurrentTimeDisplay = "";

    /**
     * 显示当前时间
     */
    static class ShowTimeHandler extends Handler {
        private WeakReference<AlarmSoundFragment> mWeakReference;

        public ShowTimeHandler(AlarmSoundFragment AlarmSoundFragment) {
            mWeakReference = new WeakReference<>(AlarmSoundFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AlarmSoundFragment AlarmSoundFragment = mWeakReference.get();

            switch (msg.what) {
                case UPDATE_TIME:
                    AlarmSoundFragment.mTimeTv.setText(msg.obj.toString());
                    AlarmSoundFragment.mCurrentTimeDisplay =
                            AlarmSoundFragment.mTimeTv.getText().toString();
                    break;
            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        // 启动的Activity个数加1
        DrugAlarmStatus.sActivityNumber++;

        // 画面出现在解锁屏幕上,显示,常亮
        getActivity().getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAlarmClock = getActivity().getIntent()
                .getParcelableExtra(DrugAlarmConstant.ALARM_CLOCK);
        if (mAlarmClock != null) {
            // 取得休息间隔
            mNapInterval = mAlarmClock.getNapInterval();
            // 取得休息次数
            mNapTimes = mAlarmClock.getNapTimes();
        }
        // XXX:修正休息数
        // mNapTimes = 1000;
        // 休息已执行次数
        mNapTimesRan = getActivity().getIntent().getIntExtra(
                DrugAlarmConstant.NAP_RAN_TIMES, 0);
        // 播放铃声
        playRing();

        mNotificationManager = NotificationManagerCompat.from(getActivity());
        if (mAlarmClock != null) {
            // 取消下拉列表通知消息
            mNotificationManager.cancel(mAlarmClock.getId());
        }

        mShowTimeHandler = new ShowTimeHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_alarmsound, container,
                false);
        mTimeTv = (TextView) view.findViewById(R.id.ontime_time);
        // 显示现在时间
        mTimeTv.setText(new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date()));
        mCurrentTimeDisplay = mTimeTv.getText().toString();
        // 启动更新时间线程
        new Thread(new TimeUpdateThread()).start();

        // 标签
        TextView tagTv = (TextView) view.findViewById(R.id.ontime_tag);
        if (mAlarmClock != null) {
            tagTv.setText(mAlarmClock.getTag());
        } else {
            tagTv.setText(getString(R.string.alarm_error));
            tagTv.setTextColor(Color.RED);
        }

        // 休息按钮
        TextView napTv = (TextView) view.findViewById(R.id.ontime_nap);

        // 休息开启状态
        if (mAlarmClock != null && mAlarmClock.isNap() == 1) {
            // 当执行X次休息后隐藏休息按钮
            if (mNapTimesRan != mNapTimes) {
                // 设置休息
                napTv.setText(String.format(
                        getString(R.string.touch_here_nap), mNapInterval));
                napTv.setOnClickListener(this);
            } else {
                napTv.setVisibility(View.GONE);
            }
        } else {
            napTv.setVisibility(View.GONE);
        }



        // 滑动提示
        TextView slidingTipIv = (TextView) view.findViewById(R.id.sliding_tip_tv);
        final AnimationDrawable animationDrawable = (AnimationDrawable) slidingTipIv.getCompoundDrawables()[0];
        // 直接启动动画，测试4.0模拟器没有动画效果
        slidingTipIv.post(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        });

        MySlidingView mySlidingView = (MySlidingView) view.findViewById(R.id.my_sliding_view);
        mySlidingView.setSlidingTipListener(new MySlidingView.SlidingTipListener() {
            @Override
            public void onSlidFinish() {
                // 执行关闭操作
                finishActivity();
            }
        });
        
        return view;
    }





    @Override
    public void onStop() {
        super.onStop();
//        LogUtil.d(LOG_TAG, getActivity().toString() + "：onStop");
        // 当第二个闹钟响起时第一个闹钟需要进入休息或关闭闹钟（启动此Activity时加上
        // 【Intent.FLAG_ACTIVITY_CLEAR_TOP】flag 会自动关闭当前Activity，只有
        // 【Intent.FLAG_ACTIVITY_NEW_TASK】 flag的话，
        // 只是暂停，当第二个Activity结束后后会重新恢复显示）

//        LogUtil.d(LOG_TAG, getActivity().toString() + "：activityNumber: "
//                + DrugAlarmStatus.sActivityNumber);

        // 当点击关闭或者休息按钮或者画面关闭状态时或点击电源键闹钟响起会执行一次onStop()
        // 当点击按钮
        // if (mIsOnclick) {
        // // 点击按钮后，执行程序结束处理，故Activity数减1
        // DrugAlarmStatus.activityNumber--;
        // return;
        // }
        // // 第二个闹钟Activity启动
        // if (DrugAlarmStatus.activityNumber > 1) {
        // DrugAlarmStatus.activityNumber--;
        // // // 停止运行更新时间的线程
        // // mIsRun = false;
        // // // 休息
        // // nap();
        //
        // }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止运行更新时间的线程
        mIsRun = false;

        // 当没有点击按钮，则当前响铃被新闹钟任务杀死，开启休息
        if (!mIsOnclick) {
            // 休息
            nap();
        }

        // 当前只有一个Activity
        if (DrugAlarmStatus.sActivityNumber <= 1) {
            // 停止播放
            AudioPlayer.getInstance(getActivity()).stop();
        }

        // 启动的Activity个数减一
        DrugAlarmStatus.sActivityNumber--;

        // If null, all callbacks and messages will be removed.
        if (mShowTimeHandler != null) {
            mShowTimeHandler.removeCallbacksAndMessages(null);
        }

        // 复原手机媒体音量
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                mCurrentVolume, AudioManager.ADJUST_SAME);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 点击休息
            case R.id.ontime_nap:
                // 执行休息操作
                onClickNapButton();
                break;
        }
    }

    /**
     * 执行结束当前Activity操作
     */
    private void finishActivity() {
        // 点击按钮标记
        mIsOnclick = true;

        getActivity().finish();
        getActivity().overridePendingTransition(0, 0);
    }

    /**
     * 当点击休息按钮
     */
    private void onClickNapButton() {
        if (!(mNapTimesRan == mNapTimes)) {
            // 休息
            nap();
        }
        // 执行关闭操作
        finishActivity();
    }

    /**
     * 休息
     */
    @TargetApi(19)
    private void nap() {
        // 当休息执行了X次
        if (mNapTimesRan == mNapTimes || mAlarmClock == null) {
            return;
        }
        // 休息次数加1
        mNapTimesRan++;


        // 设置休息相关信息
        Intent intent = new Intent(getActivity(), AlarmClockBroadcast.class);
        intent.putExtra(DrugAlarmConstant.ALARM_CLOCK, mAlarmClock);
        intent.putExtra(DrugAlarmConstant.NAP_RAN_TIMES, mNapTimesRan);
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(),
                -mAlarmClock.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getActivity()
                .getSystemService(Activity.ALARM_SERVICE);
        // XXX
        // 下次响铃时间
        long nextTime = System.currentTimeMillis() + 1000 * 60 * mNapInterval;


        // 当前版本为19（4.4）或以上使用精准闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTime, pi);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, pi);
        }

        // 设置通知相关信息
        Intent it = new Intent(getActivity(), AlarmClockNapNotificationActivity.class);
        it.putExtra(DrugAlarmConstant.ALARM_CLOCK, mAlarmClock);
        // FLAG_UPDATE_CURRENT 点击通知有时不会跳转！！
        // FLAG_ONE_SHOT 清除列表只响应一个
        PendingIntent napCancel = PendingIntent.getActivity(getActivity(),
                mAlarmClock.getId(), it,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // 下拉列表通知显示的时间
        CharSequence time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(nextTime);

        // 通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
        // 设置PendingIntent
        Notification notification = builder.setContentIntent(napCancel)
                // 当清除下拉列表触发
                .setDeleteIntent(napCancel)
                // 设置下拉列表标题
                .setContentTitle(
                        String.format(getString(R.string.xx_naping),
                                mAlarmClock.getTag()))
                // 设置下拉列表显示内容
                .setContentText(String.format(getString(R.string.nap_to), time))
                // 设置状态栏显示的信息
                .setTicker(
                        String.format(getString(R.string.nap_time),
                                mNapInterval))
                // 设置状态栏（小图标）
                .setSmallIcon(R.drawable.ic_nap_notification)
                // 设置下拉列表（大图标）
                .setLargeIcon(
                        BitmapFactory.decodeResource(getResources(),
                                R.drawable.ic_launcher)).setAutoCancel(true)
                // 默认呼吸灯
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.FLAG_SHOW_LIGHTS)
                .build();
/*        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;*/

        // 下拉列表显示休息信息
        mNotificationManager.notify(mAlarmClock.getId(), notification);
    }

    /**
     * 播放铃声
     */
    private void playRing() {
        mAudioManager = (AudioManager) getActivity().getSystemService(
                Context.AUDIO_SERVICE);
        mCurrentVolume = mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        if (mAlarmClock != null) {
            // 设置铃声音量
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mAlarmClock.getVolume(), AudioManager.ADJUST_SAME);

            // 默认铃声
            if (mAlarmClock.getRingUrl().equals(DrugAlarmConstant.DEFAULT_RING_URL)
                    || TextUtils.isEmpty(mAlarmClock.getRingUrl())) {
                // 振动模式
                if (mAlarmClock.isVibrate() == 1) {
                    // 播放
                    AudioPlayer.getInstance(getActivity()).playRaw(
                            R.raw.ring_weac_alarm_clock_default, true, true);
                } else {
                    AudioPlayer.getInstance(getActivity()).playRaw(
                            R.raw.ring_weac_alarm_clock_default, true, false);
                }

                // 无铃声
            } else if (mAlarmClock.getRingUrl().equals(DrugAlarmConstant.NO_RING_URL)) {
                // 振动模式
                if (mAlarmClock.isVibrate() ==1) {
                    AudioPlayer.getInstance(getActivity()).stop();
                    AudioPlayer.getInstance(getActivity()).vibrate();
                } else {
                    AudioPlayer.getInstance(getActivity()).stop();
                }
            } else {
                // 振动模式
                if (mAlarmClock.isVibrate() ==1) {
                    AudioPlayer.getInstance(getActivity()).play(
                            mAlarmClock.getRingUrl(), true, true);
                } else {
                    AudioPlayer.getInstance(getActivity()).play(
                            mAlarmClock.getRingUrl(), true, false);
                }
            }
        } else {
            AudioPlayer.getInstance(getActivity()).playRaw(
                    R.raw.ring_weac_alarm_clock_default, true, true);
        }
    }

    /**
     * 显示时间的线程类
     */
    private class TimeUpdateThread implements Runnable {
        /**
         * 闹钟响铃时间
         */
        private int startedTime = 0;

        /**
         * 3分钟
         */
        private static final int TIME = 60 * 3;

        @Override
        public void run() {
            // Activity没有结束
            while (mIsRun) {


                try {
                    // 响铃XX分钟并且当前Activity没有被销毁进入休息
                    if (startedTime == TIME) {
                        // 休息开启状态
                        if (mAlarmClock != null && mAlarmClock.isNap() == 1) {
                            if (!getActivity().isFinishing()) {
                                onClickNapButton();
                                return;
                            } else {

                                return;
                            }
                        } else {
                            // 执行关闭操作
                            finishActivity();
                        }
                    }
                    Thread.sleep(1000);
                    startedTime++;
                    // 界面显示的时间
                    CharSequence currentTime = new SimpleDateFormat("HH:mm",
                            Locale.getDefault()).format(System
                            .currentTimeMillis());
                    if (mCurrentTimeDisplay.equals(currentTime)) {
                        continue;
                    }

                    Message msg = mShowTimeHandler.obtainMessage(UPDATE_TIME,
                            currentTime);
                    // 发送消息
                    mShowTimeHandler.sendMessage(msg);
                } catch (InterruptedException | NullPointerException e) {

                }
            }

        }
    }
}
