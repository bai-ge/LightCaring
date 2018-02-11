package com.carefor.location;

import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.Point;
import com.baidu.mapapi.utils.DistanceUtil;
import com.carefor.mainui.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
    private Button mBtnLoc;
    private TextView mTxtLocation;
    private TextView mTxtLatLng;

    private boolean isFirstLoc = true; // 是否首次定位

    private MyLocationData mLocData;

    private View mScaleView;
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



    private Polyline mPolyline;
    private PolylineOptions mOverlayOptions;
    private BitmapDescriptor mRedTexture;
    private List<LatLng> mPoints = new ArrayList<LatLng>();

    private LinkedList<LocationEntity> locationList = new LinkedList<LocationEntity>(); // 存放历史定位结果的链表，最大存放当前结果的前5次定位结果




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        mHandler = new Handler();

        locationService = LocationService.getInstance(getContext());
        locationService.registerListener(TAG, mListener);

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

        //视图显示完成时重新设置标尺的位置
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                resetScaleControlPosition();
            }
        });
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        mBtnLoc = (Button) root.findViewById(R.id.btn_location);
        mBtnLocModel = (Button) root.findViewById(R.id.btn_model);
        mTxtLatLng = (TextView) root.findViewById(R.id.txt_latlng);
        mTxtLocation = (TextView) root.findViewById(R.id.txt_location);
        mScaleView = root.findViewById(R.id.scale_view);

        mRedTexture = BitmapDescriptorFactory.fromAsset("icon_road_red_arrow.png");


        mBtnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationService.requestLocation();
            }
        });
        mBtnLocModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                    case COMPASS:
                        // mBtnLocModel.setText("跟随");
                        mBtnLocModel.setBackgroundResource(R.drawable.btn_follow);
                        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        break;
                    case FOLLOWING:
                       // mBtnLocModel.setText("罗盘");
                        mBtnLocModel.setBackgroundResource(R.drawable.btn_navigation);
                        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        break;
                    default:
                        break;
                }
                if(mPoints.size() > 0){
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(mPoints.get(mPoints.size() - 1));
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }
            }
        });
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int reason) {
            /*
            REASON_API_ANIMATION
            SDK导致的地图状态改变, 比如点击缩放控件、指南针图标

            REASON_DEVELOPER_ANIMATION
            开发者调用,导致的地图状态改变

            REASON_GESTURE
            用户手势触发导致的地图状态改变,比如双击、拖拽、滑动底图
            */
                switch (reason){
                    case REASON_API_ANIMATION:
                        break;
                    case REASON_DEVELOPER_ANIMATION:
                        break;
                    case REASON_GESTURE:
                        // mBtnLocModel.setText("普通");
                        mBtnLocModel.setBackgroundResource(R.drawable.btn_location);
                        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder1 = new MapStatus.Builder();
                        builder1.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
                        break;
                }
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
              resetScaleControlPosition();
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

    private void resetScaleControlPosition(){
        Rect mapRect = new Rect();
        mMapView.getGlobalVisibleRect(mapRect);
        Rect scaleRect = new Rect();
        mScaleView.getGlobalVisibleRect(scaleRect);
        mMapView.setScaleControlPosition(new android.graphics.Point(scaleRect.left - mapRect.left, scaleRect.top - mapRect.top));
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
        locationService.unregisterListener(TAG); //注销掉监听
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
            //连续瞄点
            Message locMsg = locHander.obtainMessage();
            Bundle locData;
            locData = Algorithm(location);
            if (locData != null) {
                locData.putParcelable("loc", location);
                locMsg.setData(locData);
                locHander.sendMessage(locMsg);
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
        if (locationList.isEmpty() || locationList.size() < 2) {
            LocationEntity temp = new LocationEntity();
            temp.location = location;
            temp.time = System.currentTimeMillis();
            locData.putInt("iscalculate", 0);
            locationList.add(temp);
        } else {
            if (locationList.size() > 5)
                locationList.removeFirst();
            double score = 0;
            for (int i = 0; i < locationList.size(); ++i) {
                LatLng lastPoint = new LatLng(locationList.get(i).location.getLatitude(),
                        locationList.get(i).location.getLongitude());
                LatLng curPoint = new LatLng(location.getLatitude(), location.getLongitude());
                double distance = DistanceUtil.getDistance(lastPoint, curPoint);
                curSpeed = distance / (System.currentTimeMillis() - locationList.get(i).time) / 1000;
                score += curSpeed * LocationService.EARTH_WEIGHT[i];
            }
            if (score > 0.00000999 && score < 0.00005) { // 经验值,开发者可根据业务自行调整，也可以不使用这种算法
                location.setLongitude(
                        (locationList.get(locationList.size() - 1).location.getLongitude() + location.getLongitude())
                                / 2);
                location.setLatitude(
                        (locationList.get(locationList.size() - 1).location.getLatitude() + location.getLatitude())
                                / 2);
                locData.putInt("iscalculate", 1);
            } else {
                locData.putInt("iscalculate", 0);
            }
            LocationEntity newLocation = new LocationEntity();
            newLocation.location = location;
            newLocation.time = System.currentTimeMillis();
            locationList.add(newLocation);

        }
        return locData;
    }

    /***
     * 接收定位结果消息，并显示在地图上
     */
    private Handler locHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            try {
                BDLocation location = msg.getData().getParcelable("loc");
                int iscal = msg.getData().getInt("iscalculate");
                if (location != null) {
                    LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
                    // 构建Marker图标
                    BitmapDescriptor bitmap = null;
                    if (iscal == 0) {
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_mark); // 非推算结果
                    } else {
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_focuse_mark); // 推算结果
                    }

                    // 构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
                    // 在地图上添加Marker，并显示
                    //mBaiduMap.addOverlay(option);
                   // mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));

                    if(mPoints.size() > 2){
                        LatLng lastPoint = mPoints.get(mPoints.size() - 1);
                        LatLng lessPoint = mPoints.get(mPoints.size() - 2);
                        double distance = DistanceUtil.getDistance(lastPoint, lessPoint);
                        mTxtLatLng.append("   d: "+distance);
                        Log.d(TAG, "distance:"+distance);
                        if(distance <= 5){
                            mPoints.remove(mPoints.size() - 1);
                        }
                    }
                    mPoints.add(point);

                    if(mOverlayOptions == null){
                         mOverlayOptions = new PolylineOptions().width(10).points(mPoints).color(0xAAFF0000);
                         mPolyline = (Polyline) mBaiduMap.addOverlay(mOverlayOptions);
                    }else{
                        mPolyline.setPoints(mPoints);
                    }


                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    };

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

