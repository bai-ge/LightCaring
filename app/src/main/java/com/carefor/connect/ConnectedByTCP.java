package com.carefor.connect;



import android.support.annotation.NonNull;
import android.util.Log;


import com.carefor.util.Loggerx;
import com.carefor.util.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by baige on 2018/3/20.
 */

public class ConnectedByTCP extends BaseConnector {

    public static final String TAG = ConnectedByTCP.class.getSimpleName();

    final ConnectedByTCP self = this;

    private SocketClientAddress address;

    private Socket runningSocket;

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

    private ConnectionThread connectionThread;

    private DisconnectionThread disconnectionThread;

    private SendThread sendThread;

    private ReceiveThread receiveThread;

    private ArrayList<OnConnectedListener> connectedListeners;
    private final Object conListenerLock = new Object();

    private ArrayList<OnSocketSendingListener> sendingListeners;
    private final Object sendingListenerLock = new Object();

    private ArrayList<OnSocketReceivingListener> receivingListeners;
    private final Object receivingListenerLock = new Object();

    private boolean disconnecting;

    private LinkedBlockingQueue<SocketPacket> sendingPacketQueue;

    private SocketPacket sendingPacket;

    private SocketPacket receivingPacket;

    private SocketInputReader socketInputReader;

    private InputStream socketInputStream;

    private int heartBeatTime = 30000; //每 30 s 发送一次心跳包

    private TimerTask heartBeatTask;

    private Timer heartBeatTimer;


    /**
     * 当前连接状态
     * 当设置状态为{@link State#Connected}, 收发线程等初始操作均未启动
     * 此状态仅为一个标识
     */
    private State state;

    public enum State {
        Disconnected, Connecting, Connected
    }

    /*构造方法*/
    public ConnectedByTCP() {
        this(new SocketClientAddress());
    }

    public ConnectedByTCP(Socket socket){
        if(socket == null || socket.isClosed()){
            throw new IllegalArgumentException("we need a Socket to connect");
        }
        setAddress(new SocketClientAddress(socket.getRemoteSocketAddress().toString().substring(1), socket.getPort()));
        setRunningSocket(socket);
        setState(ConnectedByTCP.State.Connected);

        setLastSendHeartBeatMessageTime(System.currentTimeMillis());
        setLastReceiveMessageTime(System.currentTimeMillis());
        setLastSendMessageTime(NoSendingTime);

        setSendingPacket(null);
        setReceivingResponsePacket(null);
        mOnConnectedListener.onConnected(this);
        getSendThread().start();
        getReceiveThread().start();
        startHeartBeatTask();
    }

    public ConnectedByTCP(SocketClientAddress address) {
        this.address = address;
    }


    public SocketClientAddress getAddress() {
        return address;
    }

    public void setAddress(SocketClientAddress address) {
        this.address = address;
    }

    public Socket getRunningSocket() {
        if (this.runningSocket == null) {
            this.runningSocket = new Socket();
        }
        return this.runningSocket;
    }

    public ConnectedByTCP setRunningSocket(Socket socket) {
        this.runningSocket = socket;
        return this;
    }

    public long getLastReceiveMessageTime() {
        return lastReceiveMessageTime;
    }

    protected ConnectedByTCP setLastReceiveMessageTime(long lastReceiveMessageTime) {
        this.lastReceiveMessageTime = lastReceiveMessageTime;
        return this;
    }

    public long getLastSendHeartBeatMessageTime() {
        return lastSendHeartBeatMessageTime;
    }

    protected ConnectedByTCP setLastSendMessageTime(long lastSendMessageTime) {
        this.lastSendMessageTime = lastSendMessageTime;
        return this;
    }

    public long getLastSendMessageTime() {
        return this.lastSendMessageTime;
    }

    protected ConnectedByTCP setLastSendHeartBeatMessageTime(long lastSendHeartBeatMessageTime) {
        this.lastSendHeartBeatMessageTime = lastSendHeartBeatMessageTime;
        return this;
    }

    protected ConnectedByTCP setState(State state) {
        this.state = state;
        return this;
    }

    public State getState() {
        if (this.state == null) {
            return State.Disconnected;
        }
        return this.state;
    }

    protected LinkedBlockingQueue<SocketPacket> getSendingPacketQueue() {
        if (sendingPacketQueue == null) {
            sendingPacketQueue = new LinkedBlockingQueue<SocketPacket>();
        }
        return sendingPacketQueue;
    }

