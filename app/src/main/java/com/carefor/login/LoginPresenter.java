package com.carefor.login;

import android.util.Log;

import com.carefor.data.entity.User;
import com.carefor.data.source.Repository;
import com.carefor.data.source.ServerAPI;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.guidepage.GuidepageActivity;
import com.carefor.register.RegisterActivity;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/22.
 */

public class LoginPresenter implements LoginContract.Presenter {
    private final static String TAG = LoginPresenter.class.getCanonicalName();

    private Repository mRepository;

    private LoginFragment mFragment;

    private OnPresenterListener mListener;

    public LoginPresenter(Repository repository, LoginFragment loginFragment) {
        mRepository = checkNotNull(repository);
        mFragment = checkNotNull(loginFragment);
        mFragment.setPresenter(this);
    }

    public void setOnPresenterListener(OnPresenterListener listener){
        mListener = listener;
    }

    @Override
    public void start() {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        if(CacheRepository.isNeedOpenGuidepage){
            if(mListener != null){
                mListener.startActivity(GuidepageActivity.class);
            }
        }
        User user = cacheRepository.who();
        if(user != null ){
            if(user.getName() != null && !user.getName().isEmpty()){
                mFragment.showName(user.getName());
            }
            if(user.getPsw() != null && !user.getPsw().isEmpty()){
                mFragment.setPsw(user.getPsw());
            }
        }
    }

    @Override
    public void login(final String name, String psw) {
        //TODO
        mFragment.showTip("登录");
        final User user = new User();
        user.setName(name);
        user.setPsw(psw);
        user.setDeviceId(CacheRepository.getInstance().getDeviceId());

        mRepository.afxLogin(user, new ServerAPI.BaseCallBackAdapter() {
            @Override
            public void success() {
                mFragment.showTip("用户\""+name+"\"登录成功");
                final CacheRepository cacheRepository = CacheRepository.getInstance();
                cacheRepository.setLogin(true);
                if(mListener != null){
                    mListener.closeActivity();
                }
                // 从远程服务器加载个人信息
                mRepository.afxQueryByName(name, new ServerAPI.BaseCallBackAdapter(){
                    @Override
                    public void loadUsers(List<User> list) {
                        super.loadUsers(list);
                        user.setUid(list.get(0).getUid());
                        user.setType(list.get(0).getType());
                        user.setTel(list.get(0).getTel());
                        cacheRepository.setYouself(user);
                        Log.d(TAG, "用户同步信息"+user);
                    }
                });
            }

            @Override
            public void fail() {
                mFragment.showTip("登录失败");
            }

            @Override
            public void timeout() {
                mFragment.showTip("登录超时");
            }

            @Override
            public void unknown() {
                mFragment.showTip("未知错误");
            }

            @Override
            public void notfind() {
                mFragment.showTip("用户\"" + name + "\"不存在");
            }

        });
    }

    @Override
    public void skipToRegist() {
        mFragment.showTip("注册新账号");
        if(mListener != null){
            mListener.startActivity(RegisterActivity.class);
        }
    }

    @Override
    public void skipToHome() {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        cacheRepository.setLogin(true);
//        if(mListener != null){
//            mListener.startActivity(MainActivity.class);
//        }
        mFragment.finishActivity();
    }

    interface OnPresenterListener{
        void startActivity(Class content);
        void closeActivity();
    }
}
