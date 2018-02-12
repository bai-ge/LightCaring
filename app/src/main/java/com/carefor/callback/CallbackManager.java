package com.carefor.callback;

import com.carefor.util.Tools;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by baige on 2018/2/13.
 */

public class CallbackManager {
    private final static String TAG = CallbackManager.class.getCanonicalName();

    private static CallbackManager INSTANCE = null;

    private Map<String, BaseCallBack> mCallBackMap;

    private CallbackManager(){
        mCallBackMap = Collections.synchronizedMap(new LinkedHashMap<String, BaseCallBack>());
    }

    public static CallbackManager getInstance() {
        if (INSTANCE == null) {
            synchronized (CallbackManager.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new CallbackManager();
                }
            }
        }
        return INSTANCE;
    }

    public void put(BaseCallBack baseCallBack){
        if(baseCallBack != null && !Tools.isEmpty(baseCallBack.getId())){
            synchronized (mCallBackMap){
                mCallBackMap.put(baseCallBack.getId(), baseCallBack);
            }
        }
    }

    public BaseCallBack get(String id){
        if(!Tools.isEmpty(id)){
           return mCallBackMap.get(id);
        }
        return null;
    }
}
