package com.carefor.location;

import com.carefor.data.source.Repository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/1/2.
 */

public class LocationPresenter implements LocationContract.Presenter {
    private Repository mRepositor;
    private LocationFragment mFragment;
    public LocationPresenter(Repository repository, LocationFragment locationFragment) {
        mRepositor = checkNotNull(repository);
        mFragment = checkNotNull(locationFragment);
        locationFragment.setPresenter(this);
    }

    @Override
    public void start() {

    }
}
