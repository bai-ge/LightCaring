package com.carefor.location;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.carefor.util.Tools;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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

    public static float[] EARTH_WEIGHT = {0.1f,0.2f,0.4f,0.6f,0.8f}; // 推算计算权重_地球

	/***
	 * 
	 * @param locationContext
	 */
	private LocationService(Context locationContext){
        mListenersMap = Collections.synchronizedMap(new LinkedHashMap<String, BDLocationListener>());
		synchronized (objLock) {
			if(client == null){
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
	
}
