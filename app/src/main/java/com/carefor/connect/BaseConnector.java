package com.carefor.connect;

/**
 * Created by baige on 2018/3/21.
 */

public abstract class BaseConnector {
    private String deviceId;

    protected int tryConnectCount = 0;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getTryConnectCount() {
        return tryConnectCount;
    }

    public void setTryConnectCount(int tryConnectCount) {
        this.tryConnectCount = tryConnectCount;
    }

    public abstract void connect();
    public abstract void disconnect();

    public abstract boolean isConnected();

    public abstract boolean isConnected(int lastReceiveTime);

    public abstract boolean isDisconnected();

    public abstract boolean isConnecting();

    public abstract boolean isDisconnecting();

    protected abstract void sendHeartBeat();
    public abstract SocketClientAddress getAddress();
    public abstract BaseConnector registerConnectedListener(OnConnectedListener listener);
    public abstract BaseConnector unRegisterConnectedListener(OnConnectedListener listener);
    public abstract BaseConnector registerSendingListener(OnSocketSendingListener listener);
    public abstract BaseConnector unRegiserSendingListener(OnSocketSendingListener listener);
    public abstract BaseConnector registerReceivingListener(OnSocketReceivingListener listener);
    public abstract BaseConnector unRegiserReceivingListener(OnSocketReceivingListener listener);
    public abstract SocketPacket sendPacket(SocketPacket packet);
    public abstract SocketPacket sendData(byte[] content);
    public abstract SocketPacket sendData(byte[] heart, byte[] content);
    public abstract SocketPacket sendString(String message);
}
