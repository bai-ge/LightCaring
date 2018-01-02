package com.carefor.location;

import com.carefor.BasePresenter;
import com.carefor.BaseView;

/**
 * Created by baige on 2018/1/2.
 */

public interface LocationContract {
    interface Presenter extends BasePresenter {

    }

    interface View extends BaseView<Presenter> {
        void showTip(String text);

    }
}
