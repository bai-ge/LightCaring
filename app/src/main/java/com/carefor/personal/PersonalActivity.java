package com.carefor.personal;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.carefor.mainui.R;
import com.carefor.util.ActivityUtils;

public class PersonalActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_toolbar_commmon);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("个人设置");

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

        PersonalFragment personalFragment = (PersonalFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if(personalFragment == null){
            personalFragment = new PersonalFragment();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), personalFragment, R.id.contentFrame);
        }


    }
}