    protected ConnectedByTCP setSocketInputReader(SocketInputReader socketInputReader) {
        this.socketInputReader = socketInputReader;
        return this;
    }
    protected SocketInputReader getSocketInputReader() throws IOException {
        if (this.socketInputReader == null) {
            this.socketInputReader = new SocketInputReader(getRunningSocket().getInputStream());
        }
        return this.socketInputReader;
    }

    protected InputStream getSocketInputStream() throws IOException {
        if (this.socketInputStream == null) {
            this.socketInputStream = getRunningSocket().getInputStream();
        }
        return this.socketInputStream;
    }

    /**
     * @param inputStream
     * @return
     */
    protected  ConnectedByTCP setSocketInputStream(InputStream inputStream){
        this.socketInputStream = inputStream;
        return this;
    }

    public boolean isConnected() {
        return getState() == State.Connected;
    }

    protected ConnectedByTCP setDisconnecting(boolean disconnecting) {
        this.disconnecting = disconnecting;
        return this;
    }

    public boolean isDisconnected() {
        return getState() == State.Disconnected;
    }

    @Override
    public boolean isConnected(int lastReceiveTime) {
        if(System.currentTimeMillis() - getLastReceiveMessageTime() > lastReceiveTime){
            return false;
        }
        return isDisconnected();
    }

    public boolean isConnecting() {
        return getState() == State.Connecting;
    }

    public boolean isDisconnecting() {
        return this.disconnecting;
    }

    protected ArrayList<OnConnectedListener> getConnectedListeners() {
        if (this.connectedListeners == null) {
            this.connectedListeners = new ArrayList<>();
        }
        return this.connectedListeners;
    }
    protected ArrayList<OnSocketSendingListener> getSendingListeners() {
        if (this.sendingListeners == null) {
            this.sendingListeners = new ArrayList<>();
        }
        return this.sendingListeners;
    }
    protected ArrayList<OnSocketReceivingListener> getReceivingListeners() {
        if (this.receivingListeners == null) {
            this.receivingListeners = new ArrayList<>();
        }
        return this.receivingListeners;
    }

    private long lastConnectTime = 0;
    @Override
    public void connect() {
        if (!isDisconnected()) {
            return;
        }

        if (getAddress() == null) {
            throw new IllegalArgumentException("we need a SocketClientAddress to connect");
        }
        if(System.currentTimeMillis() - lastConnectTime <= 8000){
            return;
        }
        lastConnectTime = System.currentTimeMillis();
        getAddress().checkValidation();
        setState(State.Connecting);
        tryConnectCount ++;
        getConnectionThread().start();
    }

    @Override
    public void disconnect() {
        if (isDisconnected() || isDisconnecting()) {
            return;
        }
        setDisconnecting(true);
        getDisconnectionThread().start();
    }

    @Override
    protected void sendHeartBeat() {
        if(!isConnected()){
            return ;
        }
        long lastSendTime = Math.max(getLastSendMessageTime(), getLastSendHeartBeatMessageTime());
        if(System.currentTimeMillis() - lastSendTime >= getHeartBeatTime()){
            SocketPacket packet = new SocketPacket();
            sendPacket(packet);
        }

    }

    public int getHeartBeatTime() {
        return heartBeatTime;
    }

    public void setHeartBeatTime(int heartBeatTime) {
        this.heartBeatTime = heartBeatTime;
    }

    @Override
    public BaseConnector registerConnectedListener(OnConnectedListener listener) {
        if (!getConnectedListeners().contains(listener)) {
            synchronized (conListenerLock) {
                if (!getConnectedListeners().contains(listener)) {
                    getConnectedListeners().add(listener);
                }
            }
        }
        return this;
    }

    @Override
    public BaseConnector unRegisterConnectedListener(OnConnectedListener listener) {
        synchronized (conListenerLock) {
            getConnectedListeners().remove(listener);
        }
        return this;
    }

    @Override
    public BaseConnector registerSendingListener(OnSocketSendingListener listener) {
        if (!getSendingListeners().contains(listener)) {
            synchronized (sendingListenerLock) {
                if (!getSendingListeners().contains(listener)) {
                    getSendingListeners().add(listener);
                }
            }
        }
        return this;
    }

