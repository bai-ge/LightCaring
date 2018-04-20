package com.carefor.dropdetection;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.carefor.BaseActivity;
import com.carefor.callback.SeniorCallBack;
import com.carefor.data.entity.User;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.data.source.remote.Parm;
import com.carefor.mainui.R;
import com.carefor.util.Tools;
import com.suke.widget.SwitchButton;

import org.json.JSONException;
import org.json.JSONObject;

public class DropDetectionActivity extends BaseActivity {

    private final static String TAG = DropDetectionActivity.class.getCanonicalName();
    private Toolbar mToolbar;
    private SwitchButton mSwitchButton;

    private TextView mUserName;

    private Repository mRepository;

    private FallService mFallService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop);



        mRepository = Repository.getInstance(LocalRepository.getInstance(getApplicationContext()));

        initView();

        mFallService = FallService.getInstance(getApplicationContext());
        mFallService.setThresholdValue(25, 5);
        mFallService.registerSensor(new FallService.OnFallServiceListener() {
            @Override
            public void onAccelerometerChanged(float accX, float accY, float accZ) {

            }
            @Override
            public void onFall() {
                showTip("老人跌倒");
                mSwitchButton.setChecked(false);
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

            @Override
            public void onRunningChange(final boolean isRunning) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwitchButton.setChecked(isRunning);
                    }
                });
            }
        }, TAG);

    }

    private void initView(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("老人跌倒检测");
        //为activity窗口设置活动栏
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        //设置返回图标
        actionBar.setHomeAsUpIndicator(0);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mUserName = (TextView) findViewById(R.id.user_name);
        mSwitchButton = (SwitchButton) findViewById(R.id.switch_drop);
        mSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                CacheRepository cacheRepository = CacheRepository.getInstance();
                User user = cacheRepository.who();
                if(user.getType() == 1){
                    ctrlDropSwitch(isChecked);
                }else if(user.getType() == 2){
                    //TODO 被监护人无法直接设置跌倒监测
                    if(isChecked){
                        mFallService.start();
                    }else{
                        mFallService.stop();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    private void start(){
        CacheRepository cacheRepository = CacheRepository.getInstance();
        User selUser = CacheRepository.getInstance().getSelectUser();
        if(cacheRepository.who().getType() == 1){
            mFallService.stop();
            //显示被监护人
            if(selUser != null && !Tools.isEmpty(selUser.getName())){
                mUserName.setText(selUser.getName() +"跌倒监测");
            }
            askDropSwitch();
        }else if(cacheRepository.who().getType() == 2){
            mSwitchButton.setChecked(mFallService.isRunning());
        }
    }



    private void askDropSwitch(){
        //询问跌倒监测状态
        CacheRepository cacheRepository = CacheRepository.getInstance();
        User selUser = CacheRepository.getInstance().getSelectUser();
        JSONObject jsonObject = new JSONObject();
        SeniorCallBack seniorCallBack = new SeniorCallBack(){
            @Override
            public void receiveMessage(String message) {
                super.receiveMessage(message);
                try {
                    JSONObject jsonObj = new JSONObject(message);
                    if(jsonObj.has(Parm.DROP_SWITCH)){
                        boolean isStart = jsonObj.getBoolean(Parm.DROP_SWITCH);
                        mSwitchButton.setChecked(isStart);
                        Log.d(TAG, "对方是否打开："+isStart);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        seniorCallBack.setId(Tools.ramdom());
        seniorCallBack.setTimeout(80000);
        try {
            jsonObject.put(Parm.Callback, seniorCallBack.getId());
            jsonObject.put(Parm.TIME, String.valueOf(System.currentTimeMillis()));
            jsonObject.put(Parm.MESSAGE_TYPE, Parm.MSG_TYPE_DROP_ASK);
            jsonObject.put(Parm.FROM, cacheRepository.who().getUid());
            jsonObject.put(Parm.TO, selUser.getUid());
            mRepository.asynSendMessageTo(cacheRepository.who().getUid(), selUser.getUid(), jsonObject.toString(), seniorCallBack);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void ctrlDropSwitch(boolean isStart){
        //设置被监护人跌倒监测状态
        CacheRepository cacheRepository = CacheRepository.getInstance();
        User selUser = CacheRepository.getInstance().getSelectUser();
        JSONObject jsonObject = new JSONObject();
        SeniorCallBack seniorCallBack = new SeniorCallBack(){
            @Override
            public void receiveMessage(String message) {
                super.receiveMessage(message);
                try {
                    JSONObject jsonObj = new JSONObject(message);
                    if(jsonObj.has(Parm.DROP_SWITCH)){
                        boolean isStart = jsonObj.getBoolean(Parm.DROP_SWITCH);
                        mSwitchButton.setChecked(isStart);
                        Log.d(TAG, "对方是否打开："+isStart);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        seniorCallBack.setId(Tools.ramdom());
        seniorCallBack.setTimeout(80000);
        try {
            jsonObject.put(Parm.Callback, seniorCallBack.getId());
            jsonObject.put(Parm.TIME, String.valueOf(System.currentTimeMillis()));
            jsonObject.put(Parm.MESSAGE_TYPE, Parm.MSG_TYPE_DROP_SWITCH);
            jsonObject.put(Parm.DROP_SWITCH, isStart);
            jsonObject.put(Parm.FROM, cacheRepository.who().getUid());
            jsonObject.put(Parm.TO, selUser.getUid());
            mRepository.asynSendMessageTo(cacheRepository.who().getUid(), selUser.getUid(), jsonObject.toString(), seniorCallBack);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
