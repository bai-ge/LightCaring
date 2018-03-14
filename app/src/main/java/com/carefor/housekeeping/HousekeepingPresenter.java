package com.carefor.housekeeping;

import com.carefor.callback.SeniorCallBack;
import com.carefor.data.entity.Housekeeping;
import com.carefor.data.source.Repository;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/3/13.
 */

public class HousekeepingPresenter implements HousekeepingContract.Presenter {
    private Repository mRepository;

    private HousekeepingContract.View mFragment;

    public HousekeepingPresenter(Repository instance, HousekeepingFragment housekeepingFragment) {
        this.mRepository = checkNotNull(instance);
        this.mFragment = checkNotNull(housekeepingFragment);
        housekeepingFragment.setPresenter(this);
    }

    @Override
    public void start() {
        mRepository.asynGetAllHousekeeping(new SeniorCallBack(){
            @Override
            public void loadHousekeepings(List<Housekeeping> housekeepingList) {
                mFragment.showHousekeepings(housekeepingList);
            }
        });
    }

    @Override
    public void search(String word) {
        mRepository.asynSearchHousekeepingByKey(word, new SeniorCallBack(){
            @Override
            public void onResponse() {
                super.onResponse();
                mFragment.setRefreshing(false);
            }

            @Override
            public void loadHousekeepings(List<Housekeeping> housekeepingList) {
                mFragment.showHousekeepings(housekeepingList);
            }
        });
    }
}
