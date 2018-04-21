package com.carefor.service;

import com.carefor.connect.SocketClientAddress;

/**
 * Created by baige on 2018/3/25.
 * 由于多进程的原因，该类只能由DamonService 初始化，其它非该进程的类都不能调用
 */

public class DaemonServiceRepository {

    private final static String TAG = DaemonServiceRepository.class.getCanonicalName();

    private static DaemonServiceRepository INSTANCE = null;//对获取实例的方法进行同步

    //网络相关
    private boolean wifiEnable; //是否打开wifi
    private boolean wifiValid; //WiFi网络是否可用

    private boolean networkValid; //手机网络是否可用

    private String deviceId;

    private SocketClientAddress serverAddress;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public SocketClientAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(SocketClientAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    private DaemonServiceRepository(){

    }

    public static DaemonServiceRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (DaemonServiceRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DaemonServiceRepository();
                }
            }
        }
        return INSTANCE;
    }

    public void setWifiEnable(boolean wifiEnable) {
        this.wifiEnable = wifiEnable;
    }
    public boolean isWifiEnable() {
        return wifiEnable;
    }

    public boolean isWifiValid() {
        return wifiValid;
    }

    public void setWifiValid(boolean wifiValid) {
        this.wifiValid = wifiValid;
    }

    public boolean isNetworkValid() {
        return networkValid;
    }

    public void setNetworkValid(boolean networkValid) {
        this.networkValid = networkValid;
    }


}
