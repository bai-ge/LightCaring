package com.carefor.location;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.carefor.BaseApplication;
import com.carefor.mainui.R;

import java.text.DecimalFormat;

import static android.content.Context.SENSOR_SERVICE;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/1/2.
 */


public class LocationFragment extends Fragment implements SensorEventListener, LocationContract.View {

    private final static String TAG = LocationFragment.class.getCanonicalName();

    private LocationContract.Presenter mPresenter;

    private Toast mToast;

    private Handler mHandler;

    private MapView mMapView;

    private Button mBtnLocModel;
    private TextView mTxtLocation;
    private TextView mTxtLatLng;

    private boolean isFirstLoc = true; // 是否首次定位

    private MyLocationData mLocData;

    // 定位相关
    private LocationService locationService;
    private BaiduMap mBaiduMap;
    private BitmapDescriptor mCurrentMarker;

    private SensorManager mSensorManager;

    private MyLocationConfiguration.LocationMode mCurrentMode;

    private Double mLastX = 0.0;
    private int mCurrentDirection = 0; //方向 0~360
    private double mCurrentLat = 0.0;//维度
    private double mCurrentLon = 0.0; //经度
    private float mCurrentAccracy; //精度




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        mHandler = new Handler();

        //TODO 把定位服务分离出来
        locationService = ((BaseApplication)getActivity().getApplication()).locationService;
        locationService.registerListener(mListener);

        //获取locationservice实例，建议应用中只初始化1个location实例


        mSensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);//获取传感器管理服务
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        Log.d(TAG, "onCreate()");
    }

    public static LocationFragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public void setPresenter(LocationContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_location, container, false);
        initView(root);
        Log.d(TAG, "onCreateView()");
        return root;

    }

    private void initView(View root){
        // 地图初始化
        mMapView = (MapView) root.findViewById(R.id.map_view);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);


        mBtnLocModel = (Button) root.findViewById(R.id.btn_model);
        mBtnLocModel.setText("普通");
        mTxtLatLng = (TextView) root.findViewById(R.id.txt_latlng);
        mTxtLocation = (TextView) root.findViewById(R.id.txt_location);



        mBtnLocModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                locationService.start();

                switch (mCurrentMode) {
                    case NORMAL:
                        mBtnLocModel.setText("跟随");
                        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        break;
                    case COMPASS:
                        mBtnLocModel.setText("普通");
                        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder1 = new MapStatus.Builder();
                        builder1.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
                        break;
                    case FOLLOWING:
                        mBtnLocModel.setText("罗盘");
                        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[SensorManager.DATA_X];
        if (Math.abs(x - mLastX) > 1.0) {
            mCurrentDirection = (int) x;
            mLocData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(mLocData);
        }
        mLastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        mMapView.onResume();
        super.onResume();
        //为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
        locationService.start();
        mPresenter.start();

    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    public void onStop() {
        //取消注册传感器监听
        mSensorManager.unregisterListener(this);
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    public void onDestroy() {

        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void showTip(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mToast.setText(text);
                mToast.show();
            }
        });
    }

    /**
     * 定位SDK监听函数
     */
    private BDLocationListener mListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            mLocData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(mLocData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }

            DecimalFormat df = new DecimalFormat("#.00");
            mTxtLatLng.setText("经度："+ df.format(mCurrentLon) + "    维度："+df.format(mCurrentLat));

            StringBuffer stringBuffer = new StringBuffer(256);
            stringBuffer.append(location.getAddrStr());
            if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                stringBuffer.append("(");
                for (int i = 0; i < location.getPoiList().size() && i < 2; i++) {
                    Poi poi = (Poi) location.getPoiList().get(i);
                    stringBuffer.append(poi.getName() + ";");
                }
                stringBuffer.append(")");
            }
            mTxtLocation.setText(stringBuffer);
            Log.d(TAG, "onReceiveLocation()");
        }
    };



}

