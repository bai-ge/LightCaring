package com.carefor.telephone;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.carefor.mainui.R;
import com.carefor.view.CircleImageView;
import com.carefor.view.ProgressBall;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by baige on 2017/10/29.
 */

public class PhoneFragment extends Fragment implements PhoneContract.View , SensorEventListener {
    private final static String TAG = PhoneFragment.class.getCanonicalName();
    private PhoneContract.Presenter mPresenter;
    private Handler mHandler;
    private Toast mToast;

    private TextView mTextDelayTime;
    private CircleImageView mImg;
    private TextView mTextUserName;
    private TextView mTextAddress;
    private TextView mTextStatus;
    private EditText mEditLog;
    private ProgressBall mProgressBall;
    private Button mBtnHangUp;
    private Button mBtnPickUp;


    private Button mBtnSilence;
    private Button mBtnRecord;
    private Button mBtnLoudspeaker;
    private SimpleDateFormat mSimpleDateFormat;

    //距离感应器
    private SensorManager mSensorManager;

    private Sensor mSensor;

    private long clickTime;
    private int count;




    @Override
    public void onResume() {
        super.onResume();

        mSensorManager.registerListener(this , mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mPresenter.start();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");


       // mWakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Telephone");


        mSensorManager = (SensorManager)getContext().getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_phone, container, false);
        initView(root);
        return root;
    }

    private void initView(View root) {
        mTextDelayTime = (TextView) root.findViewById(R.id.network_speed);
        mImg = (CircleImageView) root.findViewById(R.id.user_img);
        mTextUserName = (TextView) root.findViewById(R.id.user_name);
        mTextAddress = (TextView) root.findViewById(R.id.address);
        mTextStatus = (TextView) root.findViewById(R.id.status);
        mEditLog = (EditText) root.findViewById(R.id.log);
        mProgressBall = (ProgressBall) root.findViewById(R.id.progress);
        mBtnHangUp = (Button) root.findViewById(R.id.btn_hang_up);
        mBtnPickUp = (Button) root.findViewById(R.id.btn_pick_up);

        mBtnSilence = (Button) root.findViewById(R.id.btn_silence);
        mBtnRecord = (Button) root.findViewById(R.id.btn_record);
        mBtnLoudspeaker = (Button) root.findViewById(R.id.btn_loudspeaker);

        mBtnLoudspeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAudioMode();
            }
        });

        mBtnHangUp.setOnClickListener(new View.OnClickListener() {//挂断电话
            @Override
            public void onClick(View v) {
                mPresenter.onHangUp();
            }
        });
        mBtnPickUp.setOnClickListener(new View.OnClickListener() {//接听电话
            @Override
            public void onClick(View v) {
                mBtnPickUp.setVisibility(View.GONE);
                mPresenter.onPickUp();
            }
        });

        //连击5次显示或隐藏日志窗口
        mImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(System.currentTimeMillis() - clickTime <= 500){
                    count ++;
                    if(count >= 5){
                        if(mEditLog != null && mTextDelayTime != null){
                            if(mEditLog.getVisibility() == View.VISIBLE){
                                mEditLog.setVisibility(View.INVISIBLE);
                                mTextDelayTime.setVisibility(View.INVISIBLE);
                                mTextAddress.setVisibility(View.INVISIBLE);
                            }else{
                                mEditLog.setVisibility(View.VISIBLE);
                                mTextDelayTime.setVisibility(View.VISIBLE);
                                mTextAddress.setVisibility(View.VISIBLE);
                            }
                        }
                        count = 0;
                    }
                }else{
                    count = 0;
                }
                clickTime = System.currentTimeMillis();
            }
        });
        //TODO 调试时默认显示
