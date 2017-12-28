package com.carefor.register;

import com.carefor.BasePresenter;
import com.carefor.BaseView;
import com.carefor.data.entity.User;

/**
 * Created by baige on 2017/12/22.
 */

public interface RegisterContract {

    interface Presenter extends BasePresenter {
        void getTelCode(String tel);
        void register(User user, String code);
    }

    interface View extends BaseView<Presenter> {
        void showTip(String text);
    }
}
