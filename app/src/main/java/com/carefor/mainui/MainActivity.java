package com.carefor.mainui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.carefor.BaseActivity;
import com.carefor.about.AboutActivity;
import com.carefor.broadcast.SendMessageBroadcast;
import com.carefor.connect.msg.Parm;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.jpush.JPushMessageProcess;
import com.carefor.login.LoginActivity;
import com.carefor.membermanage.MemberManageActivity;
import com.carefor.personal.PersonalActivity;
import com.carefor.service.DaemonService;
import com.carefor.service.IPush;
import com.carefor.setting.SettingActivity;
import com.carefor.util.ActivityUtils;
import com.carefor.util.IPUtil;
import com.carefor.util.Loggerx;
import com.carefor.util.Tools;

import java.util.ArrayList;

import cn.jpush.android.api.JPushInterface;


public class MainActivity extends BaseActivity {

    private final static String TAG = MainActivity.class.getCanonicalName();

    private DrawerLayout mDrawerLayout;

    private NavigationView mNavigationView;

    private  MainFragment mainFragment;





    private final int SDK_PERMISSION_REQUEST = 127;

    private String permissionInfo = "";

    //编译之后在app\build\generated\source\aidl\debug\com\carefor\service\IPush.java下
    private IPush mIpush;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mIpush = IPush.Stub.asInterface(iBinder);
            try {
                mIpush.setDeviceId(JPushInterface.getRegistrationID(MainActivity.this));
                int state = mIpush.getConnectState();
                if(mainFragment == null){
                    return;
                }
                switch (state){
                    case Parm.CONNECTING:
                        mainFragment.showInform("正在连接服务器……");
                        break;

                    case Parm.CONNECTED:
                        mainFragment.showInform("正在登录……");
                        break;
                    case Parm.LOGIN:
                        mainFragment.hideInform();
                        break;
                    case Parm.DISCONNECTED:
                        mainFragment.showInform("已断开连接!");
                        break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mIpush = null;
        }
    };

    private BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "动作："+action);
            if(action.equals(SendMessageBroadcast.ACTION_CONNECT_STATE)){
                Bundle bundle = intent.getExtras();
                if(bundle.containsKey(SendMessageBroadcast.KEY_CONNECT_STATE)){
                    int state = bundle.getInt(SendMessageBroadcast.KEY_CONNECT_STATE);
                    if(mainFragment == null){
                        return;
                    }
                    switch (state){
                        case Parm.CONNECTING:
                            mainFragment.showInform("正在连接服务器……");
                            break;

                        case Parm.CONNECTED:
                            mainFragment.showInform("正在登录……");
                            break;
                        case Parm.LOGIN:
                            mainFragment.showInform("登录成功!", 5000);
                            break;
                        case Parm.DISCONNECTED:
                            mainFragment.showInform("已断开连接!");
                            break;
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

        Intent intent = new Intent(this, DaemonService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);//绑定服务




        // Set up the navigation drawer.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }


        CacheRepository cacheRepository = CacheRepository.getInstance();
        cacheRepository.readConfig(this);


        mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (mainFragment == null) {
            // Create the fragment
            mainFragment = MainFragment.newInstance();
            try {
                ActivityUtils.addFragmentToActivity(
                        getSupportFragmentManager(), mainFragment, R.id.contentFrame);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        Repository repository = Repository.getInstance(LocalRepository.getInstance(getApplicationContext()));
        MainPresenter mainPresenter = new MainPresenter(repository, mainFragment);
        mainPresenter.setOnPresenterListener(mOnPresenterListener);

        getPersimmions();

        Log.d("guide_page", "启动主页面");
        registerReceiver();
    }


    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SendMessageBroadcast.ACTION_CONNECT_STATE);
        registerReceiver(connectReceiver, intentFilter);
    }


    @TargetApi(23)
    private void getPersimmions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //showTip("开始申请权限");
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

            // 读写权限，必须要
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }else{
                Loggerx.bWriteToFile = true;
            }

            // 读取电话状态权限，必须要
            if (checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.READ_PHONE_STATE);
            }
//            // 创建与删除文件权限，必须
//            if (checkSelfPermission(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)  != PackageManager.PERMISSION_GRANTED) {
//                permissions.add(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS);
//            }
            //访问网络，必须
            if (checkSelfPermission(android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.INTERNET);
            }


            // 创建与删除文件权限，非必须
            if (addPermission(permissions, android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)) {
                permissionInfo += "Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS Deny \n";
            }
//            //访问网络，非必须
//            if (addPermission(permissions, Manifest.permission.INTERNET)) {
//                permissionInfo += "Manifest.permission.INTERNET Deny \n";
//            }

            if (permissions.size() > 0) {
                //showTip("权限"+permissionInfo);
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
            if(!Tools.isEmpty(permissionInfo)){
                showTip(permissionInfo);
            }

        }

    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)) {//判断该权限是不是用户禁止过
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }

        } else {
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //确保读写文件权限有效
        for (int i = 0; i < permissions.length ; i++) {
            if(permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    grantResults[i] == PackageManager.PERMISSION_GRANTED){
                Loggerx.bWriteToFile = true;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateView();
    }

    public void updateView(){
        if(mNavigationView == null){
            return;
        }
        ImageView headImg = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.app_logo_img);
        TextView textName = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.user_name);

        CacheRepository cacheRepository = CacheRepository.getInstance();
        textName.setText(cacheRepository.who().getName());
    }

    private MainPresenter.OnPresenterListener mOnPresenterListener = new MainPresenter.OnPresenterListener() {
        @Override
        public void startActivity(Class content) {
            Intent intent = new Intent(MainActivity.this, content);
            MainActivity.this.startActivity(intent);
        }

        @Override
        public void startService(Class content) {
            Intent intent = new Intent(MainActivity.this, content);
            MainActivity.this.startService(intent);
        }

        @Override
        public void showDrawer() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
    };

    private void setupDrawerContent(NavigationView navigationView) {
//        ImageView headImg = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.head_img);
//        headImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, EditUserActivity.class);
//                startActivity(intent);
//            }
//        });
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {

                    Intent intent;
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.navigation_menu_home:
                                showTip(menuItem.getTitle().toString());
                                break;
                            case R.id.navigation_personal_setting:
                                showTip(menuItem.getTitle().toString());
                                intent = new Intent(MainActivity.this, PersonalActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.navigation_menu_setting:
                                showTip(menuItem.getTitle().toString());
                                 intent = new Intent(MainActivity.this, SettingActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.navigation_menu_info:
                                showTip(menuItem.getTitle().toString());
                                intent = new Intent(MainActivity.this, AboutActivity.class);
                                startActivity(intent);
                                break;

                            case R.id.navigation_membermanage:
                                showTip(menuItem.getTitle().toString());
                                intent = new Intent(MainActivity.this, MemberManageActivity.class);
                                startActivity(intent);
                                break;

                            case R.id.navigation_menu_logout:
                                showTip(menuItem.getTitle().toString());
                                CacheRepository cacheRepository = CacheRepository.getInstance();
                                cacheRepository.logout();//清除数据
                                 intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                break;


                            case R.id.navigation_menu_exit:
                                showTip(menuItem.getTitle().toString());
                                close();
                                break;
                            default:
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void close() {
        this.finishAll();
    }

    @Override
    protected void onDestroy() {
        Log.d("save", "关闭主界面");
        CacheRepository cacheRepository = CacheRepository.getInstance();
        cacheRepository.saveConfig(this);
        unbindService(mConnection);
        unregisterReceiver(connectReceiver);
        super.onDestroy();
    }
}

