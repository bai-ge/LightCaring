package com.carefor.service;

import android.os.CountDownTimer;
import android.util.Log;

import com.carefor.connect.BaseConnector;
import com.carefor.connect.ConnectedByTCP;
import com.carefor.connect.OnConnectedListener;
import com.carefor.connect.OnSocketReceivingListener;
import com.carefor.connect.SocketClientAddress;
import com.carefor.connect.SocketPacket;
import com.carefor.connect.msg.MessageManager;
import com.carefor.connect.msg.Parm;
import com.carefor.data.entity.DeviceModel;
import com.carefor.util.Loggerx;
import com.carefor.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by baige on 2018/3/27.
 */

public class ServerConnector {
    private final static String TAG = ServerConnector.class.getCanonicalName();

    private final ServerConnector self;

    private static ServerConnector INSTANCE = null;

    private DeviceModel mServerDevice;
    private final Object serverConnectLock = new Object();

    private DaemonServiceRepository mDaemonServiceRepository;

    private OnServerConnectorListener mListener;

    private Timer mTimer;

    private TimerTask mLoginTask;



    public OnServerConnectorListener getListener() {
        return mListener;
    }

    public void setListener(OnServerConnectorListener mListener) {
        this.mListener = mListener;
    }

    private ServerConnector(){
        self = this;
        mDaemonServiceRepository = DaemonServiceRepository.getInstance();
        mTimer = new Timer();
    }


