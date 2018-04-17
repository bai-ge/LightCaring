package com.carefor.connect;


import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by baige on 2018/3/23.
 */

public class DatagramSocketServer {

    final DatagramSocketServer self = this;

    private static final String TAG = DatagramSocketServer.class.getCanonicalName();

    private int localPort = -1;

    public final static int DEFAULT_LOCAL_PORT = 12059;

    private boolean isStart;

    private boolean isClosing;

    private final static int DATA_LEN = 1024;

    private DatagramSocket runningServerSocket;

    private Map<String, ConnectedByUDP> connectedByUDPmap;

    private LinkedBlockingQueue<DatagramPacket> sendingPacketQueue;

    private List<OnDatagramSocketServerListener> datagramSocketServerListeners;
    private final Object serverListenerLock = new Object();


    //TODO 可能这里不需要这些监听器
    private ArrayList<OnSocketSendingListener> sendingListeners;
    private final Object sendingListenerLock = new Object();

    private ArrayList<OnSocketReceivingListener> receivingListeners;
    private final Object receivingListenerLock = new Object();

    private SendThread sendThread;

    private ReceiveThread receiveThread;

    private CloseThread closeThread;

    private int heartBeatTime = 30000;

    private TimerTask heartBeatTask;

    private Timer heartBeatTimer;


    public DatagramSocketServer() {
        localPort = DEFAULT_LOCAL_PORT;
        connectedByUDPmap = Collections.synchronizedMap(new LinkedHashMap<String, ConnectedByUDP>());
    }
    public DatagramSocketServer(int localPort) {
        this();
        this.localPort = localPort;
    }

