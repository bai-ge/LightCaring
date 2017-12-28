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

    }
    interface View extends BaseView<Presenter>{
        void showEmUser(User user);
    }
}