//        mEditLog.setVisibility(View.INVISIBLE);
//        mTextDelayTime.setVisibility(View.INVISIBLE);
//        mTextAddress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setPresenter(PhoneContract.Presenter presenter) {
        mPresenter = presenter;
    }

    public static PhoneFragment newInstance() {
        return new PhoneFragment();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float range = event.values[0];

        /*
        PhoneFragment: event.values[0] =5.0, [1] =374.0, [2] =0.0
        PhoneFragment: range =5.0,mSensor.getMaximumRange()= 30000.0
        * */
        Log.v(TAG, "event.values[0] ="+event.values[0]+", [1] ="+event.values[1]+", [2] ="+event.values[2]);
        Log.v(TAG, "range =" + range+",mSensor.getMaximumRange()= "+mSensor.getMaximumRange());
        if (range == 0) {
            showTip("息屏");
            Log.v(TAG, "息屏");
        } else {
            showTip("正常");
            Log.v(TAG, "正常");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.v(TAG, "accuracy = "+accuracy);
    }

    /**
     * 设置语音播放的模式
     *
     * @param ctx
     * @param mode
     */
    public void setAudioMode(Context ctx, int mode) {
        if (mode != AudioManager.MODE_NORMAL && mode != AudioManager.MODE_IN_COMMUNICATION) {
            return;
        }
        if(ctx == null){
            return;
        }
        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (mode == AudioManager.MODE_NORMAL) {
            audioManager.setSpeakerphoneOn(true);//打开扬声器
        } else if (mode == AudioManager.MODE_IN_COMMUNICATION) {
            audioManager.setSpeakerphoneOn(false);//关闭扬声器
        }
        audioManager.setMode(mode);
    }
    /**
     * 设置语音播放的模式
     *
     * @param mode
     */
    public void setAudioMode(int mode) {
        if (mode != AudioManager.MODE_NORMAL && mode != AudioManager.MODE_IN_COMMUNICATION) {
            return;
        }
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        if (mode == AudioManager.MODE_NORMAL) {
            audioManager.setSpeakerphoneOn(true);//打开扬声器
        } else if (mode == AudioManager.MODE_IN_COMMUNICATION) {
            audioManager.setSpeakerphoneOn(false);//关闭扬声器
        }
        audioManager.setMode(mode);
    }

    private void changeAudioMode() {
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        int mode = audioManager.getMode();
        if (mode == AudioManager.MODE_NORMAL) {
            audioManager.setSpeakerphoneOn(false);//关闭扬声器
            showTip("打开听筒");
        } else if (mode == AudioManager.MODE_IN_COMMUNICATION) {
            audioManager.setSpeakerphoneOn(true);//打开扬声器
            showTip("打开扬声器");
        }
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION - mode);
    }

    @Override
    public void showAddress(final String address) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTextAddress.setText(address);
            }
        });
    }

    @Override
    public void showTip(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mToast.setText(text);
                mToast.show();
            }
        });
    }

    @Override
    public void showStatus(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTextStatus.setText(text);
            }
        });
    }

    @Override
    public void showName(final String name) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTextUserName.setText(name);
            }
        });
    }

    @Override
    public void showLog(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mEditLog.append(mSimpleDateFormat.format(new Date()) + " " + text + "\n");
            }
        });
    }

    @Override
    public void showLog(final TelePhone.LogBean logBean) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mEditLog.append(mSimpleDateFormat.format(new Date(logBean.getTime())) + " " + logBean.getLog() + "\n");
            }
        });
    }

    @Override
    public void showDelayTime(final long delay) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTextDelayTime.setText(String.valueOf(delay) + "ms");
            }
        });
    }

    @Override
    public void clearLog() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mEditLog.setText("");
            }
        });
    }

    @Override
    public void hidePickUpBtn() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBtnPickUp.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void showProgress(final boolean isShow) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(isShow){
                    mProgressBall.setVisibility(View.VISIBLE);
                }else{
                    mProgressBall.setVisibility(View.INVISIBLE);
                }

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

    }



    @Override
    public void close() {
        setAudioMode(getContext(), AudioManager.MODE_NORMAL);
        Activity activity = getActivity();
        if(activity != null){
            activity.finish();
        }
    }
}
