package com.carefor.telephone;


/**
 * Created by baige on 2017/12/24.
 */

public interface TelePhoneAPI {
    interface BaseCallBack {
        void isBusy();

        void isErrorStatus();
    }

    class BaseCallBackFactory implements BaseCallBack {

        @Override
        public void isBusy() {

        }

        @Override
        public void isErrorStatus() {

        }
    }

    class BaseCallBackAdapter extends BaseCallBackFactory {

    }

    //状态
    boolean isLeisure();
    boolean isCalling();
    boolean beCalled();
    boolean isBusy();

    //TCP发出呼叫命令
    void afxCallTo(String deviceId, String name,  BaseCallBack callBack);

    void callTo(String deviceId, String name, BaseCallBack callBack);

    void afxBeCall(String deviceId, String name,  BaseCallBack callBack);

    void beCall(String deviceId, String name, BaseCallBack callBack);

    void connectSuccess();

    void oppBusy();

    //包括TCP发送指令 （界面使用这里）
    void onHangUp(BaseCallBack callBack);

    void onPickUp(BaseCallBack callBack);


    void onNetworkChange();

    //（消息接收器用这里）
    void canTalk();

    void stop();
}