    @Override
    public BaseConnector unRegiserSendingListener(OnSocketSendingListener listener) {
        synchronized (sendingListenerLock) {
            getSendingListeners().remove(listener);
        }
        return this;
    }

    @Override
    public BaseConnector registerReceivingListener(OnSocketReceivingListener listener) {
        if (!getReceivingListeners().contains(listener)) {
            synchronized (receivingListenerLock) {
                if (!getReceivingListeners().contains(listener)) {
                    getReceivingListeners().add(listener);
                }
            }
        }
        return this;
    }

    @Override
    public BaseConnector unRegiserReceivingListener(OnSocketReceivingListener listener) {
        synchronized (receivingListenerLock) {
            getReceivingListeners().remove(listener);
        }
        return this;
    }

    @Override
    public SocketPacket sendPacket(final SocketPacket packet) {
        if (!isConnected()) {
            return null;
        }
        if (packet == null) {
            return null;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                self.__i__enqueueNewPacket(packet);
            }
        }).start();
        return packet;
    }

    @Override
    public SocketPacket sendData(byte[] content) {
        if (!isConnected()) {
            return null;
        }
        if(content == null || content.length == 0){
            return null;
        }
        SocketPacket packet = new SocketPacket(content, false);
        return sendPacket(packet);
    }

    @Override
    public SocketPacket sendData(byte[] heart, byte[] content) {
        if (!isConnected()) {
            return null;
        }
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
        if (!isConnected()) {
            return null;
        }
        return sendData(Tools.stringToData(message, Tools.DEFAULT_ENCODE));
    }

    private   void startHeartBeatTask(){
        synchronized (ConnectedByTCP.class){
            if(heartBeatTask == null){
                heartBeatTask = new TimerTask() {
                    @Override
                    public void run() {
                        sendHeartBeat();
                    }
                };
            }
            if(heartBeatTimer == null ){
                heartBeatTimer = new Timer();
                heartBeatTimer.schedule(heartBeatTask, 0, heartBeatTime);
            }
        }

    }
    private  void stopHeartBeatTask(){
        synchronized (ConnectedByTCP.class){
            if(heartBeatTask != null){
                heartBeatTask.cancel();
                heartBeatTask = null;
            }
            if(heartBeatTimer != null){
                heartBeatTimer.cancel();
                heartBeatTimer = null;
            }
        }
    }

    //取消不确定调用顺序的函数
