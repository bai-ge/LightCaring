package com.carefor.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.carefor.callback.SeniorCallBack;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.data.source.remote.Parm;
import com.carefor.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 
 * @author baidu
 *
 */
public class LocationService {
    private final static String TAG = LocationService.class.getCanonicalName();

    private static LocationService INSTANCE = null;

    private Map<String, BDLocationListener> mListenersMap = null;

	private LocationClient client = null;
	private LocationClientOption mOption,DIYoption;
	private Object objLock = new Object();
    private long mLimiteTime;
    private Timer mTimer;

    private BDLocation mRecentLocation;
    private long mRefreshTime;

    private  Context mContext;





    private LinkedList<LocationEntity> mLocationList = new LinkedList<LocationEntity>(); // 存放历史定位结果的链表，最大存放当前结果的前5次定位结果

    public static float[] EARTH_WEIGHT = {0.1f,0.2f,0.4f,0.6f,0.8f}; // 推算计算权重_地球

	/***
	 * 
	 * @param locationContext
	 */
	private LocationService(Context locationContext){
        mListenersMap = Collections.synchronizedMap(new LinkedHashMap<String, BDLocationListener>());
		synchronized (objLock) {
			if(client == null){
                mContext = checkNotNull(locationContext);
				client = new LocationClient(locationContext);
                client.registerLocationListener(mListener);
				client.setLocOption(getDefaultLocationClientOption());
			}
		}
	}



