package com.carefor.register;

import com.carefor.callback.SeniorCallBack;
import com.carefor.data.entity.User;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/22.
 */

public class RegisterPresenter implements RegisterContract.Presenter {
    private final static String TAG = RegisterPresenter.class.getCanonicalName();

    private RegisterFragment mFragment;

    private Repository mRepository;

    public RegisterPresenter(Repository instance, RegisterFragment registerFragment) {
        mRepository = checkNotNull(instance);
        mFragment = checkNotNull(registerFragment);
        registerFragment.setPresenter(this);
    }



    @Override
    public void start() {

    }

    @Override
    public void getTelCode(String tel) {
        mRepository.asynVerification(tel, new SeniorCallBack() {
            @Override
            public void success() {
                mFragment.showTip("正在获取验证码……");
            }

            @Override
            public void fail() {
                mFragment.showTip("获取验证码失败");
            }

            @Override
            public void timeout() {
                mFragment.showTip("消息发送超时");
            }

            @Override
            public void invalid() {
                mFragment.showTip("号码无效");
            }
        });
    }

    @Override
    public void register(final User user, String code) {
        user.setDeviceId(CacheRepository.getInstance().getDeviceId());
        user.setType(1);
        mRepository.asynRegister(user, code, new SeniorCallBack() {
            @Override
            public void success() {
                mFragment.showTip("用户\""+user.getName()+"\"注册成功");
            }

            @Override
            public void fail() {
                mFragment.showTip("用户\""+user.getName()+"\"注册失败");
            }

            @Override
            public void timeout() {
                mFragment.showTip("连接超时");
            }

            @Override
            public void invalid() {
                mFragment.showTip("用户信息无效");
            }
        });
    }
}
