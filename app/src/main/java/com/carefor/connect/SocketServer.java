package com.carefor.connect;

import android.support.annotation.NonNull;


import com.carefor.util.StringValidation;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by baige on 2018/3/23.
 */

public class SocketServer extends OnConnectedListener.SimpleOnConnectedListener{
    final SocketServer self = this;

    public static final int DEFAULT_PORT = 12056;
    public static final int NoPort = -1;
    public static final int MaxPort = 65535;

    private int port = NoPort;

    private boolean listening;

    private ServerSocket runningServerSocket;

    private ArrayList<ConnectedByTCP> runningSocketServerClients;

    private List<OnSocketServerListener> onSocketServerListenerList;
    private final Object serverListenerLock = new Object();

    private ListenThread listenThread;

    /* Constructors */
    public SocketServer() {
        port = DEFAULT_PORT;
    }
    public SocketServer(int port) {
        this();
        setPort(port);
    }

    public boolean beginListen(){
        if (isListening()) {
            return false;
        }

        if (getRunningServerSocket() == null) {
            return false;
        }
        setListening(true);
        __i__onSocketServerBeginListen();
        return true;
    }
    public boolean beginListen(int port) {
        if (isListening()) {
            return false;
        }

        setPort(port);

        if (getRunningServerSocket() == null) {
            return false;
        }

        setListening(true);
        __i__onSocketServerBeginListen();
        return true;
    }

    public int beginListenFromPort(int port) {
        if (isListening()) {
            return NoPort;
        }

        while (port <= MaxPort) {
            if (beginListen(port)) {
                return port;
            }
            port++;
        }

        return NoPort;
    }
    private void __i__onSocketServerBeginListen() {
        mOnSocketServerListener.onServerBeginListen(self, getPort());
        getListenThread().start();
    }


    protected SocketServer setListening(boolean listening) {
        this.listening = listening;
        return this;
    }
    public boolean isListening() {
        return this.listening;
    }

    protected SocketServer setRunningServerSocket(ServerSocket runningServerSocket) {
        this.runningServerSocket = runningServerSocket;
        return this;
    }
    protected ServerSocket getRunningServerSocket() {
        if (this.runningServerSocket == null) {
            try {
                this.runningServerSocket = new ServerSocket(getPort());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.runningServerSocket;
    }

    public int getPort() {
        return this.port;
    }
    protected SocketServer setPort(int port) {
        if (!StringValidation.validateRegex("" + port, StringValidation.RegexPort)) {
            throw new IllegalArgumentException("we need a correct remote port to listen");
        }

        if (isListening()) {
            return this;
        }

        this.port = port;
        return this;
    }

    protected List<OnSocketServerListener> getOnSocketServerListenerList() {
        if(this.onSocketServerListenerList == null){
            synchronized (serverListenerLock){
                if (this.onSocketServerListenerList == null) {
                    this.onSocketServerListenerList = new ArrayList<OnSocketServerListener>();
                }
            }
        }
        return this.onSocketServerListenerList;
    }

    protected SocketServer setListenThread(ListenThread listenThread) {
        this.listenThread = listenThread;
        return this;
    }
    protected ListenThread getListenThread() {
        if (this.listenThread == null) {
            this.listenThread = new ListenThread();
        }
        return this.listenThread;
    }
    private boolean __i__checkServerSocketAvailable() {
        return getRunningServerSocket() != null && !getRunningServerSocket().isClosed();
    }

    protected ArrayList<ConnectedByTCP> getRunningSocketServerClients() {
        if (this.runningSocketServerClients == null) {
            this.runningSocketServerClients = new ArrayList<ConnectedByTCP>();
        }
        return this.runningSocketServerClients;
    }

    /**
     * 注册监听回调
     * @param listener 回调接收者
     */
    public SocketServer registerSocketServerListener(OnSocketServerListener listener) {
        if (!getOnSocketServerListenerList().contains(listener)) {
            synchronized (serverListenerLock){
                if (!getOnSocketServerListenerList().contains(listener)) {
                    getOnSocketServerListenerList().add(listener);
                }
            }
        }
        return this;
    }

    /**
     * 取消注册监听回调
     * @param listener 回调接收者
     */
    public SocketServer removeSocketServerListener(OnSocketServerListener listener) {
        synchronized (serverListenerLock){
            getOnSocketServerListenerList().remove(listener);
        }
        return this;
    }

    @Override
    public void onConnected(BaseConnector connector) {
        /*监听器在接收到的时候已经通知了，所有这里不需要操作*/
    }

    @Override
    public void onDisconnected(BaseConnector connector) {
        if(connector instanceof ConnectedByTCP){
            getRunningSocketServerClients().remove(connector);
            mOnSocketServerListener.onClientDisconnected(self, (ConnectedByTCP) connector);
        }
    }

    @Override
    public void onResponse(BaseConnector connector, @NonNull SocketPacket responsePacket) {
        /*这里可以单独处理某些命令，比如强制断开连接*/
    }

    private OnSocketServerListener mOnSocketServerListener = new OnSocketServerListener() {
        @Override
        public void onServerBeginListen(SocketServer socketServer, int port) {
            for (OnSocketServerListener listener : getOnSocketServerListenerList()){
                listener.onServerBeginListen(socketServer, port);
            }
        }

        @Override
        public void onServerStopListen(SocketServer socketServer, int port) {
            for (OnSocketServerListener listener : getOnSocketServerListenerList()){
                listener.onServerStopListen(socketServer, port);
            }
        }

        @Override
        public void onClientConnected(SocketServer socketServer, ConnectedByTCP socketServerClient) {
            for (OnSocketServerListener listener : getOnSocketServerListenerList()){
                listener.onClientConnected(socketServer, socketServerClient);
            }
        }

        @Override
        public void onClientDisconnected(SocketServer socketServer, ConnectedByTCP socketServerClient) {
            for (OnSocketServerListener listener : getOnSocketServerListenerList()){
                listener.onClientDisconnected(socketServer, socketServerClient);
            }
        }
    };
    private void __i__disconnectAllClients() {
        while (getRunningSocketServerClients().size() > 0) {
            ConnectedByTCP client = getRunningSocketServerClients().get(0);
            getRunningSocketServerClients().remove(client);
            client.disconnect();
        }
    }
    private class ListenThread extends Thread {
        private boolean running;
        protected ListenThread setRunning(boolean running) {
            this.running = running;
            return this;
        }
        protected boolean isRunning() {
            return this.running;
        }

        @Override
        public void run() {
            super.run();
            setRunning(true);
            while (!Thread.interrupted()
                    && self.__i__checkServerSocketAvailable()) {
                Socket socket = null;
                try {
                    socket = self.getRunningServerSocket().accept();


                    ConnectedByTCP socketServerClient = new ConnectedByTCP(socket);
                    getRunningSocketServerClients().add(socketServerClient);
                    socketServerClient.registerConnectedListener(self);
                    mOnSocketServerListener.onClientConnected(self, socketServerClient);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            setRunning(false);

            self.setListening(false);
            self.setListenThread(null);
            self.setRunningServerSocket(null);

            self. __i__disconnectAllClients();
            mOnSocketServerListener.onServerStopListen(self, getPort());
        }
    }
}
