package com.carefor.location;

import android.graphics.Point;
import android.graphics.Rect;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
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
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import com.carefor.data.entity.Location;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.mainui.R;
import com.carefor.util.Tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
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


    //界面组件
    private TextView mTxtLatLng;

    private TextView mTxtAccuracy; //精度

    private TextView mTxtArea; //地区

    private TextView mTxtLocation; //详细地址描述

    private LinearLayout mInformLayout;//通知栏
    private TextView mTxtInform;

    private RelativeLayout mLocationLayout; //位置窗口

    private LinearLayout mLayout; //第一行字体

    private ImageView mImgLocationType; //定位图标

    private Button mBtnLocModel; //模式切换按钮

    private Button mBtnAskLocation; //即时定位按钮

    private Button mBtnAskHelp; //一键呼救按钮

    private Button mBtnDestination; //目标定位按钮

    private Button mBtnTrack; //是否显示路线按钮

    private InfoWindow mInfoWindow; //目标显示对话框

    private View mDialog;//对话框视图

    private View mScaleView; //标尺定位视图

    private View mCompassView;//指南针定位视图

    //对话框组件
    private TextView mTxtName; //目标名字

    private TextView mTxtBatteryPercent;//电池电量

    private ImageView mImgAccuracy;//信号精度，用四张图片表示 浅颜色(qfz, qft, qfx, qfv) 深颜色(qga, qfu, qfy, qfw)

    private TextView mTxtBArea;

    private TextView mTxtBLocation;

    private TextView mTxtTime;

    private TextView mTxtDistance;

    private UiSettings mUiSettings;

    private boolean mDialogIsShowing;


    //TODO 调试相关
    private long mClickTime;
    private int mClickCount;


    private Runnable mHintInformRunnable = new Runnable() {
        @Override
        public void run() {
            if (mInformLayout != null) {
                mInformLayout.setVisibility(View.GONE);
            }
        }
    };
    private Runnable mHintLocationDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBaiduMap != null && mInfoWindow != null) {
                mBaiduMap.hideInfoWindow();
            }
        }
    };












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
    private BitmapDescriptor mTargetMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_qis);

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

    @Override
    public void isGuardian(boolean is) {
        if(is){
            mBtnAskHelp.setVisibility(View.GONE);
        }else{
            mBtnAskLocation.setVisibility(View.GONE);
            mBtnDestination.setVisibility(View.INVISIBLE);
        }

    }

    private void initView(View root) {


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
        mUiSettings = mBaiduMap.getUiSettings();
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
                if (marker == mMarker) {
                    if(mDialogIsShowing){
                        mDialogIsShowing = false;
                        mBaiduMap.hideInfoWindow();
                        mMapView.removeView(mDialog);
                    }else{
                        showLocationDialog(marker.getPosition());
                    }
                }
                return true;
            }
        });


        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        mInformLayout = (LinearLayout) root.findViewById(R.id.inform_layout);
        mLocationLayout = (RelativeLayout) root.findViewById(R.id.relative_layout);
        mLayout = (LinearLayout) root.findViewById(R.id.layout_my_location);

        mImgLocationType = (ImageView) root.findViewById(R.id.img_location_type);

        mTxtInform = (TextView) root.findViewById(R.id.inform_text);
        mTxtLatLng = (TextView) root.findViewById(R.id.txt_latlng);
        mTxtAccuracy = (TextView) root.findViewById(R.id.txt_accuracy);
        mTxtArea = (TextView) root.findViewById(R.id.txt_area);
        mTxtLocation = (TextView) root.findViewById(R.id.txt_location);

        mBtnLocModel = (Button) root.findViewById(R.id.btn_model);
        mBtnDestination = (Button) root.findViewById(R.id.btn_destination);
        mBtnTrack = (Button) root.findViewById(R.id.btn_track);

        mBtnAskLocation = (Button) root.findViewById(R.id.btn_ask_location);
        mBtnAskHelp = (Button) root.findViewById(R.id.btn_ask_help);

        mScaleView = root.findViewById(R.id.scale_view);
        mCompassView = root.findViewById(R.id.compass_view);

        //加载对话框视图
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mDialog = inflater.inflate(R.layout.dialog_map, null);
        initDialogView(mDialog);
        mDialog.setAlpha(0.2f);//设置透明度
        mDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogIsShowing = false;
                mBaiduMap.hideInfoWindow();
                mMapView.removeView(mDialog);
            }
        });

        mRedTexture = BitmapDescriptorFactory.fromAsset("icon_road_red_arrow.png");

        mBtnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mSelDesPolyline != null && mSelDesPolyline.isVisible())||
                        ( mMyPolyline != null && mMyPolyline.isVisible())) {
                    mBtnTrack.setBackgroundResource(R.drawable.btn_hint_track);
                    if(mSelDesPolyline != null){
                        mSelDesPolyline.setVisible(false);
                    }
                    if(mMyPolyline != null){
                        mMyPolyline.setVisible(false);
                    }

                } else {
                    mBtnTrack.setBackgroundResource(R.drawable.btn_show_track);
                    if (mSelDesPolyline != null) {
                        mSelDesPolyline.setVisible(true);
                    }
                    if(mMyPolyline != null){
                        mMyPolyline.setVisible(true);
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
                if (mBaiduMap != null && mMarker != null) {
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
                        mBtnLocModel.setBackgroundResource(R.drawable.btn_followx);
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
                if (size > 0) {
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(mCacheRepository.getMyPoints().get(size - 1));
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }
                mLocationLayout.setVisibility(View.VISIBLE);
            }
        });

        //TODO 调试，连击超过5次更换状态
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(System.currentTimeMillis() - mClickTime < 500){
                    mClickCount ++;
                    if(mClickCount >= 5){
                        if(mTxtLatLng.getVisibility() == View.VISIBLE){
                            mTxtLatLng.setVisibility(View.INVISIBLE);
                            mTxtAccuracy.setVisibility(View.INVISIBLE);
                        }else{
                            mTxtLatLng.setVisibility(View.VISIBLE);
                            mTxtAccuracy.setVisibility(View.VISIBLE);
                        }
                        mClickCount = 0;
                    }
                }else{
                    mClickCount = 0;
                }
                mClickTime = System.currentTimeMillis();
            }
        });

        mLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationLayout.setVisibility(View.INVISIBLE);
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
                switch (reason) {
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
    }
    private void initDialogView(View root){
        mTxtName = (TextView) root.findViewById(R.id.txt_name);
        mTxtBatteryPercent = (TextView) root.findViewById(R.id.txt_battery);
        mImgAccuracy = (ImageView) root.findViewById(R.id.img_signal);
        mTxtBArea = (TextView) root.findViewById(R.id.txt_area);
        mTxtBLocation = (TextView) root.findViewById(R.id.txt_location);
        mTxtTime = (TextView) root.findViewById(R.id.txt_time);
        mTxtDistance = (TextView) root.findViewById(R.id.txt_distance);
    }


    /**
     * 陀螺仪方向改变
     *
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


    private void resetScaleControlPosition() {
        Rect mapRect = new Rect();
        mMapView.getGlobalVisibleRect(mapRect);
        Rect rect = new Rect();
        mScaleView.getGlobalVisibleRect(rect);
        mMapView.setScaleControlPosition(new android.graphics.Point(rect.left - mapRect.left, rect.top - mapRect.top));
        mCompassView.getGlobalVisibleRect(rect);
        mBaiduMap.setCompassPosition(new Point(rect.left - mapRect.left, rect.top - mapRect.top));
    }

    private void marker(LatLng ll) {
        Log.d(TAG, "标记");
        if (ll == null) {
            return;
        }
        if (mMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions().position(ll).icon(mTargetMarker).anchor(0.5f, 1f).perspective(false).zIndex(7);
            mMarker = (Marker) mBaiduMap.addOverlay(markerOptions);
        } else {
            mMarker.setPosition(ll);
        }
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(ll);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
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
                mInformLayout.setVisibility(View.VISIBLE);
                mHandler.removeCallbacks(mHintInformRunnable);
                mHandler.postDelayed(mHintInformRunnable, 1000 * 30);
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
            if(location.getLocType() == BDLocation.TypeGpsLocation){
                mImgLocationType.setBackgroundResource(R.drawable.ic_loc_gps);
            }else{
                mImgLocationType.setBackgroundResource(R.drawable.ic_loc_net);
            }
            //更新位置
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            int size = mCacheRepository.getMyPoints().size();
            double distance = 0;

//            if (size > 2) {
//                LatLng lastPoint = mCacheRepository.getMyPoints().get(size - 1);
//                LatLng lessPoint = mCacheRepository.getMyPoints().get(size - 2);
//                distance = DistanceUtil.getDistance(lastPoint, lessPoint);
//                Log.d(TAG, "distance:" + distance + "accuracy: " + location.getRadius());
//                if (distance <= 5 && location.getRadius() <= 50) {
//                    mCacheRepository.getMyPoints().remove(size - 1);
//                }
//            }
            if (location.getRadius() <= 10) {
                mCacheRepository.getMyPoints().add(point);
            }
            if (mCacheRepository.getMyPoints().size() >= 2) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mMyPolyline == null) {
                            PolylineOptions polylineOptions = new PolylineOptions().width(10).points(mCacheRepository.getMyPoints()).color(0xAA0000FF);
                            mMyPolyline = (Polyline) mBaiduMap.addOverlay(polylineOptions);
                        } else {
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
            mTxtLatLng.setText("(" + df.format(mCacheRepository.getCurrentLon()) + " ," + df.format(mCacheRepository.getCurrentLat()) +")");

            df = new DecimalFormat("#.00");
            mTxtAccuracy.setText(df.format(location.getRadius()));

            mTxtArea.setText(location.getAddrStr());

            StringBuffer stringBuffer = new StringBuffer(256);
            if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                stringBuffer.append("(");
                for (int i = 0; i < location.getPoiList().size() && i < 2; i++) {
                    Poi poi = location.getPoiList().get(i);
                    stringBuffer.append(poi.getName() + ";");
                }
                stringBuffer.append(")");
            }
//            mTxtLocation.setText(stringBuffer);
            mTxtLocation.setText(location.getLocationDescribe());
            Log.d(TAG, "onReceiveLocation()");

            List<Location> plocList = mCacheRepository.getDesLocation();
            if(plocList != null && plocList.size() > 0){
                Location ploc = plocList.get(plocList.size() - 1);
                distance = DistanceUtil.getDistance(point, ploc.getLatLng());
                String distanceTxt = "";
                if(distance < 1000){
                    distanceTxt = "距离："+df.format(distance) + "m";
                }else {
                    distanceTxt =  "距离："+df.format((distance / 1000)) + "km";
                }
                mTxtDistance.setText(distanceTxt);
            }
        }

    };



    @Override
    public void showPLocation(Location loc) {
        if(loc != null  && loc.getLatLng() != null){
            List<Location> locList = CacheRepository.getInstance().getDesLocation();
                locList.add(loc);
                if (locList.size() > 500) {
                    locList.remove(0);
                }
            Collections.sort(locList);//根据时间排序
            showLocationDialog(loc);
        }
    }

    @Override
    public void showLocationDialog(Location loc) {
        if (loc != null && loc.getLatLng() != null) {
            if (loc.getDescription() != null) {
                if(!Tools.isEmpty(loc.getName())){
                    mTxtName.setText(loc.getName());
                }
                if(loc.getBatteryPercent() != 0){
                    DecimalFormat df = new DecimalFormat("0%");
                    mTxtBatteryPercent.setText(df.format(loc.getBatteryPercent()));
                }

                if(loc.getAccuracy() != 0){
                    showSignal(loc.getAccuracy());
                }


                if(!Tools.isEmpty(loc.getArea())){
                    mTxtBArea.setText(loc.getArea());
                }

                mTxtBLocation.setText(loc.getDescription());
                mTxtTime.setText(Tools.formatTime(loc.getTime()));

                if(mLocData != null && loc.getLatLng() != null){
                    LatLng lastLatLng = new LatLng(mLocData.latitude, mLocData.longitude);
                    double distance = DistanceUtil.getDistance(loc.getLatLng(), lastLatLng);

                    String distanceTxt = "";
                    DecimalFormat df = new DecimalFormat("#.00");
                    if(distance < 1000){
                        distanceTxt = "距离："+df.format(distance) + "m";
                    }else {
                        distanceTxt =  "距离："+df.format((distance / 1000)) + "km";
                    }
                    mTxtDistance.setText(distanceTxt);
                }
            }
            marker(loc.getLatLng());
        }
    }

    @Override
    public void showPLocation(List<Location> locationList) {
        //TODO 展示路径
        final List<LatLng> points = new ArrayList<>();
        for (int i = 0; i < locationList.size(); i++) {
            points.add(locationList.get(i).getLatLng());
        }
        showLocationDialog(locationList.get(locationList.size() - 1));
        showTargetTrack(points);
    }

    @Override
    public void showTargetTrack(final List<LatLng> loc) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSelDesPolyline == null) {
                    mSelDesPolyline = (Polyline) mBaiduMap.addOverlay(new PolylineOptions().width(10).points(loc).color(0xAAFF0000));
                } else {
                    mSelDesPolyline.setPoints(loc);
                }
            }
        });
        marker(loc.get(loc.size() - 1));
    }



    //信号精度，用四张图片表示 浅颜色(qfz, qft, qfx, qfv) 深颜色(qga, qfu, qfy, qfw) 由弱到强
    @Override
    public void showSignal(final float acc) { //数值越小，强度越大
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(acc < 20){
                    mImgAccuracy.setBackgroundResource(R.drawable.qfv);
                    Log.d(TAG, "showSignal()"+acc);
                }else if(acc < 50){
                    mImgAccuracy.setBackgroundResource(R.drawable.qfx);
                    Log.d(TAG, "showSignal()"+acc);
                }else if(acc < 100){
                    mImgAccuracy.setBackgroundResource(R.drawable.qft);
                    Log.d(TAG, "showSignal()"+acc);
                }else{
                    mImgAccuracy.setBackgroundResource(R.drawable.qfz);
                    Log.d(TAG, "showSignal()"+acc);
                }
            }
        });

    }

    @Override
    public void showLocationDialog(final LatLng ll) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBaiduMap.hideInfoWindow();
                mMapView.removeView(mDialog);
                int height =  mTargetMarker.getBitmap().getHeight();
                Log.d(TAG, "height ="+height);
                mInfoWindow = new InfoWindow(mDialog, ll, -height);
                mBaiduMap.showInfoWindow(mInfoWindow);
                mDialogIsShowing = true;
                //mHandler.removeCallbacks(mHintLocationDialogRunnable);
               // mHandler.postDelayed(mHintLocationDialogRunnable, 8000);
            }
        });
    }

}

