package com.carefor.telephone;


import com.carefor.BasePresenter;
import com.carefor.BaseView;

/**
 * Created by baige on 2017/10/29.
 */

public interface PhoneContract {
    interface Presenter extends BasePresenter {
        void onHangUp();

        void onPickUp();
    }

    interface View extends BaseView<Presenter> {
//        void showUser(User user);
//      void showAddress(User user);


        void showDelayTime(long delay);

        void showName(String name);

        void showAddress(String address);

        void showTip(String text);

        void showStatus(String text);

        void showLog(String text);

        void clearLog();

        void hidePickUpBtn();

        void close();
    }
}
