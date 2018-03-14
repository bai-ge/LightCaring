package com.carefor.connect;

import android.util.Log;

import com.carefor.data.entity.DeviceModel;
import com.carefor.data.entity.MessageHeader;
import com.carefor.data.entity.Transinformation;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.telephone.TelePhone;
import com.carefor.telephone.TelePhoneAPI;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

/*
* 保持与服务器连接，当收到呼叫信息时主动通知TelePhone
* */

/**
 * Created by baige on 2017/12/21.
 */

public class Connector {

    private final static String TAG = Connector.class.getCanonicalName();

    private static Connector INSTANCE = null;

    private ConnectedByTCP connectedByTCP = null;
    private long connectedRefreshTime;

    private ConnectedByUDP connectedByUDP = null;

    private static ExecutorService fixedThreadPool = null;

    private static ExecutorService connectThreadPool = null;

    private Map<String, OnConnectorListener> mListenerMap = null;


    private long mP2pConnetTime = 0;

    public final static String LISTENER_SERVICE = "service";
    public final static String LISTENER_PRESENTER = "presenter";

    private int mConnectRunning = 0;

    //网络相关
    private boolean wifiEnable; //是否打开wifi
    private boolean wifiValid; //WiFi网络是否可用

    private boolean networkValid; //手机网络是否可用

    //网络相关
    public boolean isWifiEnable() {
        return wifiEnable;
    }

