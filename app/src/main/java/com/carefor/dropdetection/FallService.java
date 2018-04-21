package com.carefor.dropdetection;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.carefor.util.Tools;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static android.content.Context.SENSOR_SERVICE;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/3/5.
 */

public class FallService implements SensorEventListener{
    private final static String TAG = FallService.class.getCanonicalName();

    private static FallService INSTANCE = null;

    private Context mContext;

    private SensorManager mSensorManager;

    private Sensor mAccelerometer;

    private Map<String, OnFallServiceListener> mListenerMap;

    private boolean isRunning;

    private float mAccX, mAccY, mAccZ;

    private float mSvm;


    public  int svmCount = 0;
    public  float[] svmData;
    public  float[] svmFilteringData;
    public  int minLowThresholdValueIndex = -1;

    private float mHighThresholdValue = 25;

    private float mLowThresholdValue = 5;


    private FallService(@NonNull Context context){
        mContext = checkNotNull(context);

        mListenerMap = Collections.synchronizedMap(new LinkedHashMap<String, OnFallServiceListener>());

        svmData = new float[150];

        svmFilteringData = new float[150];

        //获取SensorManager，系统的传感器管理服务
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        //获取accelerometer加速度传感器
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    public static FallService getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (FallService.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new FallService(context);
                }
            }
        }
        return INSTANCE;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        if(isRunning != running){
            isRunning = running;
            mListener.onRunningChange(running);
        }
    }

    /*
           设置阈值
            */
    public void setThresholdValue(float highThreshold, float lowThreshold){
        this.mHighThresholdValue = highThreshold;
        this.mLowThresholdValue = lowThreshold;
        Log.d(TAG, highThreshold + "   " + lowThreshold);
    }

    public void start(){
        setRunning(true);
        reset();
    }
    public void stop(){
        setRunning(false);
    }
    public void reset(){
        svmCount = 0;
        minLowThresholdValueIndex = -1;

        for (int i = 0; i < svmData.length; i++){
            svmData[i] = 0;
        }
        //中值滤波
        for (int i = 0; i < svmFilteringData.length; i++){
            svmFilteringData[i] = 0;
        }
    }

    private  void setSvmData(float svm){
        svmData[svmCount] = svm;
        svmFilteringData[svmCount] = median(svmCount);//获取中值
        if(svmFilteringData[svmCount] <= mLowThresholdValue){
            minLowThresholdValueIndex = svmCount;
        }else{
            if(minLowThresholdValueIndex > svmCount && svmCount + svmData.length - minLowThresholdValueIndex > 10){
                minLowThresholdValueIndex = -1;
            }else if(svmCount - minLowThresholdValueIndex > 10){
                minLowThresholdValueIndex = -1;
            }
        }
        if(isFell(svmCount)){
            setRunning(false);
            mListener.onFall();
        }
        svmCount = (svmCount + 1) % svmData.length;
    }

    private  float median(int index){
        int a, b, c;
        float s1, s2, s3, temp;
        a = (svmData.length - 2 + index) % svmData.length;
        b = (svmData.length - 1 + index) % svmData.length;
        c = index;
        s1 = svmData[a];
        s2 = svmData[b];
        s3 = svmData[c];

        if(s1 > s2){
            temp = s1;
            s1 = s2;
            s2 = temp;
        }
        if(s2 > s3){
            temp = s2;
            s2 = s3;
            s3 = temp;
        }
        return s2;
    }

    private  boolean isFell(int index){
        if(minLowThresholdValueIndex != -1 && svmFilteringData[index] >= mHighThresholdValue){
            return true;
        }
        return false;
    }

    public void registerSensor(OnFallServiceListener listener, String key){
        if(mSensorManager != null && !Tools.isEmpty(key)){
            synchronized (mListenerMap) {
                mListenerMap.put(key, listener);
//                if (mListenerMap.size() == 1) {
//                    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
//                    Log.d(TAG, "FallSensorManager.registerSensor()");
//                }
            }
        }
    }
    public void unregisterSensor(String key){
        if(mSensorManager != null){
            synchronized (mListenerMap){
                mListenerMap.remove(key);
//                if(mListenerMap.size() == 0){
//                    mSensorManager.unregisterListener(this);
//                    Log.d(TAG, "FallSensorManager.unregisterSensor");
//                }
            }

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if(!isRunning){
                    return;
                }
                mAccX = event.values[0];
                mAccY = event.values[1];
                mAccZ = event.values[2];
                mSvm = (float) Math.sqrt(mAccX * mAccX + mAccY * mAccY + mAccZ * mAccZ);
                Log.d(TAG,mAccX + "  " + mAccY + "  " + mAccZ );
                mListener.onAccelerometerChanged(mAccX, mAccY, mAccZ);
                setSvmData(mSvm);
                break;
        }
    }

    private OnFallServiceListener mListener = new OnFallServiceListener() {
        @Override
        public void onAccelerometerChanged(float accX, float accY, float accZ) {
            Iterator<Map.Entry<String, OnFallServiceListener>> it = mListenerMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, OnFallServiceListener> entry = it.next();
                OnFallServiceListener listener = entry.getValue();
                if (listener != null) {
                    listener.onAccelerometerChanged(accX, accY, accZ);
                }
            }
        }

        @Override
        public void onFall() {
            Iterator<Map.Entry<String, OnFallServiceListener>> it = mListenerMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, OnFallServiceListener> entry = it.next();
                OnFallServiceListener listener = entry.getValue();
                if (listener != null) {
                    listener.onFall();
                }
            }
            if(mContext != null){
                Intent intent = new Intent(mContext, DropSoundActivity.class);
                mContext.startActivity(intent);
            }
        }

        @Override
        public void onRunningChange(boolean isRunning) {
            Iterator<Map.Entry<String, OnFallServiceListener>> it = mListenerMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, OnFallServiceListener> entry = it.next();
                OnFallServiceListener listener = entry.getValue();
                if (listener != null) {
                    listener.onRunningChange(isRunning);
                }
            }
        }
    };

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    interface OnFallServiceListener{
        void onAccelerometerChanged(float accX, float accY, float accZ);
        void onFall();
        void onRunningChange(boolean isRunning);
    }

}


