package com.carefor.telephone;


import android.util.Log;

import com.carefor.connect.Connector;
import com.carefor.data.entity.DeviceModel;
import com.carefor.data.entity.Transinformation;
import com.carefor.data.source.Repository;
import com.carefor.data.source.cache.CacheRepository;

import java.util.Timer;
import java.util.TimerTask;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/10/29.
 */

public class PhonePresenter implements PhoneContract.Presenter {
    private final static String TAG = PhonePresenter.class.getCanonicalName();
    private PhoneFragment mPhonefragment;
    private Repository mRepository;
    private TelePhone mTelePhone;
    private Timer mHeartBeatTimer;
    private TimerTask mHeartBeatTask;

    public PhonePresenter(Repository repository, PhoneFragment phoneFragment) {
        mRepository = checkNotNull(repository);
        mPhonefragment = checkNotNull(phoneFragment);
        mPhonefragment.setPresenter(this);

    }


    @Override
    public void start() {
        Connector connector = Connector.getInstance();
        CacheRepository cacheRepository = CacheRepository.getInstance();

        mPhonefragment.showLog("device id=" + cacheRepository.getDeviceId());
        mPhonefragment.showLog("call to="+ cacheRepository.getEmergencyUser().getDeviceId());
        mPhonefragment.showLog("通话服务器地址："+cacheRepository.getServerIp()+":"+cacheRepository.getServerPort());

        //当通话界面在前台显示的时候连接器数据直接反馈回这里
        //当被切换出去是，启动服务，连接器的数据反馈回服务
        connector.RegistConnectorListener("presenter", mOnConnectorListener);

        if (!connector.isConnectServer()) {
            connector.afxConnectServer();
        }
        initPhoneLayout();
    }

    private void initPhoneLayout() {
        TelePhone telePhone = TelePhone.getInstance();
        DeviceModel deviceModel;
        deviceModel = CacheRepository.getInstance().getTalkWithDevice();
        if(deviceModel != null){
            mPhonefragment.showLog("对方设备");
            deviceModel = CacheRepository.getInstance().getTalkWithDevice();
            mPhonefragment.showLog("Local:"+deviceModel.getLocalIp()+":"+deviceModel.getLocalUdpPort());
            mPhonefragment.showLog("Remote:"+deviceModel.getRemoteIp()+":"+deviceModel.getRemoteUdpPort());
            mPhonefragment.showAddress(deviceModel.getRemoteIp()+":"+deviceModel.getRemoteUdpPort());
        }else{
            mPhonefragment.showAddress(CacheRepository.getInstance().getServerIp()+":"+CacheRepository.getInstance().getServerUdpPort());
        }

        if(CacheRepository.getInstance().isP2PConnectSuccess()){
            mPhonefragment.showLog("P2P连接建立成功");
            mPhonefragment.showAddress(CacheRepository.getInstance().getP2PIp()+":"+CacheRepository.getInstance().getP2PPort());

        }

        switch (telePhone.getStatus()) {
            case TelePhone.Status.LEISURE:
                mPhonefragment.showStatus("空闲");
                mPhonefragment.showLog("空闲");
                break;
            case TelePhone.Status.CALLING:
                mPhonefragment.hidePickUpBtn();
                mPhonefragment.showStatus("正在呼叫中");
                mPhonefragment.showLog("正在呼叫中");
                mPhonefragment.showName(CacheRepository.getInstance().getTalkWith());
                break;
            case TelePhone.Status.CALLED:
                mPhonefragment.showName(CacheRepository.getInstance().getTalkWith());
                mPhonefragment.showStatus("被呼叫中");
                mPhonefragment.showLog("被呼叫中");

                break;
            case TelePhone.Status.BUSY:
                mPhonefragment.showStatus("通话中");
                mPhonefragment.showLog("通话中");
                break;
            case TelePhone.Status.ERROR:
                mPhonefragment.showStatus("错误");
                mPhonefragment.showLog("错误");
                break;
        }
        if(CacheRepository.getInstance().getEmergencyUser() != null){
            mPhonefragment.showName(CacheRepository.getInstance().getEmergencyUser().getName());
        }
        //TODO 先显示当前的一些状态再设置监听器

        telePhone.setOnTelePhoneListener(mTelePhoneListener);
    }

