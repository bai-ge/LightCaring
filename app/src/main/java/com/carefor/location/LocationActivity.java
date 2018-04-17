package com.carefor.location;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.carefor.BaseActivity;
import com.carefor.data.source.Repository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.mainui.R;
import com.carefor.util.ActivityUtils;


import java.util.List;

/**
 * Created by baige on 2018/1/2.
 */

public class LocationActivity extends BaseActivity {
    private Toast mToast;

    private final static int SDK_PERMISSION_REQUEST = 125;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_common);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        LocationFragment locationFragment = (LocationFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (locationFragment == null) {
            locationFragment = LocationFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), locationFragment, R.id.contentFrame);
        }
        Repository repository = Repository.getInstance(LocalRepository.getInstance(getApplicationContext()));
        LocationPresenter locationPresenter = new LocationPresenter(repository, locationFragment);

        requestRunTimePermission(new String[]{Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                new PermissionListener() {
                    @Override
                    public void onGranted() {

                    }

                    @Override
                    public void onGranted(List<String> grantedPermission) {

                    }

                    @Override
                    public void onDenied(List<String> deniedPermission) {
                        StringBuffer stringBuffer = new StringBuffer();
                        for (String text : deniedPermission){
                            stringBuffer.append(text+"\n");
                        }
                        showTip("获取权限失败："+stringBuffer.toString());
                    }
                });
    }
}