    public void setWifiEnable(boolean wifiEnable) {
        this.wifiEnable = wifiEnable;
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


    private ConnectServiceThread connectServiceThread;
    private long tryConnectServiceTime;

    class ConnectServiceThread extends Thread{
        @Override
        public void run() {
            super.run();
            CacheRepository cacheRepository = CacheRepository.getInstance();
            ConnectedByTCP conTCP = null;
            SocketAddress address = new InetSocketAddress(cacheRepository.getServerIp(), cacheRepository.getServerPort());
            Socket socket = new Socket();
            try {
                Log.v(TAG, "服务器地址：" + address.toString());
                socket.setSoTimeout(5000);//通过此方法设置超时才生效
                socket.connect(address, 5000);
                Log.d(TAG, "连接服务器成功");
                cacheRepository.setLocalIp(socket.getLocalAddress().getHostAddress());
                cacheRepository.setLocalPort(socket.getLocalPort());
                conTCP = new ConnectedByTCP(cacheRepository.getDeviceId(), socket, mOnTCPConnectListener);
                //登录
                String msg = MessageManager.login();
                conTCP.send(msg);
                synchronized (Connector.class){
                    connectedRefreshTime = System.currentTimeMillis();
                    Connector.this.connectedByTCP = conTCP;
                }

            }catch (IOException e) {
                mOnTCPConnectListener.connectedFail();
                mOnTCPConnectListener.exceptionCaught(conTCP, e);
                Log.d(TAG, "连接失败："+e.getMessage());
            }catch (Exception e){
                Log.d(TAG, "连接异常:"+e.getMessage());
            }
        }
    }

    //不建议使用
    Runnable mConnectRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "测试连接服务器");
            CacheRepository cacheRepository = CacheRepository.getInstance();
            if (mConnectRunning == 0) {
                synchronized (Connector.class) {
                    if (mConnectRunning == 0) {
                        mConnectRunning ++;
                        //检查是否已经连接指定主机
                        if (cacheRepository.getServerIp() == null) {
                            Log.d(TAG, "测试连接服务器 ip =" + null);
                            mConnectRunning --;
                            return;
                        }
                        if (connectedByTCP != null && connectedByTCP.isWork()) {
                            if (connectedByTCP.getSocket().getInetAddress().getHostAddress().equals(cacheRepository.getServerIp())
                                    && connectedByTCP.getSocket().getPort() == cacheRepository.getServerPort()) {
                                Log.d(TAG, "ip=" + connectedByTCP.getSocket().getInetAddress().getHostAddress());
                                Log.d(TAG, "serverIP=" + cacheRepository.getServerIp());
                                mListener.connectSuccess();
//                        if (mListener != null) {
//                            mListener.connectSuccess();
//                        }
                                return;
                            }
                        }
                        if (connectedByTCP != null) {
                            connectedByTCP.close();
                            connectedByTCP = null;
                        }

                        SocketAddress address = new InetSocketAddress(cacheRepository.getServerIp(), cacheRepository.getServerPort());
                        Socket socket = new Socket();
                        try {
                            Log.v(TAG, "服务器地址：" + address.toString());
                            socket.setSoTimeout(5000);//通过此方法设置超时才生效
                            socket.connect(address, 5000);
                            Log.d(TAG, "连接服务器成功");
                            cacheRepository.setLocalIp(socket.getLocalAddress().getHostAddress());
                            cacheRepository.setLocalPort(socket.getLocalPort());
                            connectedByTCP = new ConnectedByTCP(cacheRepository.getDeviceId(), socket, mOnTCPConnectListener);
                            //登录
                            String msg = MessageManager.login();
                            connectedByTCP.send(msg);

                        }catch (IOException e) {
                            mOnTCPConnectListener.connectedFail();
                            mOnTCPConnectListener.exceptionCaught(connectedByTCP, e);
                            Log.d(TAG, "连接失败："+e.getMessage());
                        }
                        mConnectRunning--;
                    }
                }
            }

        }
    };

    private Connector() {
        fixedThreadPool = Executors.newFixedThreadPool(5);//创建最多能并发运行5个线程的线程池
        connectThreadPool = Executors.newFixedThreadPool(3);
        mListenerMap = Collections.synchronizedMap(new LinkedHashMap<String, OnConnectorListener>());
        connectedByUDP = new ConnectedByUDP();
        connectedByUDP.setOnUDPConnectListener(mOnUDPConnectListener);
        connectedByUDP.start();
        CacheRepository cacheRepository = CacheRepository.getInstance();
        cacheRepository.setLocalUdpPort(ConnectedByUDP.localPort);
    }

    public static Connector getInstance() {
        if (INSTANCE == null) {
            synchronized (Connector.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new Connector();
                }
            }
        }
        return INSTANCE;
    }

    //注册监听器
    public void RegistConnectorListener(String id, OnConnectorListener listener) {
        checkNotNull(id);
        checkNotNull(listener);
        synchronized (mListenerMap.getClass()) {
            mListenerMap.put(id, listener);
        }
    }

    //注销监听器
    public void unRegistConnectorListener(String id) {
        checkNotNull(id);
        synchronized (mListenerMap.getClass()) {
            mListenerMap.remove(id);
        }
    }

    //发送心跳包
    public void sendHeartBeat() {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    sendMessage("\r\n");
                }
            });
        }
    }

    public void afxConnectServer() {
        if(wifiValid || networkValid){
            long refreshTime = connectedRefreshTime;
            CacheRepository cacheRepository = CacheRepository.getInstance();
            //检查是否已经连接指定主机
            if (cacheRepository.getServerIp() == null) {
                Log.d(TAG, "测试连接服务器 ip =" + null);
                return;
            }

            //检查是否更改了服务器IP或端口
            if (connectedByTCP != null && connectedByTCP.isWork()) {
                if (connectedByTCP.getSocket().getInetAddress().getHostAddress().equals(cacheRepository.getServerIp())
                        && connectedByTCP.getSocket().getPort() == cacheRepository.getServerPort()) {
                    Log.d(TAG, "ip=" + connectedByTCP.getSocket().getInetAddress().getHostAddress());
                    Log.d(TAG, "serverIP=" + cacheRepository.getServerIp());
                    mListener.connectSuccess();
//                        if (mListener != null) {
//                            mListener.connectSuccess();
//                        }
                    return;
                }
            }
            //保证connectedByTCP 没有被重新更新
            synchronized (Connector.class){
                if (connectedByTCP != null && refreshTime == connectedRefreshTime) {
                    connectedByTCP.close();
                    connectedByTCP = null;
                }
            }
            if(connectedByTCP == null){
                if(connectServiceThread != null
                        && !connectServiceThread.isInterrupted()
                        && System.currentTimeMillis() - tryConnectServiceTime >= 8000){
                    connectServiceThread.interrupt();
                }
                connectServiceThread = new ConnectServiceThread();
                connectServiceThread.start();
                tryConnectServiceTime = System.currentTimeMillis();
            }
        }else{
            Log.d(TAG, "没有网络");
        }
    }

    public void afxSendMessage(final String msg) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    sendMessage(msg);
                }
            });
        }
    }

    public void sendMessage(String msg) {
        if (connectedByTCP == null || !connectedByTCP.isWork()) {
            mConnectRunnable.run();
        }
        if (connectedByTCP != null) {
            connectedByTCP.send(msg);
        }
    }

    public void sendMessage(String ip, int port, byte[] buf) {
        if (connectedByUDP == null || !connectedByUDP.isWork()) {
            connectedByUDP = new ConnectedByUDP();
            connectedByUDP.setOnUDPConnectListener(mOnUDPConnectListener);
            connectedByUDP.start();
            CacheRepository cacheRepository = CacheRepository.getInstance();
            cacheRepository.setLocalUdpPort(ConnectedByUDP.localPort);
        }
        if (connectedByUDP != null) {
            try {
                InetAddress address = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
                if (connectedByUDP.send(packet)) {
                    Log.d(TAG, "成功发送数据报" + packet.getSocketAddress().toString());
                } else {
                    Log.d(TAG, "数据报发送失败" + packet.getSocketAddress().toString());
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, e.getMessage() + ip + ":" + port);
                e.printStackTrace();
            }
        }
    }

    public void afxSendMessage(final String ip, final int port, final byte[] buf) {
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    sendMessage(ip, port, buf);
                }
            });
        }
    }


    private ConnectedByTCP.OnTCPConnectListener mOnTCPConnectListener = new ConnectedByTCP.OnTCPConnectListener() {
        @Override
        public void connectedSuccess(ConnectedByTCP connectedByTCP) {
            Log.d(TAG, "连接服务器成功");


        }

        @Override
        public void connectedFail() {
            Log.d(TAG, "连接服务器失败");
        }

        @Override
        public void receiveMessage(String uuid, String msg) {
            Transinformation tranfor = MessageParser.parser(msg);
            Log.d(TAG, "收到信息：" + msg.length());
            Log.d(TAG, "信息类型：" + tranfor.getMessageHeader().getMethod());
            Log.v(TAG, "信息内容：" + msg);
            if (tranfor == null) {
                return;
            }
            MessageHeader header = tranfor.getMessageHeader();
            //登录信息
            if ((header.getMethod() & MessageHeader.Method.LOGIN) > 0) {
                loginMessage(tranfor);
            }
            //登出信息
            if ((header.getMethod() & MessageHeader.Method.LOGOUT) > 0) {
                logoutMessage(tranfor);
            }
            //处理数据同步
            if ((header.getMethod() & MessageHeader.Method.POST) > 0) {
                executePost(tranfor);
            }
            if ((header.getMethod() & MessageHeader.Method.CALL_TO) > 0) {
                executeCalled(tranfor);
            }
            if ((header.getMethod() & MessageHeader.Method.REPLY_CALL_TO) > 0) {
                executeReplayCall(tranfor);
            }
            if ((header.getMethod() & MessageHeader.Method.REPLY_CHECK) > 0) {
                executeCheck(tranfor);
            }
            if ((header.getMethod() & MessageHeader.Method.PICK_UP) > 0) {
                executePickUp(tranfor);
            }
            if ((header.getMethod() & MessageHeader.Method.HANG_UP) > 0) {
                executeHangUp(tranfor);
            }
        }

        @Override
        public void disconnected(String uuid) {
            mListener.disconnected(uuid);
        }

        @Override
        public void reconnected(String uuid) {

        }

        @Override
        public void exceptionCaught(ConnectedByTCP connectedByTCP, Throwable cause) {

        }
    };

    private ConnectedByUDP.OnUDPConnectListener mOnUDPConnectListener = new ConnectedByUDP.OnUDPConnectListener() {
        @Override
        public void connectedSuccess(String userId, String ip, int port) {

        }

        @Override
        public void receiveMessage(DatagramPacket packet) {
            Map<String, Object> attr = MessageManager.messageParser(packet);
            if (attr != null) {
                int type = (int) attr.get("type");
                switch (type) {
                    case MessageManager.TYPE_VOICE:
                        if (attr.containsKey("delay")) {
                            Log.d(TAG, "数据延时：" + String.valueOf((long) attr.get("delay")));
                            TelePhone.getInstance().setDelayTime((long) attr.get("delay"));
                        }
                        OnReceiveVoice((byte[]) attr.get(MessageManager.getTagName(MessageManager.TAG_VOICE)));
                        break;
                    case MessageManager.TYPE_P2P:
                        String device_id = new String((byte[]) attr.get(MessageManager.getTagName(MessageManager.TAG_DEVICE_ID)));
                        String ip = packet.getAddress().getHostAddress();
                        if (!device_id.isEmpty()) {
                            CacheRepository cacheRepository = CacheRepository.getInstance();
                            if (cacheRepository.getTalkWith() != null &&
                                    cacheRepository.getTalkWith().equals(device_id)) {//确定是和自己连接的udp
                                if (CacheRepository.getInstance().isP2PConnectSuccess()) {
                                    //一次建立过程中可能会多次收到udp数据包，来自不同的网段，10秒内认为是同一个连接,超过10秒认为是新的连接
                                    if (System.currentTimeMillis() - mP2pConnetTime > 10000) {
                                        mP2pConnetTime = System.currentTimeMillis();
                                        cacheRepository.setP2PIp(ip);
                                        cacheRepository.setP2PPort(packet.getPort());

                                        if (!cacheRepository.isP2PConnectSuccess()) {
                                            sendMessage(ip, packet.getPort(), MessageManager.udpP2P(cacheRepository.getDeviceId()));
                                        }
                                        cacheRepository.setP2PConnectSuccess(true);
                                        mListener.receviceP2P(device_id, ip, packet.getPort());
                                    }
                                } else {
                                    mP2pConnetTime = System.currentTimeMillis();
                                    cacheRepository.setP2PIp(ip);
                                    cacheRepository.setP2PPort(packet.getPort());

                                    if (!cacheRepository.isP2PConnectSuccess()) {
                                        sendMessage(ip, packet.getPort(), MessageManager.udpP2P(cacheRepository.getDeviceId()));
                                    }
                                    cacheRepository.setP2PConnectSuccess(true);
                                    mListener.receviceP2P(device_id, ip, packet.getPort());
                                }
                            }
                        }
                        break;
                    case MessageManager.TYPE_TRANF:
                        if (attr.containsKey(MessageManager.getTagName(MessageManager.TAG_VOICE))) {
                            OnReceiveVoice((byte[]) attr.get(MessageManager.getTagName(MessageManager.TAG_VOICE)));
                        }
                        break;
                    default:
                        Log.d(TAG, "发现未处理数据");

                }
            }
        }

        @Override
        public void doNotWork() {

        }
    };

    private void loginMessage(Transinformation tranfor) {
        LinkedHashMap<String, Object> dateMap = tranfor.getDateMap();
        String id = tranfor.getMessageHeader().getFrom();
        CacheRepository cacheRepository = CacheRepository.getInstance();
        if (id != null && !id.isEmpty() && id.equals(cacheRepository.getDeviceId())) {
            if (dateMap.containsKey(MessageHeader.Param.REMOTE_IP)) {
                cacheRepository.setRemoteIp((String) dateMap.get(MessageHeader.Param.REMOTE_IP));
            }
            if (dateMap.containsKey(MessageHeader.Param.REMOTE_PORT)) {
                cacheRepository.setRemoteUdpPort((Integer) dateMap.get(MessageHeader.Param.REMOTE_PORT));
            }
            mListener.loginSuccess();
        }
    }

    private void logoutMessage(Transinformation tranfor) {
        LinkedHashMap<String, Object> dateMap = tranfor.getDateMap();
        String id = tranfor.getMessageHeader().getFrom();
        //TODO
    }

    //处理接收到的提交数据
    private void executePost(Transinformation tranfor) {
        //获取用户列表


        //得到服务器列表
    }

    //处理呼叫信息
    private void executeCalled(Transinformation tranfor) {
        Map<String, String> dataMap = tranfor.getMessageHeader().getParamMap();
        String id = tranfor.getMessageHeader().getFrom();
        if (tranfor.getMessageHeader().getDeslist().contains(CacheRepository.getInstance().getDeviceId())) {
            //TODO 处理其他事
            DeviceModel deviceModel = new DeviceModel();
            CacheRepository.getInstance().setTalkWith(id);
            deviceModel.setDeviceidId(id);
            if (dataMap.containsKey(MessageHeader.Param.REMOTE_IP)) {
                deviceModel.setRemoteIp(dataMap.get(MessageHeader.Param.REMOTE_IP));
            }
            if (dataMap.containsKey(MessageHeader.Param.REMOTE_UDP_PORT)) {
                deviceModel.setRemoteUdpPort(Integer.valueOf(dataMap.get(MessageHeader.Param.REMOTE_UDP_PORT)));
            }
            if (dataMap.containsKey(MessageHeader.Param.LOCAL_IP)) {
                deviceModel.setLocalIp(dataMap.get(MessageHeader.Param.LOCAL_IP));
            }
            if (dataMap.containsKey(MessageHeader.Param.LOCAL_UDP_PORT)) {
                deviceModel.setLocalUdpPort(Integer.valueOf(dataMap.get(MessageHeader.Param.LOCAL_UDP_PORT)));
            }
            CacheRepository.getInstance().setTalkWithDevice(deviceModel);
            TelePhone.getInstance().beCall(id, new TelePhoneAPI.BaseCallBackAdapter() {
                @Override
                public void isBusy() {
                    super.isBusy();
                    Log.d(TAG, "正在通话中……错误");
                }
            });
        }
    }

    //回复呼叫信息
    private void executeReplayCall(Transinformation tranfor) {
        Map<String, String> dataMap = tranfor.getMessageHeader().getParamMap();
        String id = tranfor.getMessageHeader().getFrom();
        if (tranfor.getMessageHeader().getDeslist().contains(CacheRepository.getInstance().getDeviceId())) {
            //TODO 处理其他事
            DeviceModel deviceModel = new DeviceModel();
            if (CacheRepository.getInstance().getTalkWith() != null && CacheRepository.getInstance().getTalkWith().equals(id)) {
                deviceModel.setDeviceidId(id);
                if (dataMap.containsKey(MessageHeader.Param.REMOTE_IP)) {
                    deviceModel.setRemoteIp(dataMap.get(MessageHeader.Param.REMOTE_IP));
                }
                if (dataMap.containsKey(MessageHeader.Param.REMOTE_UDP_PORT)) {
                    deviceModel.setRemoteUdpPort(Integer.valueOf(dataMap.get(MessageHeader.Param.REMOTE_UDP_PORT)));
                }
                if (dataMap.containsKey(MessageHeader.Param.LOCAL_IP)) {
                    deviceModel.setLocalIp(dataMap.get(MessageHeader.Param.LOCAL_IP));
                }
                if (dataMap.containsKey(MessageHeader.Param.LOCAL_UDP_PORT)) {
                    deviceModel.setLocalUdpPort(Integer.valueOf(dataMap.get(MessageHeader.Param.LOCAL_UDP_PORT)));
                }
                CacheRepository.getInstance().setTalkWithDevice(deviceModel);
                //TODO P2P 连接开始
                tryP2PConnect(CacheRepository.getInstance().getTalkWithDevice());
            }

//            if (mListener != null) {
//                mListener.handUp(tranfor);
//            }
        }
    }

    //收到该消息说明UDP能连接到服务器
    private void executeCheck(Transinformation tranfor) {
        Map<String, Object> dataMap = tranfor.getDateMap();
        String id = tranfor.getMessageHeader().getFrom();
        if (id != null && id.equals(CacheRepository.getInstance().getDeviceId())) {
            //TODO 处理其他事

            if (dataMap.containsKey(MessageHeader.Param.REMOTE_IP)) {
                CacheRepository.getInstance().setRemoteIp((String) dataMap.get(MessageHeader.Param.REMOTE_IP));
                Log.d(TAG, "获取远程IP" + CacheRepository.getInstance().getRemoteIp());
            }
            if (dataMap.containsKey(MessageHeader.Param.REMOTE_UDP_PORT)) {
                CacheRepository.getInstance().setRemoteUdpPort((Integer) dataMap.get(MessageHeader.Param.REMOTE_UDP_PORT));
                Log.d(TAG, "获取远程UDP端口：" + CacheRepository.getInstance().getRemoteUdpPort());
            }
            if (dataMap.containsKey(MessageHeader.Param.LOCAL_IP)) {
                CacheRepository.getInstance().setRemoteIp((String) dataMap.get(MessageHeader.Param.LOCAL_IP));
            }
            if (dataMap.containsKey(MessageHeader.Param.LOCAL_UDP_PORT)) {
                CacheRepository.getInstance().setRemoteUdpPort((Integer) dataMap.get(MessageHeader.Param.LOCAL_UDP_PORT));
            }
            //Tip 唤醒界面
            if (TelePhone.getInstance().getStatus() == TelePhone.Status.CALLED) {
                mListener.beCalled(CacheRepository.getInstance().getTalkWithDevice());
            }
            TelePhone.getInstance().checkUdpSuccess();
        }
    }

    private void executeHangUp(Transinformation tranfor) {
        String id = tranfor.getMessageHeader().getFrom();
        if (tranfor.getMessageHeader().getDeslist().contains(CacheRepository.getInstance().getDeviceId())) {
            //TODO 处理其他事
            mListener.handUp(tranfor);
        }
    }

    private void executePickUp(Transinformation tranfor) {
        String id = tranfor.getMessageHeader().getFrom();
        if (tranfor.getMessageHeader().getDeslist().contains(CacheRepository.getInstance().getDeviceId())) {
            //TODO 处理其他事
            mListener.pickUp(tranfor);
        }
    }

    public void tryP2PConnect(DeviceModel deviceModel) {
        Log.d(TAG, "尝试与远程设备建立P2P连接");
        Log.d(TAG, "远程设备");
        Log.d(TAG, "设备ID" + deviceModel.getDeviceidId());
        Log.d(TAG, "远程地址：" + deviceModel.getRemoteIp() + ":" + deviceModel.getRemoteUdpPort());
        Log.d(TAG, "本地地址：" + deviceModel.getLocalIp() + ":" + deviceModel.getLocalUdpPort());

        Log.d(TAG, "本地设备");
        Log.d(TAG, "设备ID" + CacheRepository.getInstance().getDeviceId());
        Log.d(TAG, "远程地址：" + CacheRepository.getInstance().getRemoteIp() + ":" + CacheRepository.getInstance().getRemoteUdpPort());
        Log.d(TAG, "本地地址：" + CacheRepository.getInstance().getLocalIp() + ":" + CacheRepository.getInstance().getLocalUdpPort());
        byte[] buf = MessageManager.udpP2P(CacheRepository.getInstance().getDeviceId());
        afxSendMessage(deviceModel.getLocalIp(), deviceModel.getLocalUdpPort(), buf);
        afxSendMessage(deviceModel.getRemoteIp(), deviceModel.getLocalUdpPort(), buf);
    }

    public boolean isConnectServer() {
        if (connectedByTCP != null && connectedByTCP.isWork()) {
            Log.d(TAG, "已经连接服务器");
            return true;
        }
        return false;
    }

    public interface OnConnectorListener {
        void connectSuccess();

        void disconnected(String mid);

        void loginSuccess();

        void receviceP2P(String device_id, String ip, int port);

        void beCalled(DeviceModel deviceModel);

        void pickUp(Transinformation tranfor);

        void handUp(Transinformation tranfor);
    }

    static public class OnConnectorListenerAdapter implements OnConnectorListener {
        @Override
        public void connectSuccess() {

        }

        @Override
        public void disconnected(String mid) {

        }

        @Override
        public void loginSuccess() {

        }

        @Override
        public void receviceP2P(String device_id, String ip, int port) {

        }

        @Override
        public void beCalled(DeviceModel deviceModel) {

        }

        @Override
        public void pickUp(Transinformation tranfor) {

        }

        @Override
        public void handUp(Transinformation tranfor) {

        }
    }

    private OnConnectorListener mListener = new OnConnectorListener() {
        @Override
        public void connectSuccess() {

        }

        @Override
        public void disconnected(String mid) {
            Iterator<Map.Entry<String, OnConnectorListener>> iterator = mListenerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, OnConnectorListener> entity = iterator.next();
                OnConnectorListener value = entity.getValue();
                if (value != null) {
                    value.disconnected(mid);
                }
            }
        }

        @Override
        public void loginSuccess() {
            Iterator<Map.Entry<String, OnConnectorListener>> iterator = mListenerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, OnConnectorListener> entity = iterator.next();
                OnConnectorListener value = entity.getValue();
                if (value != null) {
                    value.loginSuccess();
                }
            }
        }

        @Override
        public void receviceP2P(String device_id, String ip, int port) {
            Log.d(TAG, "P2P连接成功,对方设备：" + device_id);
            if (mListenerMap.containsKey(LISTENER_PRESENTER)) {
                mListenerMap.get(LISTENER_PRESENTER).receviceP2P(device_id, ip, port);
            }
            if (mListenerMap.containsKey(LISTENER_SERVICE)) {
                mListenerMap.get(LISTENER_SERVICE).receviceP2P(device_id, ip, port);
            }
        }

        @Override
        public void beCalled(DeviceModel deviceModel) {
            if (mListenerMap.containsKey(LISTENER_PRESENTER)) {
                mListenerMap.get(LISTENER_PRESENTER).beCalled(deviceModel);
            }
            if (mListenerMap.containsKey(LISTENER_SERVICE)) {
                mListenerMap.get(LISTENER_SERVICE).beCalled(deviceModel);
            }
        }

        @Override
        public void pickUp(Transinformation tranfor) {
            if (mListenerMap.containsKey(LISTENER_PRESENTER)) {
                mListenerMap.get(LISTENER_PRESENTER).pickUp(tranfor);
            }
            if (mListenerMap.containsKey(LISTENER_SERVICE)) {
                mListenerMap.get(LISTENER_SERVICE).pickUp(tranfor);
            }
            TelePhone.getInstance().canTalk();
        }

        @Override
        public void handUp(Transinformation tranfor) {
            if (mListenerMap.containsKey(LISTENER_PRESENTER)) {
                mListenerMap.get(LISTENER_PRESENTER).handUp(tranfor);
            }
            if (mListenerMap.containsKey(LISTENER_SERVICE)) {
                mListenerMap.get(LISTENER_SERVICE).handUp(tranfor);
            }
            TelePhone.getInstance().stop();
        }
    };

    private void OnReceiveVoice(final byte[] buf) {
        fixedThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                TelePhone.getInstance().play(buf);
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (connectedByUDP != null) {
            connectedByUDP.close();
        }
        if (connectedByTCP != null) {
            connectedByTCP.close();
        }
    }
}
