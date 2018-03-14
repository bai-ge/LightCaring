package com.carefor.callback;

import android.util.Log;

import com.carefor.data.entity.Location;
import com.carefor.data.source.remote.Parm;
import com.carefor.util.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/2/24.
 */

public class LocationResponseBinder extends AbstractResponseBinder  {
    private static final String TAG = LocationResponseBinder.class.getCanonicalName();
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
                        JSONObject locJson;
                        try{
                            //判断是否是数组
                            JSONArray jsonArray = jsonObject.getJSONArray(Parm.DATA);
                            List<Location> locationList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                locJson = jsonArray.getJSONObject(i);
                                Location loc = Location.createByJson(locJson);
                                if(loc != null){
                                    locationList.add(loc);
                                }
                            }
                            if(locationList.size() > 0){
                                Collections.sort(locationList);//根据时间排序
                                callBack.loadLocations(locationList);
                            }else{
                                callBack.notFind();
                            }
                            return;
                        }catch (JSONException e){
                            Log.d(TAG, "data 不是数组"+e.getMessage());
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
