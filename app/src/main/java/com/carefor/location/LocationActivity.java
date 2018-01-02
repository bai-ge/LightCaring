package com.carefor.location;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.carefor.BaseActivity;
import com.carefor.data.source.Repository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.mainui.R;
import com.carefor.util.ActivityUtils;

/**
 * Created by baige on 2018/1/2.
 */

public class LocationActivity extends BaseActivity {
    private Toast mToast;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_common);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        LocationFragment locationFragment = (LocationFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if(locationFragment == null){
            locationFragment = LocationFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), locationFragment, R.id.contentFrame);
        }
        Repository repository = Repository.getInstance(LocalRepository.getInstance(getApplicationContext()));
        LocationPresenter locationPresenter = new LocationPresenter(repository, locationFragment);

    }
}