    public static LocationService getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (LocationService.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new LocationService(context);
                }
            }
        }
        return INSTANCE;
    }

    public void autoPostLocation(final Context context, long time){
        if(time < 0){
            time = 9 * 60 * 1000;
        }
        mLimiteTime = System.currentTimeMillis() + time;
        if(mTimer != null){
            mTimer.cancel();
        }
        mTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Log.d(TAG, "自动上传位置");
                if(now > mLimiteTime ){
                    mTimer.cancel();
                    stop();
                }
                if(now - mRefreshTime >= 10000){
                    requestLocation();//更新位置
                }else{
                    JSONObject json = new JSONObject();
                    String loc = new String();
                    try {
                        json.put(Parm.LNG, mRecentLocation.getLongitude());
                        json.put(Parm.LAT, mRecentLocation.getLatitude());
                        json.put(Parm.LOCATION, mRecentLocation.getAddrStr());//大致位置
                        json.put(Parm.TITLE, mRecentLocation.getLocationDescribe());//详细位置
                        json.put(Parm.ACCURACY, mRecentLocation.getRadius());
                        json.put(Parm.BATTERY_PERCENT, CacheRepository.getInstance().getBatteryPercent());
                        json.put(Parm.TIME, String.valueOf(mRefreshTime));
                        loc = json.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(!Tools.isEmpty(loc)){
                        CacheRepository cacheRepository = CacheRepository.getInstance();
                        Repository repository = Repository.getInstance(LocalRepository.getInstance(context));
                        repository.asynUploadLocation(cacheRepository.who().getUid(), loc, mRefreshTime, new SeniorCallBack());
                    }
                }
            }
        };
        mTimer.schedule(timerTask,  0,  10000);
    }

	/***
	 * 
	 * @param listener
	 * @return
	 */
	
	public void registerListener(String key, BDLocationListener listener){
		if(!Tools.isEmpty(key) && listener != null){
            synchronized (mListenersMap){
                mListenersMap.put(key, listener);
            }
		}
	}
	
	public void unregisterListener(String key){
        if(!Tools.isEmpty(key)){
            synchronized (mListenersMap){
                mListenersMap.remove(key);
            }
        }
	}
	
	/***
	 * 
	 * @param option
	 * @return isSuccessSetOption
	 */
	public boolean setLocationOption(LocationClientOption option){
		boolean isSuccess = false;
		if(option != null){
			if(client.isStarted())
				client.stop();
			DIYoption = option;
			client.setLocOption(option);
		}
		return isSuccess;
	}

	/***
	 *
	 * @return DefaultLocationClientOption  默认O设置
	 */
	public LocationClientOption getDefaultLocationClientOption(){
		if(mOption == null){
			mOption = new LocationClientOption();
			mOption.setLocationMode(LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
			mOption.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
			mOption.setScanSpan(3000);//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
            mOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
		    mOption.setIsNeedLocationDescribe(true);//可选，设置是否需要地址描述
		    mOption.setNeedDeviceDirect(false);//可选，设置是否需要设备方向结果
		    mOption.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
		    mOption.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死   
		    mOption.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
		    mOption.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
		    mOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
			mOption.setOpenGps(true);//可选，默认false，设置是否开启Gps定位
            mOption.setPriority(LocationClientOption.GpsFirst); // 设置GPS优先
            mOption.setIsNeedAltitude(false);//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
		 
		}
		return mOption;
	}


	/**
	 *
	 * @return DIYOption 自定义Option设置
	 */
	public LocationClientOption getOption(){
		if(DIYoption == null) {
			DIYoption = new LocationClientOption();
		}
		return DIYoption;
	}

	public void requestLocation(){
        synchronized (objLock) {
            if(client != null){
                if(client.isStarted()){
                    client.requestLocation();

                }else{
                    client.start();
                }
            }
        }
    }

	public void start(){
		synchronized (objLock) {
            if(client != null && !client.isStarted()){
                client.start();
            }
		}
	}
	public void stop(){
		synchronized (objLock) {
			if(client != null && client.isStarted()){
				client.stop();
			}
		}
	}

	public boolean isStart() {
		return client.isStarted();
	}

	public boolean requestHotSpotState(){
		return client.requestHotSpotState();
	}

    @Override
    protected void finalize() throws Throwable {
        client.unRegisterLocationListener(mListener);
        super.finalize();
    }

    /***
     *61 ： GPS定位结果，GPS定位成功。
     *62 ： 无法获取有效定位依据，定位失败，请检查运营商网络或者wifi网络是否正常开启，尝试重新请求定位。
     *63 ： 网络异常，没有成功向服务器发起请求，请确认当前测试手机网络是否通畅，尝试重新请求定位。
     *65 ： 定位缓存的结果。
     *66 ： 离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果。
     *67 ： 离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果。
     *68 ： 网络连接失败时，查找本地离线定位时对应的返回结果。
     *161： 网络定位结果，网络定位定位成功。
     *162： 请求串密文解析失败。
     *167： 服务端定位失败，请您检查是否禁用获取位置信息权限，尝试重新请求定位。
     *502： key参数错误，请按照说明文档重新申请KEY。
     *505： key不存在或者非法，请按照说明文档重新申请KEY。
     *601： key服务被开发者自己禁用，请按照说明文档重新申请KEY。
     *602： key mcode不匹配，您的ak配置过程中安全码设置有问题，请确保：sha1正确，“;”分号是英文状态；且包名是您当前运行应用的包名，请按照说明文档重新申请KEY。
     *501～700：key验证失败，请按照说明文档重新申请KEY。
     */
    private BDLocationListener mListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation != null && (bdLocation.getLocType() == 161 || bdLocation.getLocType() == 66 || bdLocation.getLocType() == 61)) {
                Algorithm(bdLocation);//根据速度推算出结果
                if(bdLocation.getRadius() <= 50){
                    mRecentLocation =  bdLocation;
                    mRefreshTime = System.currentTimeMillis();
                }
                Iterator<Map.Entry<String, BDLocationListener>> it = mListenersMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, BDLocationListener> entry = it.next();
                    BDLocationListener listener = entry.getValue();
                    if (listener != null) {
                        listener.onReceiveLocation(bdLocation);
                    }
                }
            }
        }
    };

    /***
     * 平滑策略代码实现方法，主要通过对新定位和历史定位结果进行速度评分，
     * 来判断新定位结果的抖动幅度，如果超过经验值，则判定为过大抖动，进行平滑处理,若速度过快，
     * 则推测有可能是由于运动速度本身造成的，则不进行低速平滑处理 ╭(●｀∀´●)╯
     *
     * @param location
     * @return Bundle
     */
    private Bundle Algorithm(BDLocation location) {
        Bundle locData = new Bundle();
        double curSpeed = 0;
        if (mLocationList.isEmpty() || mLocationList.size() < 2) {
            LocationEntity temp = new LocationEntity();
            temp.location = location;
            temp.time = System.currentTimeMillis();
            locData.putInt("iscalculate", 0);
            if(location.getRadius() <= 50){ //精度
                mLocationList.add(temp);
            }
        } else {
            if (mLocationList.size() > 5)
                mLocationList.removeFirst();
            double score = 0;
            for (int i = 0; i < mLocationList.size(); ++i) {
                LatLng lastPoint = new LatLng(mLocationList.get(i).location.getLatitude(),
                        mLocationList.get(i).location.getLongitude());
                LatLng curPoint = new LatLng(location.getLatitude(), location.getLongitude());
                double distance = DistanceUtil.getDistance(lastPoint, curPoint);
                curSpeed = distance / (System.currentTimeMillis() - mLocationList.get(i).time) / 1000;
                score += curSpeed * LocationService.EARTH_WEIGHT[i];
            }
            if (score > 0.00000999 && score < 0.00005) { // 经验值,开发者可根据业务自行调整，也可以不使用这种算法
                location.setLongitude(
                        (mLocationList.get(mLocationList.size() - 1).location.getLongitude() + location.getLongitude())
                                / 2);
                location.setLatitude(
                        (mLocationList.get(mLocationList.size() - 1).location.getLatitude() + location.getLatitude())
                                / 2);
                locData.putInt("iscalculate", 1);
            } else {
                locData.putInt("iscalculate", 0);
            }
            LocationEntity newLocation = new LocationEntity();
            newLocation.location = location;
            newLocation.time = System.currentTimeMillis();
            if(location.getRadius() <= 50){ //精度
                mLocationList.add(newLocation);
            }
        }
        return locData;
    }

    /**
     * 封装定位结果和时间的实体类
     *
     * @author baidu
     *
     */
    class LocationEntity {
        BDLocation location;
        long time;
    }
}