    private void startHeartBeatTask() {
        if (heartBeatTask == null) {
            heartBeatTask = new TimerTask() {
                @Override
                public void run() {
                    Iterator<Map.Entry<String, ConnectedByUDP>> it = connectedByUDPmap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, ConnectedByUDP> entry = it.next();
                        entry.getValue().sendHeartBeat();
                    }
                }
            };
        }
        if (heartBeatTimer == null) {
            heartBeatTimer = new Timer();
            heartBeatTimer.schedule(heartBeatTask, 0, heartBeatTime);
        }
    }

    private void stopHeartBeatTask() {
        if (heartBeatTask != null) {
            heartBeatTask.cancel();
            heartBeatTask = null;
        }
        if (heartBeatTimer != null) {
            heartBeatTimer.cancel();
            heartBeatTimer = null;
        }
    }

    public boolean isStart() {
        return isStart;
    }

    public DatagramSocketServer start() {
        if (!isStart) {
            isStart = true;
            DatagramSocket server = getRunningServerSocket();
            if (server != null) {
                getReceiveThread().start();
                getSendThread().start();
                startHeartBeatTask();
                mOnDatagramSocketServerListener.onServerStart(self);
            } else {
                isStart = false;
                mOnDatagramSocketServerListener.onServerClose(self);
            }

        } else {
            mOnDatagramSocketServerListener.onServerStart(self);
        }
        return this;
    }



    public DatagramPacket send(DatagramPacket packet) {
        if (!isStart()) {
            return null;
        }
        if (packet == null) {
            return null;
        }
        if (packet.getSocketAddress() == null) {
            return null;
        }
        __i__enqueueNewPacket(packet);
        return packet;
    }

    public void close() {
        if (!isClosing) {
            isClosing = true;
            getClosehread().start();
        }
    }

    private void __i__enqueueNewPacket(final DatagramPacket packet) {
        if (!isStart()) {
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
    public ConnectedByUDP get(String address){
        return connectedByUDPmap.get(address);
    }

    public ConnectedByUDP put(ConnectedByUDP connector){
        if(connector == null){
            return null;
        }
        SocketClientAddress address = connector.getAddress();
        if(address != null){
            String key = address.getStringRemoteAddress();
            if(key == null){
                return null;
            }
            if(connector.getRunningSocket() == null){
                connector.setRunningSocket(self);
            }
            synchronized (DatagramSocketServer.class){
                connectedByUDPmap.put(key, connector);
            }
            return connector;
        }
        return null;
    }

    public ConnectedByUDP remove(String address){
        synchronized (DatagramSocketServer.class){
            return connectedByUDPmap.remove(address);
        }
    }
    public ConnectedByUDP remove(ConnectedByUDP connector){
        if(connector == null){
            return null;
        }
        if(connector.getAddress() == null){
            return null;
        }
        synchronized (DatagramSocketServer.class){
            return connectedByUDPmap.remove(connector.getAddress().getStringRemoteAddress());
        }
    }

    public int getLocalPort() {
        return localPort;
    }

    protected List<OnDatagramSocketServerListener> getDatagramSocketServerListeners() {
        if (this.datagramSocketServerListeners == null) {
            this.datagramSocketServerListeners = new ArrayList<>();
        }
        return this.datagramSocketServerListeners;
    }

    public DatagramSocketServer registerServerListener(OnDatagramSocketServerListener listener) {
        if(!getDatagramSocketServerListeners().contains(listener)){
            synchronized (serverListenerLock){
                if(!getDatagramSocketServerListeners().contains(listener)){
                    getDatagramSocketServerListeners().add(listener);
                }
            }

        }
        return self;
    }

    public DatagramSocketServer unRegisterServerListener(OnDatagramSocketServerListener listener) {
        synchronized (serverListenerLock){
            getDatagramSocketServerListeners().remove(listener);
        }
        return self;
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

    /**
     * DatagramSocket 新建成功后就能接收数据了
     *
     * @return
     */
    protected DatagramSocket getRunningServerSocket() {
        if (this.runningServerSocket == null) {
            synchronized (DatagramSocketServer.class) {
                if (this.runningServerSocket == null) {
                    try {
                        if (getLocalPort() == -1) {
                            this.runningServerSocket = new DatagramSocket();
                            localPort = runningServerSocket.getLocalPort();
                        } else {
                            this.runningServerSocket = new DatagramSocket(getLocalPort());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                        runningServerSocket = null;
                    }
                }
            }
        }
        return this.runningServerSocket;
    }

    protected LinkedBlockingQueue<DatagramPacket> getSendingPacketQueue() {
        if (sendingPacketQueue == null) {
            sendingPacketQueue = new LinkedBlockingQueue<DatagramPacket>();
        }
        return sendingPacketQueue;
    }

    private OnDatagramSocketServerListener mOnDatagramSocketServerListener = new OnDatagramSocketServerListener() {
        @Override
        public void onServerStart(DatagramSocketServer server) {
            for (OnDatagramSocketServerListener listener : getDatagramSocketServerListeners()) {
                listener.onServerStart(server);
            }
        }

        @Override
        public void onServerClose(DatagramSocketServer server) {
            for (OnDatagramSocketServerListener listener : getDatagramSocketServerListeners()) {
                listener.onServerClose(server);
            }
        }

        @Override
        public void onServerReceivePacket(ConnectedByUDP connector, SocketPacket packet) {
            for (OnDatagramSocketServerListener listener : getDatagramSocketServerListeners()) {
                listener.onServerReceivePacket(connector, packet);
            }
        }

        @Override
        public void onClientConnected(ConnectedByUDP connector) {
            for (OnDatagramSocketServerListener listener : getDatagramSocketServerListeners()) {
                listener.onClientConnected(connector);
            }
        }

        @Override
        public void onClientDisconnected(ConnectedByUDP connector) {
            for (OnDatagramSocketServerListener listener : getDatagramSocketServerListeners()) {
                listener.onClientDisconnected(connector);
            }
        }
    };

    protected DatagramSocketServer setSendThread(DatagramSocketServer.SendThread sendThread) {
        this.sendThread = sendThread;
        return this;
    }

    protected DatagramSocketServer.SendThread getSendThread() {
        if (this.sendThread == null) {
            this.sendThread = new DatagramSocketServer.SendThread();
        }
        return this.sendThread;
    }


    protected DatagramSocketServer setReceiveThread(ReceiveThread receiveThread) {
        this.receiveThread = receiveThread;
        return this;
    }

    protected ReceiveThread getReceiveThread() {
        if (this.receiveThread == null) {
            this.receiveThread = new ReceiveThread();
        }
        return this.receiveThread;
    }

    protected DatagramSocketServer setCloseThread(CloseThread closeThread) {
        this.closeThread = closeThread;
        return this;
    }

    protected DatagramSocketServer.CloseThread getClosehread() {
        if (this.closeThread == null) {
            this.closeThread = new DatagramSocketServer.CloseThread();
        }
        return this.closeThread;
    }

    private class SendThread extends Thread {
        @Override
        public void run() {
            super.run();
            DatagramPacket packet;
            try {
                while (isStart
                        && self.runningServerSocket != null
                        && !self.runningServerSocket.isClosed()
                        && (packet = self.getSendingPacketQueue().take()) != null) {
                    try {
                        self.runningServerSocket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (isStart
                    && self.runningServerSocket != null
                    && !self.runningServerSocket.isClosed()) {
                DatagramPacket datagramPacket = new DatagramPacket(new byte[DATA_LEN], DATA_LEN);
                try {
                    self.runningServerSocket.receive(datagramPacket);
                    if (datagramPacket.getLength() >= 4) {
                        SocketPacket packet = new SocketPacket();
                        packet.unPacket(datagramPacket.getData());

                        SocketClientAddress address = new SocketClientAddress(datagramPacket.getAddress().getHostAddress(), datagramPacket.getPort());
                        ConnectedByUDP connector = self.connectedByUDPmap.get(address.getStringRemoteAddress());
                        if(connector == null && !packet.isDisconnected()){
                            connector = new ConnectedByUDP(address);
                            connector.setRunningSocket(self);
                            connector.setState(ConnectedByUDP.State.Connected);
                            self.put(connector);
                            mOnDatagramSocketServerListener.onClientConnected(connector);
                            connector.setLastReceiveMessageTime(System.currentTimeMillis());
                            mOnDatagramSocketServerListener.onServerReceivePacket(connector, packet);
                        }else{
                            connector.setLastReceiveMessageTime(System.currentTimeMillis());
                            if(connector.isDisconnecting() && packet.isDisconnected()){
                                mOnDatagramSocketServerListener.onClientDisconnected(connector);
                                self.remove(connector);
                            }else if(!connector.isDisconnecting() && packet.isDisconnected()){
                                connector.disconnect();
                                mOnDatagramSocketServerListener.onClientDisconnected(connector);
                                self.remove(connector);
                            }else if(!packet.isDisconnected()){
                                connector.setState(ConnectedByUDP.State.Connected);
                                mOnDatagramSocketServerListener.onServerReceivePacket(connector, packet);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    self.close();
                }
            }
        }
    }

    private class CloseThread extends Thread {
        @Override
        public void run() {
            super.run();
            stopHeartBeatTask();
            if (self.runningServerSocket != null && !self.runningServerSocket.isClosed()) {
                self.runningServerSocket.close();
                self.runningServerSocket = null;
            }
            if (self.sendThread != null) {
                self.sendThread.interrupt();
                self.setSendThread(null);
            }
            if (self.receiveThread != null) {
                self.receiveThread.interrupt();
                self.setReceiveThread(null);
            }
            isStart = false;
            DatagramPacket packet;
            while ((packet = self.getSendingPacketQueue().poll()) != null) {
//                mOnSendingListener.onSendPacketCancel(self, packet);
            }
            self.setCloseThread(null);
            isClosing = false;
            mOnDatagramSocketServerListener.onServerClose(self);
        }
    }
}
