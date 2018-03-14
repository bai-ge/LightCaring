package com.carefor.callback;

import android.util.Log;

import com.carefor.data.entity.Housekeeping;
import com.carefor.data.source.remote.Parm;
import com.carefor.util.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/3/13.
 */

public class HousekeepingResponseBinder extends AbstractResponseBinder {
    private final static String TAG = HousekeepingResponseBinder.class.getCanonicalName();
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
                    if(codeNum == Parm.SUCCESS_CODE && jsonObject.has(Parm.DATA)){
                        JSONObject housekeepingJson;
                        try{
                            //判断是否是数组
                            JSONArray jsonArray = jsonObject.getJSONArray(Parm.DATA);
                            List<Housekeeping> housekeepings = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                housekeepingJson = jsonArray.getJSONObject(i);
                                Housekeeping housekeeping = Housekeeping.createByJson(housekeepingJson);
                                if(housekeeping != null){
                                    housekeepings.add(housekeeping);
                                }
                            }
                            if(housekeepings.size() > 0){
                                callBack.loadHousekeepings(housekeepings);
                            }else{
                                callBack.notFind();
                            }
                            return;
                        }catch (JSONException e){
                            Log.d(TAG, "data 不是数组"+e.getMessage());
                        }
                        housekeepingJson = jsonObject.getJSONObject(Parm.DATA);
                        Housekeeping housekeeping = Housekeeping.createByJson(housekeepingJson);
                        if(housekeeping != null){
                            callBack.loadHousekeeping(housekeeping);
                        }else{
                            callBack.notFind();
                        }
                    }else{
                        callBackCode(callBack, codeNum);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callBack.fail();
            }
        }
    }
}
