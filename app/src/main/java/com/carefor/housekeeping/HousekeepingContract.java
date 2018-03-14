package com.carefor.housekeeping;

import com.carefor.BasePresenter;
import com.carefor.BaseView;
import com.carefor.data.entity.Housekeeping;

import java.util.List;

/**
 * Created by baige on 2018/3/13.
 */

public interface HousekeepingContract {

    interface Presenter extends BasePresenter {
        void search(String word);
    }
    interface View extends BaseView<Presenter> {
        void showTip(String text);
        void showHousekeepings(List<Housekeeping> users);
        void setRefreshing(boolean refresh);
    }
}
