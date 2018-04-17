package com.carefor.connect;


import com.carefor.util.Tools;

import java.net.DatagramPacket;
import java.net.SocketException;

/**
 * Created by baige on 2018/3/21.
 */

public class ConnectedByUDP extends BaseConnector {



    private SocketClientAddress address;

    private DatagramSocketServer runningSocket;

    /**
     * 记录上次接收到消息的时间
     */
    private long lastReceiveMessageTime;

    /**
     * 记录上次发送心跳包的时间
     */
    private long lastSendHeartBeatMessageTime;

    /**
     * 记录上次发送数据片段的时间
     * 仅在每个发送包开始发送时计时，结束后重置计时
     * NoSendingTime 表示当前没有在发送数据
     */
    private final static long NoSendingTime = -1;
    private long lastSendMessageTime = NoSendingTime;



    //当进入正在断开连接时，再收到对方的数据包时不再进入连接状态
    private boolean disconnecting;

    public ConnectedByUDP() {
        this(new SocketClientAddress());
    }



    /**
     * 当前连接状态
     * 当设置状态为{@link ConnectedByUDP.State#Connected}
     * 此状态仅为一个标识
     */
    private ConnectedByUDP.State state;

    public enum State {
        Disconnected, Connecting, Connected
    }

    protected ConnectedByUDP setState(ConnectedByUDP.State state) {
        this.state = state;
        return this;
    }

    public ConnectedByUDP.State getState() {
        if (this.state == null) {
            return ConnectedByUDP.State.Disconnected;
        }
        return this.state;
    }
    public boolean isConnected() {
        return getState() == ConnectedByUDP.State.Connected;
    }

    @Override
    public boolean isConnected(int lastReceiveTime) {
        if(System.currentTimeMillis() - getLastReceiveMessageTime() > lastReceiveTime){
            return false;
        }
        return isDisconnected();
    }
    public boolean isConnecting() {
        return getState() == ConnectedByUDP.State.Connecting;
    }
    public boolean isDisconnected() {
        return getState() == ConnectedByUDP.State.Disconnected;
    }

    protected ConnectedByUDP setDisconnecting(boolean disconnecting) {
        this.disconnecting = disconnecting;
        return this;
    }
    public boolean isDisconnecting() {
        return this.disconnecting;
    }

    public ConnectedByUDP(SocketClientAddress address) {
        this.address = address;
    }

    public SocketClientAddress getAddress() {
        return address;
    }

    public void setAddress(SocketClientAddress address) {
        this.address = address;
    }

    public DatagramSocketServer getRunningSocket() {
        return runningSocket;
    }

    public ConnectedByUDP setRunningSocket(DatagramSocketServer runningSocket) {
        this.runningSocket = runningSocket;
        return this;
    }

    @Override
    public void connect() {
        if(isConnected()){
            return;
        }
        if (getAddress() == null) {
            throw new IllegalArgumentException("we need a SocketClientAddress to connect");
        }

        if(getRunningSocket() == null){
            throw new IllegalArgumentException("we need a DatagramSocketServer to connect");
        }
        getAddress().checkValidation();
        setState(State.Connecting);
        setDisconnecting(false);
        //TODO 向远程发送连接请求，暂时使用心跳包代替
        sendConnectedPacket();
    }

    @Override
    public void disconnect() {
        //TODO 向远程发送断开请求
//        if(getRunningSocket() != null){
//            getRunningSocket().remove(this);
//        }
        if(getRunningSocket() == null){
            return;
        }
        setDisconnecting(true);
        sendDisconnectedPacket();
        //不再发送信息
        setRunningSocket(null);
        //TODO 清除更多的数据
    }

    @Override
    protected void sendHeartBeat() {
        if(isDisconnecting()){
            return;
        }
        if (getAddress() == null) {
            throw new IllegalArgumentException("we need a SocketClientAddress to connect");
        }

        if(getRunningSocket() == null){
            throw new IllegalArgumentException("we need a DatagramSocketServer to connect");
        }
        getAddress().checkValidation();
        SocketPacket packet = new SocketPacket();
       if(sendPacket(packet) != null){
            setLastSendHeartBeatMessageTime(System.currentTimeMillis());
       }
    }

