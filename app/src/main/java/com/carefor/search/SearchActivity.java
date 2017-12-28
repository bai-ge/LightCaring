package com.carefor.search;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.carefor.data.source.Repository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.mainui.R;
import com.carefor.util.ActivityUtils;


/**
 * Created by baige on 2017/12/26.
 */

public class SearchActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private Toast mToast;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_toolbar_commmon);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("添加成员");
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

        SearchFragment searchFragment  = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if(searchFragment == null){
            searchFragment = SearchFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), searchFragment, R.id.contentFrame);
        }
        SearchPresenter searchPresenter = new SearchPresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), searchFragment);
    }

    public void showTip(final String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.setText(text);
            }
        });
    }
}