    @Override
    public void onHangUp() {
        mPhonefragment.showLog("您已挂断电话");
        TelePhone.getInstance().onHangUp(new TelePhoneAPI.BaseCallBackAdapter(){});
        CacheRepository.getInstance().setP2PConnectSuccess(false);
        mPhonefragment.close();
    }


    @Override
    public void onPickUp() {
        //TODO 远程服务器执行接听指令，正确连接之后修改状态
        mPhonefragment.showLog("您已接听电话");
       TelePhone.getInstance().onPickUp(new TelePhoneAPI.BaseCallBackAdapter(){});
    }

    private TelePhone.OnTelePhoneListener mTelePhoneListener = new TelePhone.OnTelePhoneListener() {

        @Override
        public void showTip(String text) {
            mPhonefragment.showTip(text);
        }

        @Override
        public void showLog(String text) {
            mPhonefragment.showLog(text);
        }

        @Override
        public void exceptionCaught(Throwable cause) {
            mPhonefragment.showLog("异常:" + cause.getMessage());
        }
    };

    private Connector.OnConnectorListener mOnConnectorListener = new Connector.OnConnectorListener() {
        @Override
        public void beCalled(DeviceModel deviceModel) {
            Log.d(TAG, "执行者接收到呼叫信息");
            mPhonefragment.showAddress("Local:"+deviceModel.getLocalIp()+":"+deviceModel.getLocalUdpPort()+"" +
                    "Remote:"+deviceModel.getRemoteIp()+":"+deviceModel.getRemoteUdpPort());
            mPhonefragment.showName(deviceModel.getDeviceidId());
            mPhonefragment.showStatus("被呼叫中");
        }

        @Override
        public void pickUp(Transinformation tranfor) {
            DeviceModel deviceModel;
            deviceModel = CacheRepository.getInstance().getTalkWithDevice();
            if(deviceModel != null){
                mPhonefragment.showLog("对方设备");
                deviceModel = CacheRepository.getInstance().getTalkWithDevice();
                mPhonefragment.showLog("Local:"+deviceModel.getLocalIp()+":"+deviceModel.getLocalUdpPort());
                mPhonefragment.showLog("Remote:"+deviceModel.getRemoteIp()+":"+deviceModel.getRemoteUdpPort());
                mPhonefragment.showAddress(deviceModel.getRemoteIp()+":"+deviceModel.getRemoteUdpPort());
            }
            mPhonefragment.showLog("对方已经接听" );
        }

        @Override
        public void handUp(Transinformation tranfor) {
            mPhonefragment.showLog("对方已经挂断" );
            mPhonefragment.close();
        }

        @Override
        public void connectSuccess() {
            CacheRepository cacheRepository = CacheRepository.getInstance();
            mPhonefragment.showLog("成功连接服务器:" + cacheRepository.getServerIp() + ":" + cacheRepository.getServerPort());
        }

        @Override
        public void loginSuccess() {
            CacheRepository cacheRepository = CacheRepository.getInstance();
            mPhonefragment.showLog("登录成功");
            mPhonefragment.showLog("localAddress=" + cacheRepository.getLocalIp() + ":" + cacheRepository.getLocalPort());
            mPhonefragment.showLog("remoteAddress=" + cacheRepository.getRemoteIp() + ":" + cacheRepository.getRemotePort());
        }

        @Override
        public void receviceP2P(String device_id, String ip, int port) {
            DeviceModel deviceModel;
            deviceModel = CacheRepository.getInstance().getTalkWithDevice();
            if(deviceModel != null){
                mPhonefragment.showLog("对方设备");
                deviceModel = CacheRepository.getInstance().getTalkWithDevice();
                mPhonefragment.showLog("Local:"+deviceModel.getLocalIp()+":"+deviceModel.getLocalUdpPort());
                mPhonefragment.showLog("Remote:"+deviceModel.getRemoteIp()+":"+deviceModel.getRemoteUdpPort());
                mPhonefragment.showAddress(deviceModel.getRemoteIp()+":"+deviceModel.getRemoteUdpPort());
            }
            mPhonefragment.showAddress(ip+":"+port);
            mPhonefragment.showLog("成功建立P2P连接");
        }
    };
}