    protected void sendConnectedPacket(){
        //TODO 暂时使用心跳包代替
        if (getAddress() == null) {
            throw new IllegalArgumentException("we need a SocketClientAddress to connect");
        }

        if(getRunningSocket() == null){
            throw new IllegalArgumentException("we need a DatagramSocketServer to connect");
        }
        getAddress().checkValidation();
        SocketPacket packet = new SocketPacket();
        packet.setHeartBeat(true);
        if(sendPacket(packet) != null){
            setLastSendMessageTime(System.currentTimeMillis());
        }
    }

    protected void sendDisconnectedPacket(){
        //TODO 暂时使用心跳包代替
        if (getAddress() == null) {
            throw new IllegalArgumentException("we need a SocketClientAddress to connect");
        }

        if(getRunningSocket() == null){
            throw new IllegalArgumentException("we need a DatagramSocketServer to connect");
        }
        getAddress().checkValidation();
        SocketPacket packet = new SocketPacket();
        packet.setDisconnected(true);
        if(sendPacket(packet) != null){
            setLastSendMessageTime(System.currentTimeMillis());
        }
    }

    @Override
    public BaseConnector registerConnectedListener(OnConnectedListener listener) {
        return null;
    }

    @Override
    public BaseConnector unRegisterConnectedListener(OnConnectedListener listener) {
        return null;
    }

    @Override
    public BaseConnector registerSendingListener(OnSocketSendingListener listener) {
        return null;
    }

    @Override
    public BaseConnector unRegiserSendingListener(OnSocketSendingListener listener) {
        return null;
    }

    @Override
    public BaseConnector registerReceivingListener(OnSocketReceivingListener listener) {
        return null;
    }

    @Override
    public BaseConnector unRegiserReceivingListener(OnSocketReceivingListener listener) {
        return null;
    }

    public long getLastReceiveMessageTime() {
        return lastReceiveMessageTime;
    }

    protected ConnectedByUDP setLastReceiveMessageTime(long lastReceiveMessageTime) {
        this.lastReceiveMessageTime = lastReceiveMessageTime;
        return this;
    }

    public long getLastSendHeartBeatMessageTime() {
        return lastSendHeartBeatMessageTime;
    }

    protected ConnectedByUDP setLastSendMessageTime(long lastSendMessageTime) {
        this.lastSendMessageTime = lastSendMessageTime;
        return this;
    }

    public long getLastSendMessageTime() {
        return this.lastSendMessageTime;
    }

    protected ConnectedByUDP setLastSendHeartBeatMessageTime(long lastSendHeartBeatMessageTime) {
        this.lastSendHeartBeatMessageTime = lastSendHeartBeatMessageTime;
        return this;
    }
    @Override
    public SocketPacket sendPacket(SocketPacket packet) {
        if(packet == null){
            return null;
        }
        if(getAddress() == null){
            return null;
        }
        if(getRunningSocket() == null){
            return null;
        }
        if(getAddress().isValid()){
            if(!packet.isPacket()){
                packet.packet();
            }
            DatagramPacket datagramPacket = null;
            try {
                datagramPacket = new DatagramPacket(packet.getAllBuf(), packet.getAllBuf().length, getAddress().getInetSocketAddress());
            } catch (SocketException e) {
                e.printStackTrace();
            }
            if(getRunningSocket() != null && datagramPacket != null){
                getRunningSocket().send(datagramPacket);
                return packet;
            }
        }
        return null;
    }

    @Override
    public SocketPacket sendData(byte[] content) {
        if(content == null || content.length == 0){
            return null;
        }
        SocketPacket packet = new SocketPacket(content, false);
        return sendPacket(packet);
    }

    @Override
    public SocketPacket sendData(byte[] heart, byte[] content) {
        SocketPacket packet = null;
        if(heart == null || heart.length == 0){
            return sendData(content);
        }
        if(content == null || content.length == 0){
            packet = new SocketPacket(heart, true);
            return sendPacket(packet);
        }
        packet = new SocketPacket(heart, content);
        return sendPacket(packet);
    }

    @Override
    public SocketPacket sendString(String message) {
        return sendData(Tools.stringToData(message, Tools.DEFAULT_ENCODE));
    }
}
