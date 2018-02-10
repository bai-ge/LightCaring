package com.carefor.connect;

import android.util.Log;

import com.carefor.data.entity.DeviceModel;
import com.carefor.data.entity.MessageHeader;
import com.carefor.data.entity.Transinformation;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.telephone.TelePhone;
import com.carefor.telephone.TelePhoneAPI;
import com.google.common.collect.Interners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
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

    private ConnectedByUDP connectedByUDP = null;

    private static ExecutorService fixedThreadPool = null;

    private Map<String, OnConnectorListener> mListeners = null;

    private long mP2pConnetTime = 0;

    public final static String LISTENER_SERVICE = "service";
    public final static String LISTENER_PRESENTER = "presenter";

    Runnable mConnectRunnable = new Runnable() {
        @Override
        public void run() {
            CacheRepository cacheRepository = CacheRepository.getInstance();
            synchronized (Connector.class) {
                //检查是否已经连接指定主机
                if (cacheRepository.getServerIp() == null) {
                    return;
                }
                if (connectedByTCP != null && connectedByTCP.isWork()) {
                    if (connectedByTCP.getSocket().getInetAddress().getHostAddress().equals(cacheRepository.getServerIp())
                            && connectedByTCP.getSocket().getPort() == cacheRepository.getServerPort()) {
                        Log.d(TAG, "ip=" + connectedByTCP.getSocket().getInetAddress().getHostAddress());
                        Log.d(TAG, "serverIP=" + cacheRepository.getServerIp());
                        OnConnectSuccess();
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
                    socket.connect(address, 10000);
                    Log.d(TAG, "连接服务器成功");
                    cacheRepository.setLocalIp(socket.getLocalAddress().getHostAddress());
                    cacheRepository.setLocalPort(socket.getLocalPort());
                    connectedByTCP = new ConnectedByTCP(cacheRepository.getDeviceId(), socket, mOnTCPConnectListener);
                    //登录
                    String msg = MessageManager.login();
                    connectedByTCP.send(msg);
                } catch (IOException e) {
                    mOnTCPConnectListener.connectedFail();
                    mOnTCPConnectListener.exceptionCaught(connectedByTCP, e);
                }
            }
        }
    };

    private Connector() {
        fixedThreadPool = Executors.newFixedThreadPool(5);//创建最多能并发运行5个线程的线程池
        mListeners = Collections.synchronizedMap(new LinkedHashMap<String, OnConnectorListener>());
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
        synchronized (mListeners.getClass()) {
            mListeners.put(id, listener);
        }
    }

    //注销监听器
    public void unRegistConnectorListener(String id) {
        checkNotNull(id);
        synchronized (mListeners.getClass()) {
            mListeners.remove(id);
        }
    }

    //发送心跳包
    public void sendHeartBeat(){
        if(fixedThreadPool != null){
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    sendMessage("\r\n");
                }
            });
        }
    }

    public void afxConnectServer() {
        fixedThreadPool.submit(mConnectRunnable);
    }

    public void afxSendMessage(final String msg){
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
                        if(attr.containsKey("delay")){
                            Log.d(TAG, "数据延时："+ String.valueOf((long)attr.get("delay")));
                            TelePhone.getInstance().setDelayTime((long)attr.get("delay"));
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
                                        OnReceiveP2P(device_id, ip, packet.getPort());
                                    }
                                } else {
                                    mP2pConnetTime = System.currentTimeMillis();
                                    cacheRepository.setP2PIp(ip);
                                    cacheRepository.setP2PPort(packet.getPort());

                                    if (!cacheRepository.isP2PConnectSuccess()) {
                                        sendMessage(ip, packet.getPort(), MessageManager.udpP2P(cacheRepository.getDeviceId()));
                                    }
                                    cacheRepository.setP2PConnectSuccess(true);
                                    OnReceiveP2P(device_id, ip, packet.getPort());
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
            OnLoginSuccess();
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
            TelePhone.getInstance().beCall(id, new TelePhoneAPI.BaseCallBackAdapter(){
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
                OnBeCalled(CacheRepository.getInstance().getTalkWithDevice());
            }
            TelePhone.getInstance().checkUdpSuccess();
        }
    }

    private void executeHangUp(Transinformation tranfor) {
        String id = tranfor.getMessageHeader().getFrom();
        if (tranfor.getMessageHeader().getDeslist().contains(CacheRepository.getInstance().getDeviceId())) {
            //TODO 处理其他事
            OnHandUp(tranfor);
        }
    }

    private void executePickUp(Transinformation tranfor) {
        String id = tranfor.getMessageHeader().getFrom();
        if (tranfor.getMessageHeader().getDeslist().contains(CacheRepository.getInstance().getDeviceId())) {
            //TODO 处理其他事
            OnPickUp(tranfor);
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

        void loginSuccess();

        void receviceP2P(String device_id, String ip, int port);

        void beCalled(DeviceModel deviceModel);

        void pickUp(Transinformation tranfor);

        void handUp(Transinformation tranfor);
    }

    private void OnConnectSuccess() {
        if (mListeners.containsKey(LISTENER_PRESENTER)) {
            mListeners.get(LISTENER_PRESENTER).connectSuccess();
        } else if (mListeners.containsKey(LISTENER_SERVICE)) {
            mListeners.get(LISTENER_SERVICE).connectSuccess();
        }
//        Iterator<Entry<String, OnConnectorListener>> iterator = mListeners.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Entry<String, OnConnectorListener> entity = iterator.next();
//            try {
//                Method method = entity.getClass().getMethod(methodName);
//                method.invoke(entity, tranfor);
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private void OnLoginSuccess() {
        if (mListeners.containsKey(LISTENER_PRESENTER)) {
            mListeners.get(LISTENER_PRESENTER).loginSuccess();
        }
        if (mListeners.containsKey(LISTENER_SERVICE)) {
            mListeners.get(LISTENER_SERVICE).loginSuccess();
        }
    }

    private void OnBeCalled(DeviceModel deviceModel) {
        if (mListeners.containsKey(LISTENER_PRESENTER)) {
            mListeners.get(LISTENER_PRESENTER).beCalled(deviceModel);
        }
        if (mListeners.containsKey(LISTENER_SERVICE)) {
            mListeners.get(LISTENER_SERVICE).beCalled(deviceModel);
        }
    }

    private void OnPickUp(Transinformation tranfor) {
        if (mListeners.containsKey(LISTENER_PRESENTER)) {
            mListeners.get(LISTENER_PRESENTER).pickUp(tranfor);
        }
        if (mListeners.containsKey(LISTENER_SERVICE)) {
            mListeners.get(LISTENER_SERVICE).pickUp(tranfor);
        }
        TelePhone.getInstance().canTalk();
    }

    private void OnHandUp(Transinformation tranfor) {
        if (mListeners.containsKey(LISTENER_PRESENTER)) {
            mListeners.get(LISTENER_PRESENTER).handUp(tranfor);
        }
        if (mListeners.containsKey(LISTENER_SERVICE)) {
            mListeners.get(LISTENER_SERVICE).handUp(tranfor);
        }
        TelePhone.getInstance().stop();
    }

    private void OnReceiveVoice(byte[] buf) {
        TelePhone.getInstance().play(buf);
    }

    private void OnReceiveP2P(String device_id, String ip, int port) {
        Log.d(TAG, "P2P连接成功,对方设备：" + device_id);
        if (mListeners.containsKey(LISTENER_PRESENTER)) {
            mListeners.get(LISTENER_PRESENTER).receviceP2P(device_id, ip, port);
        }
        if (mListeners.containsKey(LISTENER_SERVICE)) {
            mListeners.get(LISTENER_SERVICE).receviceP2P(device_id, ip, port);
        }
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
