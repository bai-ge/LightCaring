package com.carefor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import com.carefor.data.source.cache.CacheRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baige on 2017/12/24.
 */

public class BaseActivity extends AppCompatActivity {
   public static List<BaseActivity> activities = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activities.add(this);
        Log.d("finishAll", "添加"+this.getClass().getCanonicalName()+"活动："+activities.size());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activities.remove(this);
        Log.d("finishAll", "删除"+this.getClass().getCanonicalName()+"活动："+activities.size());
    }
    public void finishAll(){
        for (BaseActivity activity : activities){
            activity.finish();
        }
    }
}
