package com.carefor.location;

import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.carefor.callback.SeniorCallBack;
import com.carefor.data.entity.Location;
import com.carefor.data.entity.User;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.data.source.remote.Parm;
import com.carefor.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/1/2.
 */

public class LocationPresenter implements LocationContract.Presenter {
    private static final String TAG = LocationPresenter.class.getCanonicalName();
    private Repository mRepositor;
    private LocationFragment mFragment;
    private int defalutNum = 100000000;
    private Timer mTimer;


    public LocationPresenter(Repository repository, LocationFragment locationFragment) {
        mRepositor = checkNotNull(repository);
        mFragment = checkNotNull(locationFragment);
        locationFragment.setPresenter(this);
    }

    @Override
    public void start() {

        //检查用户身份
        User user = CacheRepository.getInstance().who();
        if(user != null){
            if(user.getType() == 1){
                mFragment.isGuardian(true);
                //30秒请求一次位置信息
                if(mTimer == null){
                    mTimer = new Timer();
                }
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        loadLocation();
                    }
                }, 0, 1000 * 30);
            }else if(user.getType() == 2){
                mFragment.isGuardian(false);
            }
        }else{
            mFragment.isGuardian(true);
            //30秒请求一次位置信息
            if(mTimer == null){
                mTimer = new Timer();
            }
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    loadLocation();
                }
            }, 0, 1000 * 30);
        }

    }
    public void stop(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void askLocation() {
        mFragment.showInform("正在请求打开被监护人设备定位功能……");
        final CacheRepository cacheRepository = CacheRepository.getInstance();
        SeniorCallBack seniorCallBack = new SeniorCallBack() {
            @Override
            public void success() {
                super.success();
                mFragment.showTip("发送成功");
            }

            @Override
            public void timeout() {
                super.timeout();
                mFragment.showTip("发送超时");
            }

            @Override
            public void unknown() {
                super.unknown();
                mFragment.showTip("未知错误");
            }

            @Override
            public void error(Exception e) {
                super.error(e);
                mFragment.showTip("错误：" + e.getMessage());
            }

            @Override
            public void loadLocation(Location loc) {
                super.loadLocation(loc);
                mFragment.showInform("被监护人设备已经成功开启定位功能");
                mFragment.showPLocation(loc);
                cacheRepository.setShowNotification(true);
                mFragment.showTip("收到位置信息");
            }
        };
        seniorCallBack.setId(Tools.ramdom());
        seniorCallBack.setTimeout(80000);

        JSONObject content = new JSONObject();
        try {
            content.put(Parm.MESSAGE_TYPE, String.valueOf(2));
            content.put(Parm.ROUTER, String.valueOf(cacheRepository.who().getUid()));
            content.put(Parm.Callback, seniorCallBack.getId());
            content.put(Parm.SHOW_NOTIFICATION, String.valueOf(!cacheRepository.isShowNotification()));
            content.put(Parm.FROM, String.valueOf(cacheRepository.who().getUid()));
            content.put(Parm.TO, String.valueOf(cacheRepository.getSelectUser().getUid()));
            Log.d(TAG, "content" + content.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(cacheRepository.isShowNotification()){
            mRepositor.asynSendMessageTo(cacheRepository.who().getUid(), cacheRepository.getSelectUser().getUid(), content.toString(), seniorCallBack);
        }else{
            mRepositor.asynAskLocation(defalutNum++, "我需要获知被监护人位置的一条指令", cacheRepository.who().getUid(), cacheRepository.getSelectUser().getUid(), content.toString(), seniorCallBack);
        }

    }

    @Override
    public void loadLocation() {
        mFragment.showInform("正在获取被监护人位置……");
        List<Location> locationList = CacheRepository.getInstance().getDesLocation();
        if (locationList.size() == 0 ||
                System.currentTimeMillis() - locationList.get(locationList.size() - 1).getTime() >= 1000 * 60 * 30) { //大于半小时
            Log.d(TAG, "根据ID查询位置");
            locationList.clear();
            CacheRepository.getInstance().getSelPoints().clear();

            mRepositor.asynSearchLocationByById(CacheRepository.getInstance().getSelectUser().getUid(), new SeniorCallBack() {

                @Override
                public void success() {
                    super.success();
                    mFragment.showInform("加载成功");
                }

                @Override
                public void fail() {
                    super.fail();
                    askLocation();
                }

                @Override
                public void notFind() {
                    super.notFind();
                    askLocation();
                }

                @Override
                public void timeout() {
                    super.timeout();
                    mFragment.showTip("发送超时");
                }

                @Override
                public void unknown() {
                    super.unknown();
                    mFragment.showTip("未知错误");
                }

                @Override
                public void error(Exception e) {
                    super.error(e);
                    mFragment.showTip("错误：" + e.getMessage());
                }

                @Override
                public void loadLocation(Location loc) {
                    super.loadLocation(loc);
                    mFragment.showPLocation(loc);
                    mFragment.showTip("收到位置信息");
                }

                @Override
                public void loadLocations(List<Location> locationList) {
                    super.loadLocations(locationList);
                    mFragment.showInform("加载被监护人位置成功");
                    Log.d(TAG, "Location first" + locationList.get(0).toString());
                    Log.d(TAG, "Location last" + locationList.get(locationList.size() - 1).toString());
                    if(System.currentTimeMillis() - locationList.get(locationList.size()-1).getTime() > 1000 * 60 * 10 ){
                        askLocation();
                    }
                    filterLocation(locationList);

                }
            });
        } else {
            Log.d(TAG, "根据时间查询位置");
            mRepositor.asynSearchLocationByTime(CacheRepository.getInstance().getSelectUser().getUid(), locationList.get(locationList.size()-1).getTime(), new SeniorCallBack() {

                @Override
                public void success() {
                    super.success();
                    mFragment.showInform("加载成功");
                }

                @Override
                public void timeout() {
                    super.timeout();
                    mFragment.showTip("发送超时");
                }

                @Override
                public void unknown() {
                    super.unknown();
                    mFragment.showTip("未知错误");
                }

                @Override
                public void error(Exception e) {
                    super.error(e);
                    mFragment.showTip("错误：" + e.getMessage());
                }

                @Override
                public void loadLocation(Location loc) {
                    super.loadLocation(loc);
                    mFragment.showPLocation(loc);
                    mFragment.showTip("收到位置信息");
                }

                @Override
                public void loadLocations(List<Location> locationList) {
                    super.loadLocations(locationList);
                    mFragment.showInform("加载被监护人位置成功");
                    Log.d(TAG, "Location first" + locationList.get(0).toString());
                    Log.d(TAG, "Location last" + locationList.get(locationList.size() - 1).toString());
                    if(System.currentTimeMillis() - locationList.get(locationList.size()-1).getTime() > 1000 * 60 * 10 ){
                        askLocation();
                    }
                    filterLocation(locationList);
                }
            });
        }
    }
    private void filterLocation(List<Location> locationList){
        //更新本地数据
        List<Location> locList = CacheRepository.getInstance().getDesLocation();
        for (int i = 0; i < locationList.size(); i++) {
            locList.add(locationList.get(i));
            if (locList.size() > 500) {
                locList.remove(0);
            }
        }
        Collections.sort(locList);//根据时间排序

        //过滤多余节点
        //过滤超时节点
        int size = locList.size();
        int day = 1000 * 60 * 60 * 24;
        long limitTime = locList.get(size-1).getTime();
        List<LatLng> points = CacheRepository.getInstance().getSelPoints();
        points.clear();
        for (int i = 0; i < locList.size(); i++) {
            LatLng loc = locList.get(i).getLatLng();
            if(locList.get(i).getTime() + day < limitTime){
                continue;
            }

            if(points.size() > 2){
                LatLng lastPoint = points.get(points.size() - 1);
                LatLng lessPoint = points.get(points.size() - 2);
                double distance = DistanceUtil.getDistance(lastPoint, lessPoint);
                if(distance <= 2){
                    points.remove(points.size() - 1);
                }
            }
            points.add(loc);
            if(points.size() >= 200){
                points.remove(0);
            }
        }
        mFragment.showLocationDialog(locList.get(size-1));//仅展示窗口
        mFragment.showTargetTrack(points);//展示轨迹
    }
}
