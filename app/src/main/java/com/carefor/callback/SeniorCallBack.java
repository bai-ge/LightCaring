package com.carefor.callback;

import android.util.Log;

import com.carefor.data.entity.Candidate;
import com.carefor.data.entity.Housekeeping;
import com.carefor.data.entity.Location;
import com.carefor.data.entity.User;

import java.util.List;

/**
 * Created by baige on 2018/2/13.
 */

public class SeniorCallBack extends BaseCallBack {

    private final static String TAG = SeniorCallBack.class.getCanonicalName();

    private AbstractResponseBinder mResponseBinder;

    @Override
    public void setResponseBinder(AbstractResponseBinder responseBinder) {
        this.mResponseBinder = responseBinder;
    }

    /*初级接口部分*/
    @Override
    public void timeout() {

    }

    /**
     * @param json
     */
    @Override
    public final void response(String json) {

        /*
        * TODO 通用解析器
        * 需要根据服务器返回的json 数据，调用本身不同的函数，解决持续通信问题
        * */
        if(mResponseBinder != null){
            mResponseBinder.parse(json, this);
        }
    }

    @Override
    public void error(Exception e) {

    }

    @Override
    public void onFinish() {

    }

    /*
        返回码接口部分
        */
    @Override
    public void success() {
        Log.d(TAG, "调用错了");
    }

    @Override
    public void fail() {

    }

    @Override
    public void unknown() {

    }

    @Override
    public void notFind() {

    }

    @Override
    public void typeConvert() {

    }

    @Override
    public void exist() {

    }

    @Override
    public void isBlank() {

    }

    @Override
    public void invalid() {

    }

    /*复杂接口部分*/
    @Override
    public void meaning(String text) {

    }

    @Override
    public void onResponse() {

    }

    public void loadUsers(List<User> list) {

    }


    public void loadAUser(User user) {

    }

    @Override
    public void loadLocation(Location loc) {

    }

    @Override
    public void loadLocations(List<Location> locationList) {

    }

    @Override
    public void loadHousekeeping(Housekeeping housekeeping) {

    }

    @Override
    public void loadHousekeepings(List<Housekeeping> housekeepingList) {

    }

    @Override
    public void receiveMessage(String message) {

    }

    @Override
    public void loadCandidate(Candidate candidate) {

    }
}
