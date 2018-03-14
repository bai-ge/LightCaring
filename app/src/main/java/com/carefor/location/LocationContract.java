package com.carefor.location;

import com.baidu.mapapi.model.LatLng;
import com.carefor.BasePresenter;
import com.carefor.BaseView;
import com.carefor.data.entity.Location;

import java.util.List;

/**
 * Created by baige on 2018/1/2.
 */

public interface LocationContract {
    interface Presenter extends BasePresenter {
        void askLocation();
        void loadLocation();
        void stop();
    }

    interface View extends BaseView<Presenter> {
        void showTip(String text);
        void showInform(String text);
        void showPLocation(Location loc);
        void showPLocation(List<Location> locationList);

        void showTargetTrack(List<LatLng> loc);
        void setTargetDialog(String text);
        void showLocationDialog(LatLng ll);

    }
}
