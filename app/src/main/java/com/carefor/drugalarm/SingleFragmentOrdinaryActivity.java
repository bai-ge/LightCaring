package com.carefor.drugalarm;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.carefor.mainui.R;

import me.imid.swipebacklayout.lib.app.SwipeBackOrdinaryActivity;

/**
 * Created by Ryoko on 2018/3/14.
 */

public abstract class SingleFragmentOrdinaryActivity extends SwipeBackOrdinaryActivity {

    /**
     * 抽象方法：创建Fragment
     *
     * @return Fragment
     */
    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fm_activity);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.frag_containers);
        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.frag_containers, fragment)
                    .commit();

        }

    }

}
