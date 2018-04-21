package com.carefor.jpush;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.model.LatLng;
import com.carefor.callback.BaseCallBack;
import com.carefor.callback.CallbackManager;
import com.carefor.callback.SeniorCallBack;
import com.carefor.data.entity.Location;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.data.source.remote.Parm;
import com.carefor.dropdetection.FallService;
import com.carefor.location.LocationService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by baige on 2018/3/14.
 */

public class JPushMessageProcess {

    private final static String TAG = JPushMessageProcess.class.getCanonicalName();

    public static void processAskLocation(final Context context, final JSONObject json, long sendTime) {
        final CacheRepository cacheRepository = CacheRepository.getInstance();
        final Repository repository = Repository.getInstance(LocalRepository.getInstance(context));
        final LocationService locationService = LocationService.getInstance(context);
        try {
            //返回收到确认
            final int receiveUid = json.getInt(Parm.ROUTER);

            String callback = new String();
            if (json.has(Parm.Callback)) {
                callback = json.getString(Parm.Callback);
            }
            JSONObject reback = new JSONObject();
            reback.put(Parm.TIME, String.valueOf(System.currentTimeMillis()));
            reback.put(Parm.Callback, callback);
            reback.put(Parm.MESSAGE_TYPE, String.valueOf(Parm.MSG_TYPE_BACK));
            repository.asynSendMessageTo(cacheRepository.who().getUid(), receiveUid, reback.toString(), new SeniorCallBack());

            //打开定位
            locationService.registerListener(TAG, new BDLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation bdLocation) {
                    Log.d(TAG, "接收到位置信息" + bdLocation);
                    String address = bdLocation.getAddrStr();
                    double lat = bdLocation.getLatitude();
                    double lng = bdLocation.getLongitude();
                    String title = bdLocation.getLocationDescribe();

                    boolean isShowNotification = false;
                    try{
                        if(json.has(Parm.SHOW_NOTIFICATION)){
                            isShowNotification = json.getBoolean(Parm.SHOW_NOTIFICATION);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }



                    JSONObject jsonContent = new JSONObject();
                    try {
                        jsonContent.put(Parm.MESSAGE_TYPE, String.valueOf(Parm.MSG_TYPE_LOCATION));
                        jsonContent.put(Parm.LOCATION, address);
                        jsonContent.put(Parm.JWD, lng + "," + lat);
                        jsonContent.put(Parm.TITLE, title);
                        jsonContent.put(Parm.TIME, String.valueOf(System.currentTimeMillis()));
                        jsonContent.put(Parm.NAME, CacheRepository.getInstance().who().getName());
                        jsonContent.put(Parm.ACCURACY, String.valueOf(bdLocation.getRadius()));
                        jsonContent.put(Parm.BATTERY_PERCENT, String.valueOf(CacheRepository.getInstance().getBatteryPercent()));
                        Log.d("battery", CacheRepository.getInstance().getBatteryPercent()+"");
                        jsonContent.put(Parm.FROM, String.valueOf(cacheRepository.who().getUid()));
                        jsonContent.put(Parm.TO, String.valueOf(receiveUid));
                        if (json.has(Parm.Callback)) {
                            jsonContent.put(Parm.Callback, json.get(Parm.Callback));
                        }
                        SeniorCallBack seniorCallBack = new SeniorCallBack() {
                            @Override
                            public void success() {
                                super.success();
                                Log.d(TAG, "success()");
                                locationService.unregisterListener(TAG);
                            }

                            @Override
                            public void fail() {
                                super.fail();
                                Log.d(TAG, "fail()");
                            }

                            @Override
                            public void unknown() {
                                super.unknown();
                                Log.d(TAG, "unknown()");
                            }
                        };

                        if(isShowNotification){
                            repository.asynReplyLocation(0, "我要告诉监护人，我的位置在哪", cacheRepository.who().getUid(), receiveUid, jsonContent.toString(), seniorCallBack);
                        }else{
                            repository.asynSendMessageTo(CacheRepository.getInstance().who().getUid(), receiveUid, jsonContent.toString(), seniorCallBack);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
            locationService.requestLocation();
            locationService.autoPostLocation(context, 9 * 60 * 1000);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void processReceiveLocation(Context context, JSONObject json, long sendTime) {
        try {
            if (json.has(Parm.Callback)) {
                BaseCallBack baseCallBack = CallbackManager.getInstance().get(json.getString(Parm.Callback));
                if (baseCallBack != null && json.has(Parm.JWD)) {
                    String jwd = json.getString(Parm.JWD);
                    String[] list = jwd.split(",");
                    double lng = Double.valueOf(list[0]);
                    double lat = Double.valueOf(list[1]);

                    Location location = new Location();

                    if (json.has(Parm.LOCATION)) {
                        location.setArea(json.getString(Parm.LOCATION));
                    }
                    if (json.has(Parm.TITLE)) {
                        location.setDescription(json.getString(Parm.TITLE));
                    }
                    if(json.has(Parm.ACCURACY)){
                        location.setAccuracy((float) json.getDouble(Parm.ACCURACY));
                    }
                    if(json.has(Parm.BATTERY_PERCENT)){
                        location.setBatteryPercent((float) json.getDouble(Parm.BATTERY_PERCENT));
                    }
                    if(json.has(Parm.NAME)){
                        location.setName(json.getString(Parm.NAME));
                    }
                    location.setTime(sendTime);
                    location.setLatLng(new LatLng(lat, lng));
                    baseCallBack.loadLocation(location);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void processAskDropSwitch(Context context, JSONObject json, long sendTime) {
        int from = -1, to = -1;
        CacheRepository cacheRepository = CacheRepository.getInstance();
        Repository repository = Repository.getInstance(LocalRepository.getInstance(context));
        try {
            if (json.has(Parm.FROM)) {
                from = json.getInt(Parm.FROM);
            }
            if (json.has(Parm.TO)) {
                to = json.getInt(Parm.TO);
            }
            if (cacheRepository.who().getUid() != to) {
                return;
            }
            boolean isStart = FallService.getInstance(context).isRunning();
            JSONObject jsonMsg = new JSONObject();
            if (json.has(Parm.Callback)) {
                String callback = json.getString(Parm.Callback);
                jsonMsg.put(Parm.Callback, callback);
                jsonMsg.put(Parm.MESSAGE_TYPE, Parm.MSG_TYPE_DROP_SWITCH);
                jsonMsg.put(Parm.DROP_SWITCH, isStart);
                jsonMsg.put(Parm.FROM, cacheRepository.who().getUid());
                jsonMsg.put(Parm.TO, from);
                jsonMsg.put(Parm.TIME, String.valueOf(System.currentTimeMillis()));
                repository.asynSendMessageTo(to, from, jsonMsg.toString(), new SeniorCallBack());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //如果是监护人，则只用设置显示按钮
    //如果是被监护人，则设置显示按钮，设置显示，设置摔倒监听服务
    public static void processSetDrepSwitch(Context context, JSONObject json, long sendTime) {
        int from = -1, to = -1;
        CacheRepository cacheRepository = CacheRepository.getInstance();
        Repository repository = Repository.getInstance(LocalRepository.getInstance(context));
        try {
            if (json.has(Parm.FROM)) {
                from = json.getInt(Parm.FROM);
            }
            if (json.has(Parm.TO)) {
                to = json.getInt(Parm.TO);
            }
            if (cacheRepository.who().getUid() != to) {
                return;
            }
            boolean isStart = json.getBoolean(Parm.DROP_SWITCH);
            if (cacheRepository.who().getType() == 1) {
                if (json.has(Parm.Callback)) {
                    String callback = json.getString(Parm.Callback);
                    BaseCallBack baseCallBack = CallbackManager.getInstance().get(callback);
                    if(baseCallBack != null){
                        baseCallBack.receiveMessage(json.toString());
                    }
                }
            } else if (cacheRepository.who().getType() == 2) {
                //TODO 界面同时显示更新
                FallService fallService = FallService.getInstance(context);
                if (isStart && !fallService.isRunning()) {
                    fallService.start();
                } else if (!isStart && fallService.isRunning()) {
                    fallService.stop();
                }
                //回复状态信息
                JSONObject jsonMsg = new JSONObject();
                if (json.has(Parm.Callback)) {
                    String callback = json.getString(Parm.Callback);
                    jsonMsg.put(Parm.Callback, callback);
                    jsonMsg.put(Parm.MESSAGE_TYPE, Parm.MSG_TYPE_DROP_SWITCH);
                    jsonMsg.put(Parm.DROP_SWITCH, fallService.isRunning());
                    jsonMsg.put(Parm.FROM, cacheRepository.who().getUid());
                    jsonMsg.put(Parm.TO, from);
                    jsonMsg.put(Parm.TIME, String.valueOf(System.currentTimeMillis()));
                    repository.asynSendMessageTo(to, from, jsonMsg.toString(), new SeniorCallBack());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
