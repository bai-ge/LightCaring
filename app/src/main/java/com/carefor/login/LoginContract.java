package com.carefor.login;

import com.carefor.BasePresenter;
import com.carefor.BaseView;
import com.carefor.mainui.MainContract;

/**
 * Created by baige on 2017/12/22.
 */

public interface LoginContract {

    interface Presenter extends BasePresenter{
        void login(String name, String psw);
        void skipToRegist();
        void skipToHome();
    }

    interface View extends BaseView<Presenter>{
        void showName(String name);
        void setPsw(String psw);
        void showTip(String text);
        void finishActivity();
    }
}
