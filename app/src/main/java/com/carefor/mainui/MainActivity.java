package com.carefor.mainui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
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
import com.carefor.connect.ConnectService;
import com.carefor.connect.HeartBeatService;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.login.LoginActivity;
import com.carefor.membermanage.MemberManageActivity;
import com.carefor.setting.SettingActivity;
import com.carefor.util.ActivityUtils;

import cn.jpush.android.api.JPushInterface;


public class MainActivity extends BaseActivity {

    private final static String TAG = MainActivity.class.getCanonicalName();

    private DrawerLayout mDrawerLayout;

    private NavigationView mNavigationView;

    private Toast mToast;

    private ConnectService.ConnectBinder mBinder = null;



    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (ConnectService.ConnectBinder) service;
            Log.d("Service", "servic is bind");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("Service", "servic disconected");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        /*
        Intent intent = new Intent(this, ConnectService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);//绑定服务
        */



        // Set up the navigation drawer.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }
        //初始化极光推送
        JPushInterface.setDebugMode(true);
        String text = JPushInterface.getRegistrationID(this);
        Log.d("JPush", "初始化之前"+text);
        JPushInterface.init(this);
        text = JPushInterface.getRegistrationID(this);
        Log.d("JPush", "初始化之后"+text);

        CacheRepository cacheRepository = CacheRepository.getInstance();
        cacheRepository.readConfig(this);

        Intent intent = new Intent(MainActivity.this, HeartBeatService.class);
        startService(intent);//调用onStartCommand()

        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
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


        Log.d("guide_page", "启动主页面");
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
                                cacheRepository.setLogin(false);
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

    private void showTip(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.setText(text);
                mToast.show();
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
        super.onDestroy();
    }
}

