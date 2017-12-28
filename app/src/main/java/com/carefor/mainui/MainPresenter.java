package com.carefor.mainui;

import com.carefor.connect.ConnectService;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.login.LoginActivity;
import com.carefor.telephone.TelePhone;
import com.carefor.telephone.TelePhoneAPI;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/22.
 */

public class MainPresenter implements MainContract.Presenter {

    private MainFragment mFragment;

    private Repository mRepository;

    private OnPresenterListener mListener;



    public MainPresenter(Repository repository, MainFragment mainFragment) {
        mRepository = checkNotNull(repository);
        mFragment = checkNotNull(mainFragment);
        mainFragment.setPresenter(this);
    }

    public void setOnPresenterListener(OnPresenterListener listener){
        mListener = listener;
    }
    @Override
    public void start() {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        if(!cacheRepository.isLogin() ){
            if(mListener != null ){
                mListener.startActivity(LoginActivity.class);
            }
        }
        if(mListener != null ){
            mListener.startService(ConnectService.class);
        }
        if(cacheRepository.getEmergencyUser() != null){
            mFragment.showEmUser(cacheRepository.getEmergencyUser());
        }
    }

    @Override
    public void skipToActivity(Class content) {
        if(mListener != null){
            mListener.startActivity(content);
        }
    }

    @Override
    public void showDrawer() {
        if(mListener != null ){
            mListener.showDrawer();
        }
    }

    @Override
    public void callTo() {
        TelePhone.getInstance().afxCallTo(CacheRepository.getInstance().getTalkWith(), new TelePhoneAPI.BaseCallBackAdapter(){});
    }

    interface OnPresenterListener{
        void startActivity(Class content);
        void startService(Class content);
        void showDrawer();
    }
}
