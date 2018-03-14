package com.carefor.callback;

import com.carefor.data.source.remote.ServerHelper;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by baige on 2018/2/12.
 *
 * 所有回调函数的基类
 */

public abstract class BaseCallBack implements ServerHelper.PrimaryCallBack, ServerHelper.CodeCallBack, ServerHelper.ComplexCallBack {
    private long timeout;
    private String id;

    private boolean hadResponse;

    private Timer mTimer;

    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            if(!hadResponse){
                timeout();
            }
        }
    };

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        if(timeout > 0){
            this.timeout = timeout;
            if(mTimer == null){
                mTimer = new Timer();
            }
            mTimer.schedule(mTimerTask, timeout);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // 停止定时器
    public void stopTimer(){
        if(mTimer != null){
            mTimer.cancel();
            // 一定设置为null，否则定时器不会被回收
            mTimer = null;
        }
    }

    public void drop(){
        stopTimer();
        CallbackManager.getInstance().remote(id);
    }

    public abstract void setResponseBinder(AbstractResponseBinder responseBinder);
}
