package com.carefor.housekeeping;

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
import com.carefor.search.SearchActivity;
import com.carefor.util.ActivityUtils;

/**
 * Created by baige on 2018/3/13.
 */

public class HousekeepingActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private Toast mToast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_toolbar_commmon);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("家政服务");
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

        HousekeepingFragment housekeepingFragment = (HousekeepingFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if(housekeepingFragment == null){
            housekeepingFragment =  HousekeepingFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), housekeepingFragment, R.id.contentFrame);
        }
        HousekeepingPresenter housekeepingPresenter = new HousekeepingPresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), housekeepingFragment);


    }
}
