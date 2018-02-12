package com.carefor.callback;

import com.carefor.data.source.remote.Parm;
import com.carefor.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/2/13.
 */

public class SimpleResponseBinder extends AbstractResponseBinder {

    @Override
    public void parse(String json, SeniorCallBack callBack) {
        checkNotNull(callBack);
        if(!Tools.isEmpty(json)){
            callBack.onResponse();
            try {
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has(Parm.CODE)){
                    int codeNum = jsonObject.getInt(Parm.CODE);

                    //TODO 可能去掉MSG
                    String text = jsonObject.getString(Parm.MSG);
                    if(!Tools.isEmpty(text)){
                        callBack.meaning(text);
                    }
                    callBackCode(callBack, codeNum);
                    //TODO 暂时仅处理返回码
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callBack.fail();
            }
        }
    }
}
