package com.carefor.membermanage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.carefor.BaseActivity;
import com.carefor.callback.SeniorCallBack;
import com.carefor.data.entity.User;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.mainui.R;
import com.carefor.search.SearchActivity;
import com.carefor.adapter.UserAdapter;
import com.carefor.view.ScrollChildSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MemberManageActivity extends BaseActivity {

    private final static String TAG = MemberManageActivity.class.getCanonicalName();
    private UserAdapter mUserAdapter;
    private Repository mRepository;


    // @InjectView(R.id.listview)
    private ListView listview;

    private Toolbar mToolbar;

    private ScrollChildSwipeRefreshLayout mSwipeRefreshLayout;//下拉刷新组件

    private Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_manage);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("成员管理");
        //为activity窗口设置活动栏
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        //设置返回图标
        actionBar.setHomeAsUpIndicator(0);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        listview = (ListView) findViewById(R.id.listview);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        // Set up progress indicator
        mSwipeRefreshLayout =
                (ScrollChildSwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimaryDark)
        );
        mSwipeRefreshLayout.setScrollUpChild(listview);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "刷新文件夹");
                loadMenber();
            }
        });

//
//        List<MemberInfo> mlist = new ArrayList<>();
//
//        //a
//        MemberInfo a = new MemberInfo();
//        Drawable drawable = getResources().getDrawable(R.drawable.head_img);
//        a.setMember_photo(drawable);
//        a.setMember_name("111");
//        a.setMember_phone("18378397030");
//        mlist.add(a);
//
//        //a
//        MemberInfo b = new MemberInfo();
//        b.setMember_photo(drawable);
//        b.setMember_name("222");
//        b.setMember_phone("18378397030");
//        mlist.add(b);
//
//        //c
//        MemberInfo c = new MemberInfo();
//        c.setMember_photo(drawable);
//        c.setMember_name("333");
//        c.setMember_phone("18378397030");
//        mlist.add(c);


        mRepository = Repository.getInstance(LocalRepository.getInstance(getApplicationContext()));
        mUserAdapter = new UserAdapter(new ArrayList<User>(0), mUserItemListener, "选择");
        listview.setAdapter(mUserAdapter);
    }

    //actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                Intent intent = new Intent(MemberManageActivity.this, SearchActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadMenber(){
        CacheRepository cacheRepository = CacheRepository.getInstance();
        final User user = cacheRepository.who();
        mUserAdapter.clear();//清除旧数据

        if(user.getType() == 1){//监护人
            mRepository.asynGetAllPupillusOf(user.getUid(), new SeniorCallBack() {
                @Override
                public void onResponse() {
                    super.onResponse();
                    showTip("查询被监护人响应");
                    setTitle("被监护人管理");
                    setRefreshing(false);

                }

                @Override
                public void loadUsers(List<User> list) {
                    super.loadUsers(list);
                    Log.d(TAG, list.toString());
                    mUserAdapter.addUsers(list);
                }

                @Override
                public void meaning(String text) {
                    showTip("查询被监护人" + text);
                }
            });
        }else if(user.getType() == 2){ //被监护人
            mRepository.asynGetAllGuardiansOf(user.getUid(), new SeniorCallBack() {
                @Override
                public void onResponse() {
                    super.onResponse();
                    showTip("查询监护人响应");
                    setTitle("监护人管理");
                    setRefreshing(false);
                }

                @Override
                public void loadUsers(List<User> list) {
                    super.loadUsers(list);
                    Log.d(TAG, list.toString());
                    mUserAdapter.addUsers(list);
                }

                @Override
                public void meaning(String text) {
                    showTip("查询监护人" + text);
                }
            });
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadMenber();
    }

    private UserAdapter.UserItemListener mUserItemListener = new UserAdapter.UserItemListener() {
        @Override
        public void onClickItem(User user) {
            Log.d(TAG, "紧急联系人" + user);
            showTip("点击" + user.getName());
            //把该用户设为紧急联系人
            mRepository.asynQuery(user.getUid(), new SeniorCallBack() {
                @Override
                public void loadAUser(User u) {
                    super.loadAUser(u);
                    Log.d(TAG, "紧急联系人" + u);
                    CacheRepository.getInstance().setSelectUser(u);
                    CacheRepository.getInstance().setTalkWith(u.getDeviceId());
                    CacheRepository.getInstance().saveConfig(getApplicationContext());
                }
            });
            CacheRepository.getInstance().setSelectUser(user);
            CacheRepository.getInstance().saveConfig(getApplicationContext());
            onBackPressed();

        }
    };

    private void showTip(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.setText(text);
                mToast.show();
            }
        });
    }
    private void setTitle(final String title){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToolbar.setTitle(title);
            }
        });
    }
    private void setRefreshing(final boolean refresh) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout.isRefreshing() && !refresh) {
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    mSwipeRefreshLayout.setRefreshing(refresh);
                }
            }
        });
    }


}
