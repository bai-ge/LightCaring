package com.carefor.mainui;

import com.carefor.BasePresenter;
import com.carefor.BaseView;
import com.carefor.data.entity.User;

/**
 * Created by baige on 2017/12/22.
 */

public interface MainContract {
    interface Presenter extends BasePresenter{
        void skipToActivity(Class content);
        void showDrawer();
        void callTo();
        void stop();

    }
    interface View extends BaseView<Presenter>{
        void showEmUser(User user);
        void showInform(String text);
        void showInform(String text, long time);
        void hideInform();
    }
}
