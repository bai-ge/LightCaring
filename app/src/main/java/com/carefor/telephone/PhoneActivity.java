package com.carefor.telephone;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.carefor.BaseActivity;
import com.carefor.data.source.Repository;
import com.carefor.data.source.local.LocalRepository;
import com.carefor.mainui.R;
import com.carefor.util.ActivityUtils;


/**
 * Created by baige on 2017/10/29.
 */

public class PhoneActivity extends BaseActivity {
    private Toast mToast;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_common);
        mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);



        PhoneFragment phoneFragment =
                (PhoneFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (phoneFragment == null) {
            // Create the fragment
            phoneFragment = PhoneFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), phoneFragment, R.id.contentFrame);
        }
        PhonePresenter presenter = new PhonePresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), phoneFragment);
    }
}