//    protected void internalOnConnected() {
//        setState(State.Connected);
//
//        setLastSendHeartBeatMessageTime(System.currentTimeMillis());
//        setLastReceiveMessageTime(System.currentTimeMillis());
//        setLastSendMessageTime(NoSendingTime);
//        setSendingPacket(null);
//        setReceivingResponsePacket(null);
//        startHeartBeatTask();
//        mOnConnectedListener.onConnected(self);
//    }

    private OnConnectedListener mOnConnectedListener = new OnConnectedListener() {
        @Override
        public void onConnected(BaseConnector connector) {
            for (OnConnectedListener listener : getConnectedListeners()){
                listener.onConnected(connector);
            }
        }

        @Override
        public void onDisconnected(BaseConnector connector) {
            for (OnConnectedListener listener : getConnectedListeners()){
                listener.onDisconnected(connector);
            }
        }

        @Override
        public void onResponse(BaseConnector connector, @NonNull SocketPacket responsePacket) {
            for (OnConnectedListener listener : getConnectedListeners()){
                listener.onResponse(connector, responsePacket);
            }
        }
    };

    private OnSocketSendingListener mOnSendingListener = new OnSocketSendingListener() {
        @Override
        public void onSendPacketBegin(BaseConnector connector, SocketPacket packet) {
            for(OnSocketSendingListener listener : getSendingListeners()){
                listener.onSendPacketBegin(connector, packet);
            }
        }

        @Override
        public void onSendPacketEnd(BaseConnector connector, SocketPacket packet) {
            for(OnSocketSendingListener listener : getSendingListeners()){
                listener.onSendPacketEnd(connector, packet);
            }
        }

        @Override
        public void onSendPacketCancel(BaseConnector connector, SocketPacket packet) {
            for(OnSocketSendingListener listener : getSendingListeners()){
                listener.onSendPacketCancel(connector, packet);
            }
        }

        @Override
        public void onSendingPacketInProgress(BaseConnector connector, SocketPacket packet, float progress, int sendedLength) {
            for(OnSocketSendingListener listener : getSendingListeners()){
                listener.onSendingPacketInProgress(connector, packet, progress, sendedLength);
            }
        }
    };
    private OnSocketReceivingListener mOnSocketReceivingListener = new OnSocketReceivingListener() {
        @Override
        public void onReceivePacketBegin(BaseConnector connector, SocketPacket packet) {
            for(OnSocketReceivingListener listener : getReceivingListeners()){
                listener.onReceivePacketBegin(connector, packet);
            }
        }

        @Override
        public void onReceivePacketEnd(BaseConnector connector, SocketPacket packet) {
            for(OnSocketReceivingListener listener : getReceivingListeners()){
                listener.onReceivePacketEnd(connector, packet);
            }
        }

        @Override
        public void onReceivePacketCancel(BaseConnector connector, SocketPacket packet) {
            for(OnSocketReceivingListener listener : getReceivingListeners()){
                listener.onReceivePacketCancel(connector, packet);
            }
        }

        @Override
        public void onReceivingPacketInProgress(BaseConnector connector, SocketPacket packet, float progress, int receivedLength) {
            for(OnSocketReceivingListener listener : getReceivingListeners()){
                listener.onReceivingPacketInProgress(connector, packet, progress, receivedLength);
            }
        }
    };



    private void __i__enqueueNewPacket(final SocketPacket packet) {
        if (!isConnected()) {
            return;
        }
        synchronized (getSendingPacketQueue()) {
            try {
                getSendingPacketQueue().put(packet);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected ConnectedByTCP setConnectionThread(ConnectionThread connectionThread) {
        this.connectionThread = connectionThread;
        return this;
    }

    protected ConnectionThread getConnectionThread() {
        if (this.connectionThread == null) {
            this.connectionThread = new ConnectionThread();
        }
        return this.connectionThread;
    }

    protected ConnectedByTCP setDisconnectionThread(DisconnectionThread disconnectionThread) {
        this.disconnectionThread = disconnectionThread;
        return this;
    }

    protected DisconnectionThread getDisconnectionThread() {
        if (this.disconnectionThread == null) {
            this.disconnectionThread = new DisconnectionThread();
        }
        return this.disconnectionThread;
    }

    protected ConnectedByTCP setSendThread(SendThread sendThread) {
        this.sendThread = sendThread;
        return this;
    }

    protected SendThread getSendThread() {
        if (this.sendThread == null) {
            this.sendThread = new SendThread();
        }
        return this.sendThread;
    }

    protected ConnectedByTCP setReceiveThread(ReceiveThread receiveThread) {
        this.receiveThread = receiveThread;
        return this;
    }

    protected ReceiveThread getReceiveThread() {
        if (this.receiveThread == null) {
            this.receiveThread = new ReceiveThread();
        }
        return this.receiveThread;
    }

    protected ConnectedByTCP setReceivingResponsePacket(SocketPacket receivingResponsePacket) {
        this.receivingPacket = receivingResponsePacket;
        return this;
    }
    protected SocketPacket getReceivingResponsePacket() {
        return this.receivingPacket;
    }

    protected ConnectedByTCP setSendingPacket(SocketPacket sendingPacket) {
        this.sendingPacket = sendingPacket;
        return this;
    }
    protected SocketPacket getSendingPacket() {
        return this.sendingPacket;
    }

    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            super.run();
            try{
                SocketClientAddress address = self.getAddress();
                if(Thread.interrupted()){
                    return;
                }
                Socket socket = self.getRunningSocket();
                Log.d(TAG, "套接字："+socket);
                Log.d(TAG, "服务器地址"+address.getStringRemoteAddress());
                Log.d(TAG, "超时："+address.getConnectionTimeout());
                socket.connect(address.getInetSocketAddress(), address.getConnectionTimeout());
                if(Thread.interrupted()){
                    return;
                }
                setTryConnectCount(0);
                self.setState(ConnectedByTCP.State.Connected);
                self.setLastSendHeartBeatMessageTime(System.currentTimeMillis());
                self.setLastReceiveMessageTime(System.currentTimeMillis());
                self.setLastSendMessageTime(NoSendingTime);

                self.setSendingPacket(null);
                self.setReceivingResponsePacket(null);

                mOnConnectedListener.onConnected(self);
                getSendThread().start();
                getReceiveThread().start();
                startHeartBeatTask();

                self.setConnectionThread(null);

            }catch (IOException e){
                e.printStackTrace();
                Loggerx.e(TAG, e.getMessage());
                self.disconnect();
            }

        }
    }

    private class DisconnectionThread extends Thread {
        @Override
        public void run() {
            super.run();

            //停止连接线程
            if (self.connectionThread != null) {
                self.getConnectionThread().interrupt();
                self.setConnectionThread(null);
            }
            //关闭心跳包
            stopHeartBeatTask();

            //关闭网络流
            if (!self.getRunningSocket().isClosed() || self.isConnecting()) {
                try {
                    self.getRunningSocket().getOutputStream().close();
                    self.getRunningSocket().getInputStream().close();
                }
                catch (IOException e) {
//                e.printStackTrace();
                }
                finally {
                    try {
                        self.getRunningSocket().close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    self.setRunningSocket(null);
                }
            }
            if (self.sendThread != null) {
                self.getSendThread().interrupt();
                self.setSendThread(null);
            }
            if (self.receiveThread != null) {
                self.getReceiveThread().interrupt();
                self.setReceiveThread(null);
            }

            self.setDisconnecting(false);
            self.setState(ConnectedByTCP.State.Disconnected);


            self.setSocketInputReader(null);
            self.setSocketInputStream(null);
//          self.setSocketConfigure(null);

            //TODO 取消正在发送和正在接收的包
            if (self.getSendingPacket() != null) {
                mOnSendingListener.onSendPacketCancel(self, self.getSendingPacket());
                self.setSendingPacket(null);
            }

            SocketPacket packet;
            while ((packet = self.getSendingPacketQueue().poll()) != null) {
                mOnSendingListener.onSendPacketCancel(self, packet);
            }

            if (self.getReceivingResponsePacket() != null) {
                mOnSocketReceivingListener.onReceivePacketCancel(self, self.getReceivingResponsePacket());
                self.setReceivingResponsePacket(null);
            }

            self.setDisconnectionThread(null);
            mOnConnectedListener.onDisconnected(self);
        }
    }

    private class SendThread extends Thread {
        @Override
        public void run() {
            super.run();

            SocketPacket packet;
            try{
                while(self.isConnected()
                        && !Thread.interrupted()
                        && (packet = self.getSendingPacketQueue().take()) != null){
                    self.setSendingPacket(packet);
                    if(!packet.isPacket()){
                        packet.packet();
                    }
                    if(packet.getAllBuf() != null && packet.getAllBuf().length > 0){
                        try {
                            self.getRunningSocket().getOutputStream().write(packet.getAllBuf());
                            self.getRunningSocket().getOutputStream().flush();
                            self.setSendingPacket(null);
                            if(packet.isHeartBeat()){
                                self.setLastSendHeartBeatMessageTime(System.currentTimeMillis());
                            }else{
                                self.setLastSendMessageTime(System.currentTimeMillis());
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                            if (self.getSendingPacket() != null) {
                                mOnSendingListener.onSendPacketCancel(self, self.getSendingPacket());
                                self.setSendingPacket(null);
                            }
                            Loggerx.e(TAG, e.getMessage());
                        }
                    }
                }
            }catch (InterruptedException e){
                Log.d(TAG, "发送线程异常"+e.getMessage());
                e.printStackTrace();
                if (self.getSendingPacket() != null) {
                    mOnSendingListener.onSendPacketCancel(self, self.getSendingPacket());
                    self.setSendingPacket(null);
                }
            }
        }
    }

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();
            try{
                while (self.isConnected()
                        && self.getSocketInputStream() != null
                        && !Thread.interrupted()){
                    SocketPacket packet =  new SocketPacket();
                    self.setReceivingResponsePacket(packet);

                    int h = self.getSocketInputStream().read();
                    if(h == -1 ){
                        throw new IOException("InputStreamReader is closed");
                    }
                    mOnSocketReceivingListener.onReceivePacketBegin(self, packet);
                    if((h & 0xFF) == SocketPacket.SIGN_HEADER){
                        int headerSize = self.getSocketInputStream().read();
                        int contentSize = SocketPacket.bufToLen(self.getSocketInputStream());

                        //实际长度还应该包含整个包的长度，但这里只计算包里面包含的信息长度
                        mOnSocketReceivingListener.onReceivingPacketInProgress(self, packet, 0f, headerSize + contentSize );

                        if(headerSize > 0){
                            byte[] header = new byte[headerSize];
                            //TODO 每过百分之一通知一次
                            int size = self.getSocketInputStream().read(header);
                            if(size == headerSize){
                                packet.setHeaderBuf(header);
                            }else{
                                Loggerx.d(TAG, "It isn't a SocketPacket!");
                                mOnSocketReceivingListener.onReceivePacketCancel(self, packet);
                                self.setReceivingResponsePacket(null);
                            }
                        }
                        mOnSocketReceivingListener.onReceivingPacketInProgress(self, packet, (float) ((1.0 * headerSize) / (headerSize + contentSize)), headerSize + contentSize );
                        h = self.getSocketInputStream().read();
                        if((h & 0xFF) != SocketPacket.SIGN_MID){
                            Loggerx.d(TAG, "It isn't a SocketPacket!");
                            mOnSocketReceivingListener.onReceivePacketCancel(self, packet);
                            self.setReceivingResponsePacket(null);
                        }
                        if(contentSize > 0){
                            byte[] content = new byte[contentSize];
                            //TODO 每过百分之一通知一次
                            int size = self.getSocketInputStream().read(content);
                            if(size == contentSize){
                                packet.setContentBuf(content);
                            }else{
                                Loggerx.d(TAG, "It isn't a SocketPacket!");
                                mOnSocketReceivingListener.onReceivePacketCancel(self, packet);
                                self.setReceivingResponsePacket(null);
                            }
                        }
                        h = self.getSocketInputStream().read();
                        if((h & 0xFF) != SocketPacket.SIGN_END){
                            Loggerx.d(TAG, "It isn't a SocketPacket!");
                            mOnSocketReceivingListener.onReceivePacketCancel(self, packet);
                            self.setReceivingResponsePacket(null);
                        }
                        self.mOnSocketReceivingListener.onReceivePacketEnd(self, packet);
                    }else if((h & 0xFF) == SocketPacket.SIGN_END){
                        byte [] remainBuf = new byte[3];
                        int size = self.getSocketInputStream().read(remainBuf);
                        if (remainBuf[0] == SocketPacket.SIGN_END
                                && remainBuf[1] == SocketPacket.SIGN_END
                                && remainBuf[2] == SocketPacket.SIGN_END) {
                            packet.setHeartBeat(true);
                        } else if (remainBuf[0] == SocketPacket.SIGN_HEADER
                                && remainBuf[1] == SocketPacket.SIGN_END
                                && remainBuf[2] == SocketPacket.SIGN_HEADER) {
                            packet.setHeartBeat(false);
                            packet.setDisconnected(true);
                        }else{
                            packet.setHeartBeat(false);
                            packet.setDisconnected(false);
                            Loggerx.d(TAG, "It isn't a SocketPacket!");
                            mOnSocketReceivingListener.onReceivePacketCancel(self, packet);
                            self.setReceivingResponsePacket(null);
                        }
                        self.mOnSocketReceivingListener.onReceivePacketEnd(self, packet);
                    }else{
                        Loggerx.d(TAG, "It isn't a SocketPacket!");
                        mOnSocketReceivingListener.onReceivePacketCancel(self, packet);
                        self.setReceivingResponsePacket(null);
                        continue;
                    }
                    if(packet != null ){
                        setLastReceiveMessageTime(System.currentTimeMillis());
                        if(!packet.isHeartBeat()){
                            mOnConnectedListener.onResponse(self, packet);
                        }
                    }
                    self.setReceivingResponsePacket(null);
                }
            }catch (Exception e){
                e.printStackTrace();
                Loggerx.d(TAG, "信息接收线程停止运行"+e.getMessage());
                self.disconnect();
                if (self.getReceivingResponsePacket() != null) {
                    mOnSocketReceivingListener.onReceivePacketCancel(self, self.getReceivingResponsePacket());
                    self.setReceivingResponsePacket(null);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{");
        stringBuffer.append(" address:"+(getAddress() == null? "null" : getAddress().getStringRemoteAddress()));
        stringBuffer.append(", isConnected:"+ isConnected());
        stringBuffer.append(" }\n");
        return stringBuffer.toString();
    }
}