    public static ServerConnector getInstance() {
        if (INSTANCE == null) {
            synchronized (ServerConnector.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServerConnector();
                }
            }
        }
        return INSTANCE;
    }

    public DeviceModel getServerDevice() {
        return mServerDevice;
    }

    public void setServerDevice(DeviceModel mServerDevice) {
        this.mServerDevice = mServerDevice;
    }

    public synchronized DeviceModel connectTOServer(String ip, int port){
        ConnectedByTCP connectedByTCP;
        Loggerx.d(TAG, "开始连接服务器"+ip+":"+port);
        synchronized (serverConnectLock) {
            if (mServerDevice == null) {//开始连接
                mServerDevice = new DeviceModel();
                connectedByTCP = new ConnectedByTCP(new SocketClientAddress(ip, port));
                connectedByTCP.registerConnectedListener(mOnConnectedListener);
                mServerDevice.setConnectedByTCP(connectedByTCP);
                connectedByTCP.connect();
            } else if (mServerDevice.getConnectedByTCP() == null) {
                connectedByTCP = new ConnectedByTCP(new SocketClientAddress(ip, port));
                mServerDevice.setConnectedByTCP(connectedByTCP);
                connectedByTCP.registerConnectedListener(mOnConnectedListener);
                connectedByTCP.connect();
            } else if (mServerDevice.getConnectedByTCP().getAddress() == null) {
                mServerDevice.getConnectedByTCP().setAddress(new SocketClientAddress(ip, port));
                mServerDevice.getConnectedByTCP().registerConnectedListener(mOnConnectedListener);
                mServerDevice.getConnectedByTCP().connect();
            } else if (!mServerDevice.getConnectedByTCP().isConnected()) {
                mServerDevice.getConnectedByTCP().getAddress().setRemoteIP(ip);
                mServerDevice.getConnectedByTCP().getAddress().setRemotePortWithInteger(port);
                mServerDevice.getConnectedByTCP().registerConnectedListener(mOnConnectedListener);
                mServerDevice.getConnectedByTCP().connect();
            } else {
                if (mServerDevice.getConnectedByTCP().getAddress().getRemotePort().equals(ip)
                        && mServerDevice.getConnectedByTCP().getAddress().getRemotePortIntegerValue() == port) {
                    return mServerDevice;
                } else {
                    mServerDevice.getConnectedByTCP().disconnect();
                    mServerDevice.getConnectedByTCP().getAddress().setRemoteIP(ip);
                    mServerDevice.getConnectedByTCP().getAddress().setRemotePortWithInteger(port);
                    mServerDevice.getConnectedByTCP().registerConnectedListener(mOnConnectedListener);
                    mServerDevice.getConnectedByTCP().connect();
                }
            }
            if(mListener != null && mServerDevice.getConnectedByTCP() != null && mServerDevice.getConnectedByTCP().isConnecting()){
                mListener.onConnecting(mServerDevice.getConnectedByTCP());
            }
        }
        return mServerDevice;
    }

    public synchronized DeviceModel connectTOServer(String ip, int port, OnConnectedListener listener){
        ConnectedByTCP connectedByTCP;
        Loggerx.d(TAG, "开始连接服务器"+ip+":"+port);
        synchronized (serverConnectLock) {
            if (mServerDevice == null) {//开始连接
                mServerDevice = new DeviceModel();
                connectedByTCP = new ConnectedByTCP(new SocketClientAddress(ip, port));
                connectedByTCP.registerConnectedListener(mOnConnectedListener);
                connectedByTCP.registerConnectedListener(listener);
                mServerDevice.setConnectedByTCP(connectedByTCP);
                connectedByTCP.connect();
            } else if (mServerDevice.getConnectedByTCP() == null) {
                connectedByTCP = new ConnectedByTCP(new SocketClientAddress(ip, port));
                mServerDevice.setConnectedByTCP(connectedByTCP);
                connectedByTCP.registerConnectedListener(mOnConnectedListener);
                connectedByTCP.registerConnectedListener(listener);
                connectedByTCP.registerConnectedListener(listener);
                connectedByTCP.connect();
            } else if (mServerDevice.getConnectedByTCP().getAddress() == null) {
                mServerDevice.getConnectedByTCP().setAddress(new SocketClientAddress(ip, port));
                mServerDevice.getConnectedByTCP().registerConnectedListener(mOnConnectedListener);
                mServerDevice.getConnectedByTCP().registerConnectedListener(listener);
                mServerDevice.getConnectedByTCP().connect();
            } else if (!mServerDevice.getConnectedByTCP().isConnected()) {
                mServerDevice.getConnectedByTCP().getAddress().setRemoteIP(ip);
                mServerDevice.getConnectedByTCP().getAddress().setRemotePortWithInteger(port);
                mServerDevice.getConnectedByTCP().registerConnectedListener(mOnConnectedListener);
                mServerDevice.getConnectedByTCP().registerConnectedListener(listener);
                mServerDevice.getConnectedByTCP().connect();
            } else {
                if (mServerDevice.getConnectedByTCP().getAddress().getRemotePort().equals(ip)
                        && mServerDevice.getConnectedByTCP().getAddress().getRemotePortIntegerValue() == port) {
                    return mServerDevice;
                } else {
                    mServerDevice.getConnectedByTCP().disconnect();
                    mServerDevice.getConnectedByTCP().getAddress().setRemoteIP(ip);
                    mServerDevice.getConnectedByTCP().getAddress().setRemotePortWithInteger(port);
                    mServerDevice.getConnectedByTCP().registerConnectedListener(mOnConnectedListener);
                    mServerDevice.getConnectedByTCP().registerConnectedListener(listener);
                    mServerDevice.getConnectedByTCP().connect();
                }
            }
            if(mListener != null && mServerDevice.getConnectedByTCP() != null && mServerDevice.getConnectedByTCP().isConnecting()){
                mListener.onConnecting(mServerDevice.getConnectedByTCP());
            }
        }
        return mServerDevice;
    }

    public boolean sendMessage(String msg){
        if(mServerDevice != null && mServerDevice.getConnectedByTCP() != null){
            if(!mServerDevice.getConnectedByTCP().isConnected()){
                mServerDevice.getConnectedByTCP().connect();
            }else{
                mServerDevice.getConnectedByTCP().sendString(msg);
                return true;
            }
        }
        return false;
    }

    //主动连接登录验证使用的监听器
    private OnConnectedListener mOnConnectedListener = new OnConnectedListener.SimpleOnConnectedListener() {

        @Override
        public void onConnected(BaseConnector connector) {
            super.onConnected(connector);
            if(mListener != null){
                mListener.onConnected(connector);
            }
            Log.v(TAG, "连接成功"+connector);

            if(mTimer == null){
                mTimer = new Timer();
            }
            if(mLoginTask == null){
                mLoginTask = new TimerTask() {
                    @Override
                    public void run() {
                        Log.v(TAG, "执行重连任务");
                        if(mServerDevice != null
                                && mServerDevice.getConnectedByTCP() != null
                                && mServerDevice.getConnectedByTCP().isConnected()
                                && mServerDevice.getLoginTime() <= 0){
                            String msg = MessageManager.login(mDaemonServiceRepository.getDeviceId(), mServerDevice.getLocalIp(), mServerDevice.getLocalPort()+"", "", "");
                            if(msg != null){
                                mServerDevice.getConnectedByTCP().sendString(msg);
                                Log.v(TAG, "发送数据："+msg);
                            }else{
                                throw new IllegalArgumentException("the login msg is null");
                            }
                        }
                    }
                };
            }
            mTimer.schedule(mLoginTask, 5000, 5000);

            ConnectedByTCP connectedByTCP = (ConnectedByTCP) connector;
            mServerDevice.setLocalIp(connectedByTCP.getRunningSocket().getLocalAddress().getHostAddress());
            mServerDevice.setLocalPort(connectedByTCP.getRunningSocket().getLocalPort());
            String msg = MessageManager.login(mDaemonServiceRepository.getDeviceId(), mServerDevice.getLocalIp(), mServerDevice.getLocalPort()+"", "", "");


            if(msg != null){
                connector.sendString(msg);
                Log.v(TAG, "发送数据："+msg);
            }else{
                throw new IllegalArgumentException("the login msg is null");
            }
        }

        @Override
        public void onResponse(BaseConnector connector, SocketPacket responsePacket) {
            // TODO Auto-generated method stub

            //if(验证成功) {获取uuid, 添加进设备中

            Log.v(TAG, "接收到数据"+connector);
            Loggerx.d(TAG,  "TCP接收到消息"+responsePacket.toString());
            //仅处理登录反馈
            if(responsePacket.getContentBuf() != null){
                String msg = Tools.dataToString(responsePacket.getContentBuf(), Tools.DEFAULT_ENCODE);
                if(!Tools.isEmpty(msg)){
                    try {
                        JSONObject json = new JSONObject(msg);
                        if (json.has(Parm.CODE) && json.has(Parm.DATA)) {
                            if(json.getInt(Parm.CODE) != Parm.CODE_SUCCESS){
                                return;
                            }
                            JSONObject dataJson = json.getJSONObject(Parm.DATA);
                            if(dataJson.has(Parm.DATA_TYPE)){
                                int type = dataJson.getInt(Parm.DATA_TYPE);
                                switch (type){
                                    case Parm.TYPE_LOGIN:
                                        if(!dataJson.has(Parm.DEVICE_ID) || !dataJson.getString(Parm.DEVICE_ID).equals(mDaemonServiceRepository.getDeviceId())){
                                            return;
                                        }
                                        if(dataJson.has(Parm.FROM)){
                                            mServerDevice.setDeviceId(dataJson.getString(Parm.FROM));
                                        }
                                        if(dataJson.has(Parm.REMOTE_IP)){
                                            mServerDevice.setRemoteIp(dataJson.getString(Parm.REMOTE_IP));
                                        }
                                        if(dataJson.has(Parm.REMOTE_PORT)){
                                            mServerDevice.setRemotePort(dataJson.getInt(Parm.REMOTE_PORT));
                                        }
                                        mServerDevice.setLoginTime(System.currentTimeMillis());
                                        if(mListener != null){
                                            mListener.onLogin(mServerDevice);
                                        }
                                        if(mTimer != null){
                                            mTimer.cancel();
                                            mTimer = null;
                                        }
                                        if(mLoginTask != null){
                                            mLoginTask.cancel();
                                            mLoginTask = null;
                                        }
                                    break;
                                    default:
                                        break;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        @Override
        public void onDisconnected(final BaseConnector connector) {
            super.onDisconnected(connector);
            if(mListener != null){
                mListener.onDisConnected(connector);
            }
            if(mTimer != null){
                mTimer.cancel();
                mTimer = null;
            }
            if(mLoginTask != null){
                mLoginTask.cancel();
                mLoginTask = null;
            }
            //TODO 启动定时连接
            Log.v(TAG, "连接关闭"+connector);
            DaemonServiceRepository daemonServiceRepository = DaemonServiceRepository.getInstance();
            if(connector.getTryConnectCount() <= 20 && (daemonServiceRepository.isNetworkValid() || daemonServiceRepository.isWifiValid())){
                Log.d(TAG, "断开连接，准备重连");
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            sleep(10 * 1000);
                            Log.d(TAG, "断开连接，开始重连");
                            connector.connect();
                            if(mListener != null && connector.isConnecting()){
                                mListener.onConnecting(connector);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }
    };

    /**
     * @return
     * 正在连接
     * 连接成功
     * 断开连接
     */
    public int getConnectState() {
        if(mServerDevice == null || mServerDevice.getConnectedByTCP() == null){
            return Parm.DISCONNECTED;
        }else{
            if(mServerDevice.getConnectedByTCP().isConnecting()){
                return Parm.CONNECTING;
            } else if(mServerDevice.getLoginTime() > 0){
                return Parm.LOGIN;
            } else if(mServerDevice.getConnectedByTCP().isConnected()){
                return Parm.CONNECTED;
            }
        }
        return Parm.DISCONNECTED;
    }

    interface OnServerConnectorListener{
        void onConnecting(BaseConnector connector);
        void onConnected(BaseConnector connector);
        void onLogin(DeviceModel device);
        void onDisConnected(BaseConnector connector);
    }
}
