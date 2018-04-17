package com.carefor.callback;

import com.carefor.data.source.remote.ServerHelper;

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

    protected int waitingResponse = 1;
    protected Object waitLock = new Object();

    public void setWaitingResponse(int waitingResponse) {
        this.waitingResponse = waitingResponse;
    }

    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            if(!hadResponse){
                timeout();
                if(waitingResponse != 0){
                    onFinish();
                }
            }
            CallbackManager.getInstance().remote(id);
        }
    };

    public TimerTask getTimerTask(){
        return mTimerTask;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        if(timeout > 0){
            this.timeout = timeout;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void drop(){
        CallbackManager.getInstance().remote(id);
    }

    public abstract void setResponseBinder(AbstractResponseBinder responseBinder);
}
