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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.carefor.data.entity.Location;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.mainui.R;
import com.carefor.util.Tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
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

    private TextView mTxtLatLng;
    private TextView mTxtLocation;
    private LinearLayout mLinearLayout;

    private Runnable mHintInformRunnable = new Runnable() {
        @Override
        public void run() {
            if(mLinearLayout != null){
                mLinearLayout.setVisibility(View.GONE);
            }
        }
    };
    private Runnable mHintLocationDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if(mBaiduMap != null && mInfoWindow != null){
                mBaiduMap.hideInfoWindow();
            }
        }
    };
    private TextView mTxtInform;
    private Button mBtnLocModel;

    private Button mBtnAskLocation;
    private Button mBtnDestination;
    private Button mBtnTrack;


    private InfoWindow mInfoWindow;
    private TextView mDialogText;

    private View mScaleView;


    private boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData mLocData;


    // 定位相关
    private LocationService locationService;
    private BaiduMap mBaiduMap;


    private SensorManager mSensorManager;

    private MyLocationConfiguration.LocationMode mCurrentMode;

    private Double mLastX = 0.0;
    private int mCurrentDirection = 0; //方向 0~360

    private CacheRepository mCacheRepository;


    //标记
    private BitmapDescriptor mTargetMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);

    private Marker mMarker;

    private Polyline mMyPolyline;
    private Polyline mSelDesPolyline;
    private BitmapDescriptor mRedTexture;






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

        mDialogText = new TextView(getContext());
        mDialogText.setBackgroundResource(R.drawable.popup);
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
        mCacheRepository = CacheRepository.getInstance();
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
        mBaiduMap.setOnMapDoubleClickListener(new BaiduMap.OnMapDoubleClickListener() {
            /**
             * 双击地图
             */
            public void onMapDoubleClick(LatLng point) {
//                marker(point);
                showTip("双击");
            }
        });
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker == mMarker){
                    showLocationDialog(marker.getPosition());
                }
                return true;
            }
        });
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBtnLocModel = (Button) root.findViewById(R.id.btn_model);
        mBtnAskLocation = (Button) root.findViewById(R.id.btn_ask_location);
        mBtnDestination = (Button) root.findViewById(R.id.btn_destination);
        mBtnTrack = (Button) root.findViewById(R.id.btn_track);
        mTxtLatLng = (TextView) root.findViewById(R.id.txt_latlng);
        mTxtLocation = (TextView) root.findViewById(R.id.txt_location);

        mLinearLayout = (LinearLayout) root.findViewById(R.id.inform_layout);
        mTxtInform = (TextView) root.findViewById(R.id.inform_text);
        mScaleView = root.findViewById(R.id.scale_view);

        mRedTexture = BitmapDescriptorFactory.fromAsset("icon_road_red_arrow.png");

        mBtnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelDesPolyline != null && mSelDesPolyline.isVisible()){
                    mBtnTrack.setBackgroundResource(R.drawable.btn_hint_track);
                    mSelDesPolyline.setVisible(false);
                }else{
                    mBtnTrack.setBackgroundResource(R.drawable.btn_show_track);
                    if(mSelDesPolyline != null){
                        mSelDesPolyline.setVisible(true);
                    }
                }
            }
        });
        mBtnAskLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.askLocation();
            }
        });
        mBtnDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBaiduMap != null && mMarker != null){
                    LatLng position = mMarker.getPosition();
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(position);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }
                mPresenter.loadLocation();
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
                                mCurrentMode, true, null));
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        break;
                    case FOLLOWING:
                       // mBtnLocModel.setText("罗盘");
                        mBtnLocModel.setBackgroundResource(R.drawable.btn_navigation);
                        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, null));
                        break;
                    default:
                        break;
                }
                int size = mCacheRepository.getMyPoints().size();
                locationService.requestLocation();
                if(size > 0){
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(mCacheRepository.getMyPoints().get(size - 1));
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
                                mCurrentMode, true, null));
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

        //TODO DEBUG
        mBtnAskLocation.setVisibility(View.GONE);
        mTxtLatLng.setVisibility(View.GONE);


    }


    /**陀螺仪方向改变
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[SensorManager.DATA_X];
        if (Math.abs(x - mLastX) > 1.0) {
            mCurrentDirection = (int) x;
            mLocData = new MyLocationData.Builder()
                    .accuracy(mCacheRepository.getCurrentAccracy())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCacheRepository.getCurrentLat())
                    .longitude(mCacheRepository.getCurrentLon()).build();
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

    private void marker(LatLng ll){
        Log.d(TAG, "标记");
        if(ll == null){
            return;
        }
        if(mMarker == null){
            MarkerOptions markerOptions = new MarkerOptions().position(ll).icon(mTargetMarker);
            mMarker = (Marker) mBaiduMap.addOverlay(markerOptions);
        }else{
            mMarker.setPosition(ll);
        }
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(ll);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        if(mInfoWindow != null){
            mBaiduMap.hideInfoWindow();
        }
        showLocationDialog(ll);
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    public void onStop() {
        mPresenter.stop();
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

    @Override
    public void showInform(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTxtInform.setText(text);
                mLinearLayout.setVisibility(View.VISIBLE);
                mHandler.removeCallbacks(mHintInformRunnable);
                mHandler.postDelayed(mHintInformRunnable, 1000*30);
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
            //更新位置
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            int size = mCacheRepository.getMyPoints().size();
            double distance = 0;

            if(size > 2){
                LatLng lastPoint = mCacheRepository.getMyPoints().get(size - 1);
                LatLng lessPoint = mCacheRepository.getMyPoints().get(size - 2);
                distance = DistanceUtil.getDistance(lastPoint, lessPoint);
                Log.d(TAG, "distance:"+distance +"accuracy: "+location.getRadius());
                if(distance <= 5 && location.getRadius() <= 50){
                    mCacheRepository.getMyPoints().remove(size - 1);
                }
            }
            if(location.getRadius() <= 50){
                mCacheRepository.getMyPoints().add(point);
            }
            if(mCacheRepository.getMyPoints().size() >= 2){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mMyPolyline == null){
                            PolylineOptions polylineOptions = new PolylineOptions().width(10).points(mCacheRepository.getMyPoints()).color(0xAA0000FF);
                            mMyPolyline = (Polyline) mBaiduMap.addOverlay(polylineOptions);
                        }else{
                            mMyPolyline.setPoints(mCacheRepository.getMyPoints());
                        }
                    }
                });
            }
            mCacheRepository.setCurrentLat(location.getLatitude());
            mCacheRepository.setCurrentLon(location.getLongitude());
            mCacheRepository.setCurrentAccracy(location.getRadius());

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

            DecimalFormat df = new DecimalFormat("#.000");
            mTxtLatLng.setText("经度："+ df.format(mCacheRepository.getCurrentLon()) + "    维度："+df.format(mCacheRepository.getCurrentLat()));
            mTxtLatLng.append(" d: "+distance);
            mTxtLatLng.append(" accuracy:"+location.getRadius());

            StringBuffer stringBuffer = new StringBuffer(256);
            stringBuffer.append(location.getAddrStr());
            if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                stringBuffer.append("(");
                for (int i = 0; i < location.getPoiList().size() && i < 2; i++) {
                    Poi poi = location.getPoiList().get(i);
                    stringBuffer.append(poi.getName() + ";");
                }
                stringBuffer.append(")");
            }
            mTxtLocation.setText(stringBuffer);
            Log.d(TAG, "onReceiveLocation()");
        }
    };

    @Override
    public void showPLocation(Location loc) {
        if(loc != null && loc.getLatLng() != null){
            if(loc.getDescription() != null){
                mDialogText.setText(loc.getDescription() +"\n"+ Tools.formatTime(loc.getTime()));
            }
            marker(loc.getLatLng());
            showTip("显示位置："+loc.toString());
        }
    }

    @Override
    public void showPLocation(List<Location> locationList) {
        //TODO 展示路径
        final List<LatLng> points = new ArrayList<>();
        for (int i = 0; i < locationList.size(); i ++){
            points.add(locationList.get(i).getLatLng());
        }
        setTargetDialog(locationList.get(locationList.size()-1).getDescription());
        showTargetTrack(points);
    }

    @Override
    public void showTargetTrack(final List<LatLng> loc) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mSelDesPolyline == null){
                    mSelDesPolyline = (Polyline) mBaiduMap.addOverlay(new PolylineOptions().width(10).points(loc).color(0xAAFF0000));
                }else{
                    mSelDesPolyline.setPoints(loc);
                }
            }
        });
        marker(loc.get(loc.size() - 1));
    }

    @Override
    public void setTargetDialog(String text) {
        mDialogText.setText(text);
    }

    @Override
    public void showLocationDialog(final LatLng ll) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mInfoWindow = new InfoWindow(mDialogText, ll, -160);
                mBaiduMap.showInfoWindow(mInfoWindow);
                mHandler.removeCallbacks(mHintLocationDialogRunnable);
                mHandler.postDelayed(mHintLocationDialogRunnable, 8000);
            }
        });
    }

}

