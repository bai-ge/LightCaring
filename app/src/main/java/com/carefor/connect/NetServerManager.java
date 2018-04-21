package com.carefor.connect;


import android.util.Log;

import com.carefor.connect.msg.MessageManager;
import com.carefor.data.entity.Candidate;
import com.carefor.data.entity.DeviceModel;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.jpush.MessageProcess;
import com.carefor.util.IPUtil;
import com.carefor.util.Loggerx;
import com.carefor.util.Tools;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class NetServerManager {
    private final static String TAG = NetServerManager.class.getCanonicalName();

    private final NetServerManager self;

    private static NetServerManager INSTANCE = null;

    private DatagramSocketServer datagramSocketServer;

    private Map<String, DeviceModel> devicesMap;

    private NetServerManager() {

        self = this;

        devicesMap = Collections.synchronizedMap(new LinkedHashMap<String, DeviceModel>());

        datagramSocketServer = new DatagramSocketServer();
        datagramSocketServer.registerServerListener(mOnDatagramSocketServerListener);

        datagramSocketServer.start();
        CacheRepository.getInstance().setLocalIp(IPUtil.getLocalIPAddress(true));
        CacheRepository.getInstance().setLocalUdpPort(datagramSocketServer.getLocalPort());
    }

    public static NetServerManager getInstance() {
        if (INSTANCE == null) {
            synchronized (NetServerManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NetServerManager();
                }
            }
        }
        return INSTANCE;
    }

    public int getUdpPort() {
        if (datagramSocketServer != null && datagramSocketServer.isStart()) {
            return datagramSocketServer.getLocalPort();
        }
        return 0;
    }


    public List<DeviceModel> getAllDevices() {
        if (devicesMap != null) {
            return new ArrayList<>(devicesMap.values());
        }
        return null;
    }


    private OnDatagramSocketServerListener mOnDatagramSocketServerListener = new OnDatagramSocketServerListener() {

        @Override
        public void onServerStart(DatagramSocketServer server) {
            // TODO Auto-generated method stub
            Loggerx.d(TAG, "UDP 监听端口：" + server.getLocalPort());
        }

        @Override
        public void onServerReceivePacket(ConnectedByUDP connector, SocketPacket packet) {
            // TODO Auto-generated method stub
            if(!packet.isHeartBeat() && !packet.isDisconnected()){
                if(packet.getHeaderBuf() == null){
                    Log.v(TAG, "UDP 接收到数据:" + packet);
                }else{
                    Log.v("voice", "size ="+packet.getContentBuf().length+"connect ="+connector.getAddress().getStringRemoteAddress());
                }
                MessageProcess.receive(connector, packet);
            }

        }

        @Override
        public void onServerClose(DatagramSocketServer server) {
            // TODO Auto-generated method stub
            Loggerx.d(TAG, "UDP 停止监听端口：" + server.getLocalPort());
        }

        @Override
        public void onClientDisconnected(ConnectedByUDP connector) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onClientConnected(ConnectedByUDP connector) {
            // TODO Auto-generated method stub

        }
    };


    //登录验证之后的用户使用的监听器
    private OnConnectedListener mClientConnectedListener = new OnConnectedListener() {

        @Override
        public void onResponse(BaseConnector connector, SocketPacket responsePacket) {
            // TODO Auto-generated method stub
            Loggerx.d(TAG, "TCP接收到消息" + responsePacket.toString());
        }

        @Override
        public void onDisconnected(BaseConnector connector) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onConnected(BaseConnector connector) {
            // TODO Auto-generated method stub

        }
    };

    public void connectToAddressByTCP(SocketClientAddress address, OnConnectedListener callback) {
        ConnectedByTCP connectedByTCP = new ConnectedByTCP(address);
        connectedByTCP.registerConnectedListener(callback);
        connectedByTCP.registerConnectedListener(mClientConnectedListener); //双方都需要发送自己的UUID等验证信息
        connectedByTCP.connect();
    }

    public void connectToAddressByUDP(SocketClientAddress address, OnDatagramSocketServerListener callack) {
        ConnectedByUDP connectedByUDP = new ConnectedByUDP(address);
        connectedByUDP.setRunningSocket(datagramSocketServer);
        datagramSocketServer.registerServerListener(callack);
        connectedByUDP.connect();
    }

    public DeviceModel getDeviceModelById(String deviceid) {
        if (Tools.isEmpty(deviceid)) {
            return null;
        } else {
            return devicesMap.get(deviceid);
        }
    }

    public ConnectedByTCP getTCPConnectorById(String deviceid) {
        if (Tools.isEmpty(deviceid)) {
            return null;
        } else {
            DeviceModel deviceModel = devicesMap.get(deviceid);
            if (deviceModel != null) {
                return deviceModel.getConnectedByTCP();
            }
        }
        return null;
    }

    public ConnectedByUDP getUDPConnectorById(String deviceid) {
        if (Tools.isEmpty(deviceid)) {
            return null;
        } else {
            DeviceModel deviceModel = devicesMap.get(deviceid);
            if (deviceModel != null) {
                return deviceModel.getConnectedByUDP();
            }
        }
        return null;
    }
    public void put(String from, DeviceModel deviceModel) {
        // TODO Auto-generated method stub
        devicesMap.put(from, deviceModel);
    }
    public DeviceModel remove(String deviceId) {
        // TODO Auto-generated method stub
        if (devicesMap != null) {
            return devicesMap.remove(deviceId);
        }
        return null;
    }

    public ConnectedByUDP getUDPConnectorByAddress(String ip, int port) {
        ConnectedByUDP connectedByUDP = datagramSocketServer.get(ip + ":" + port);
        if (connectedByUDP == null) {
            connectedByUDP = new ConnectedByUDP(new SocketClientAddress(ip, port));
        }
        connectedByUDP.setRunningSocket(datagramSocketServer);
        return connectedByUDP;
    }

    public ConnectedByUDP getUDPConnectorByAddress(String ip, String port) {
        ConnectedByUDP connectedByUDP = datagramSocketServer.get(ip + ":" + port);
        if (connectedByUDP == null) {
            connectedByUDP = new ConnectedByUDP(new SocketClientAddress(ip, port));
        }
        connectedByUDP.setRunningSocket(datagramSocketServer);
        return connectedByUDP;
    }

    public boolean sendMessage(String ip, int port, byte[] buf) {
        if (datagramSocketServer != null && datagramSocketServer.isStart()) {
            InetAddress address = null;
            try {
                address = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
                datagramSocketServer.getRunningServerSocket().send(packet);
                return true;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public void tryP2PConnect(DeviceModel deviceModel) {
        Log.d(TAG, "尝试与远程设备建立P2P连接");
        Log.d(TAG, "远程设备");
        Log.d(TAG, "设备ID" + deviceModel.getDeviceId());
        Log.d(TAG, "远程地址：" + deviceModel.getRemoteIp() + ":" + deviceModel.getRemoteUdpPort());
        Log.d(TAG, "本地地址：" + deviceModel.getLocalIp() + ":" + deviceModel.getLocalUdpPort());

        Log.d(TAG, "本地设备");
        Log.d(TAG, "设备ID" + CacheRepository.getInstance().getDeviceId());
        Log.d(TAG, "远程地址：" + CacheRepository.getInstance().getRemoteIp() + ":" + CacheRepository.getInstance().getRemoteUdpPort());
        Log.d(TAG, "本地地址：" + CacheRepository.getInstance().getLocalIp() + ":" + CacheRepository.getInstance().getLocalUdpPort());
        byte[] buf = MessageManager.udpP2P(CacheRepository.getInstance().getDeviceId());
        sendMessage(deviceModel.getLocalIp(), deviceModel.getLocalUdpPort(), buf);
        sendMessage(deviceModel.getRemoteIp(), deviceModel.getLocalUdpPort(), buf);
    }

    public void tryUdpTest() {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        String localIp = IPUtil.getLocalIPAddress(true);
        String localPort = datagramSocketServer.getLocalPort() + "";
        String msg = MessageManager.udpTest(cacheRepository.getDeviceId(), localIp, localPort);
        ConnectedByUDP connectedByUDP;
        if (!cacheRepository.getServerIp().equals("120.78.148.180") && !cacheRepository.getServerIp().equals("39.180.74.14")) {
            connectedByUDP = getUDPConnectorByAddress(cacheRepository.getServerIp(), cacheRepository.getServerUdpPort());
            connectedByUDP.sendString(msg);
        }
        connectedByUDP = getUDPConnectorByAddress("120.78.148.180", 12059);
        connectedByUDP.sendString(msg);

        connectedByUDP = getUDPConnectorByAddress("39.180.74.14", 12059);
        connectedByUDP.sendString(msg);
    }

    public void tryUdpTest(String msg) {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        ConnectedByUDP connectedByUDP;
        if (!cacheRepository.getServerIp().equals("120.78.148.180") && !cacheRepository.getServerIp().equals("39.180.74.14")) {
            connectedByUDP = getUDPConnectorByAddress(cacheRepository.getServerIp(), cacheRepository.getServerUdpPort());
            connectedByUDP.sendString(msg);
        }
        connectedByUDP = getUDPConnectorByAddress("120.78.148.180", 12059);
        connectedByUDP.sendString(msg);

        connectedByUDP = getUDPConnectorByAddress("39.180.74.14", 12059);
        connectedByUDP.sendString(msg);
    }

    public void tryPTPConnect(List<Candidate> candidates, String deviceId) {
        DeviceModel deviceModel = getDeviceModelById(deviceId);
//        if (deviceModel != null && deviceModel.getConnectedByUDP() != null && deviceModel.getConnectedByUDP().isConnected(5000)) {
//            Log.d(TAG, "已经建立P2P 连接：id ="+deviceId+", connetor ="+deviceModel.getConnectedByUDP().getAddress().getStringRemoteAddress() );
//            TelePhone.getInstance().showLog("P2P 连接成功 " + deviceModel.getConnectedByUDP().getAddress().getStringRemoteAddress());
//            return;
//        }
        ConnectedByUDP connectedByUDP = null;
        Candidate candidate = null;
        Candidate candidate1;
        Candidate candidate2;
        int port, port1, port2;
        String msg = null;
        CacheRepository cacheRepository = CacheRepository.getInstance();
        JSONObject jsonObject = MessageManager.tryPTPConnect(cacheRepository.getDeviceId(), deviceId);
        if(jsonObject != null){
            msg = jsonObject.toString();
        }
        //判断自己的NAT类型
//        1, 全锥型(Full Cone)
//
//        2,  受限锥型(Restricted Cone)， 或者说是IP受限锥型
//
//        3,  端口受限锥型(Port Restricted Cone), 或者说是IP + PORT受限锥型
//
//        4,  对称型(Symmetric)
        List<Candidate> candidateList = CacheRepository.getInstance().getCandidates();
        if (candidateList == null || candidateList.size() == 0) {
            tryUdpTest();
            return;
        }
        boolean isSymmetric = true;
        boolean isOpSymmetric = true;
        if (candidateList.size() == 1) {
            candidate = candidateList.get(0);
            if (Tools.isEquals(candidate.getLocalPort(), candidate.getRemotePort())) {
                isSymmetric = false;
            }

        } else if (candidateList.size() > 1) {
            candidate1 = candidateList.get(0);
            candidate2 = candidateList.get(1);
            if (Tools.isEquals(candidate1.getRemotePort(), candidate2.getRemotePort())) {
                isSymmetric = false;
            }
        }

        //判断对方NAT类型
        if (candidates.size() == 1) {
            candidate = candidates.get(0);
            if (Tools.isEquals(candidate.getLocalPort(), candidate.getRemotePort())) {
                isOpSymmetric = false;
            }
            //TODO 当Symmetric 处理
        } else if (candidates.size() > 1) {
            candidate1 = candidates.get(0);
            candidate2 = candidates.get(1);
            if (Tools.isEquals(candidate1.getRemotePort(), candidate2.getRemotePort())) {
                isOpSymmetric = false;
            }
        }
        if (isSymmetric && isOpSymmetric) {
            //自己和对方都是对称型，不能随便发送数据
            if (candidates.size() == 1 || candidateList.size() == 1) {
                //假对称型，不确定
                if(candidates.size() == 1){
                    candidate = candidates.get(0);
                    port = Integer.valueOf(candidate.getRelayPort());
                    // 预测几条
                    for (int i = 0; i < 10; i++) {
                        port++;
                        connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), port);
                        connectedByUDP.sendString(msg);
                    }
                    connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), candidate.getRemotePort());
                    connectedByUDP.sendString(msg);

                    if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
                        connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
                        connectedByUDP.sendString(msg);
                    }
                }else if(candidates.size() > 1){
                    candidate1 = candidates.get(0);
                    candidate2 = candidates.get(1);
                    port1 = Integer.valueOf(candidate1.getRemotePort());
                    port2 = Integer.valueOf(candidate2.getRemotePort());

                    if (Math.abs(port1 - port2) == 1) {//相差1
                         port = Math.max(port1, port2);
                        // 预测几条
                        for (int i = 0; i < 10; i++) {
                            port++;
                            connectedByUDP = getUDPConnectorByAddress(candidate1.getRemoteIp(), port);
                            connectedByUDP.sendString(msg);
                        }

                        if (!Tools.isEquals(candidate1.getLocalIp(), candidate1.getRemoteIp())) {
                            connectedByUDP = getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                            connectedByUDP.sendString(msg);
                        }

                    } else if (Math.abs(port1 - port2) < 10) {
                         port = Math.max(port1, port2);
                        // 预测几条
                        for (int i = 0; i < 10; i++) {
                            port++;
                            connectedByUDP = getUDPConnectorByAddress(candidate1.getRemoteIp(), port);
                            connectedByUDP.sendString(msg);
                        }
                        if (!Tools.isEquals(candidate1.getLocalIp(), candidate1.getRemoteIp())) {
                            connectedByUDP = getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                            connectedByUDP.sendString(msg);
                        }
                    } else { //不可能猜测
                        Log.d(TAG, "Symmetric 端口分配相差过大");
                        connectedByUDP = getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                        connectedByUDP.sendString(msg);
                    }
                }
            } else {//真对称型
                candidate1 = candidates.get(0);
                candidate2 = candidates.get(1);
                port1 = Integer.valueOf(candidate1.getRemotePort());
                port2 = Integer.valueOf(candidate2.getRemotePort());

                if (Math.abs(port1 - port2) == 1) {//相差1
                    port = Math.max(port1, port2);
                    // 预测几条
                    for (int i = 0; i < 10; i++) {
                        port++;
                        connectedByUDP = getUDPConnectorByAddress(candidate1.getRemoteIp(), port);
                        connectedByUDP.sendString(msg);
                    }
                    connectedByUDP = getUDPConnectorByAddress(candidate1.getRemoteIp(), port + 1);
                    connectedByUDP.sendString(msg);

                    if (!Tools.isEquals(candidate1.getLocalIp(), candidate1.getRemoteIp())) {
                        connectedByUDP = getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                        connectedByUDP.sendString(msg);
                    }

                } else if (Math.abs(port1 - port2) < 10) {
                    port = Math.max(port1, port2);
                    // 预测几条
                    for (int i = 0; i < 10; i++) {
                        port++;
                        connectedByUDP = getUDPConnectorByAddress(candidate1.getRemoteIp(), port);
                        connectedByUDP.sendString(msg);
                    }
                    if (!Tools.isEquals(candidate1.getLocalIp(), candidate1.getRemoteIp())) {
                        connectedByUDP = getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                        connectedByUDP.sendString(msg);
                    }
                } else { //不可能猜测
                    Log.d(TAG, "Symmetric 端口分配相差过大");
                    connectedByUDP = getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                    connectedByUDP.sendString(msg);
                }

            }
        } else if (isSymmetric && !isOpSymmetric) {
            //自己对称型，而对方不是，目标唯一
            candidate = candidates.get(0);
            connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), candidate.getRemotePort());
            connectedByUDP.sendString(msg);
            if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
                connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
                connectedByUDP.sendString(msg);
            }
        } else if (!isSymmetric && isOpSymmetric) {
            //自己非对称型，而对方是，自己可以随便发送数据
            if (candidates.size() == 1) {
                candidate = candidates.get(0);
                port = Integer.valueOf(candidate.getRelayPort());
                // 预测几条
                for (int i = 0; i < 10; i++) {
                    port++;
                    connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), port);
                    connectedByUDP.sendString(msg);
                }
                connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), candidate.getRemotePort());
                connectedByUDP.sendString(msg);

                if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
                    connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
                    connectedByUDP.sendString(msg);
                }
            } else if (candidates.size() > 1) {
                candidate1 = candidates.get(0);
                candidate2 = candidates.get(1);
                port1 = Integer.valueOf(candidate1.getRemotePort());
                port2 = Integer.valueOf(candidate2.getRemotePort());

                if (Math.abs(port1 - port2) == 1) {//相差1
                    port = Math.max(port1, port2);
                    // 预测几条
                    for (int i = 0; i < 10; i++) {
                        port++;
                        connectedByUDP = getUDPConnectorByAddress(candidate1.getRemoteIp(), port);
                        connectedByUDP.sendString(msg);
                    }

                    if (!Tools.isEquals(candidate1.getLocalIp(), candidate1.getRemoteIp())) {
                        connectedByUDP = getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                        connectedByUDP.sendString(msg);
                    }

                } else if (Math.abs(port1 - port2) < 10) {
                     port = Math.max(port1, port2);
                    // 预测几条
                    for (int i = 0; i < 10; i++) {
                        port++;
                        connectedByUDP = getUDPConnectorByAddress(candidate1.getRemoteIp(), port);
                        connectedByUDP.sendString(msg);
                    }
                    if (!Tools.isEquals(candidate1.getLocalIp(), candidate1.getRemoteIp())) {
                        connectedByUDP = getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                        connectedByUDP.sendString(msg);
                    }
                } else { //不可能猜测
                    Log.d(TAG, "Symmetric 端口分配相差过大");
                    connectedByUDP = getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                    connectedByUDP.sendString(msg);
                }
            }

        } else {
            candidate = candidates.get(0);
            connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), candidate.getRemotePort());
            connectedByUDP.sendString(msg);
            if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
                connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
                connectedByUDP.sendString(msg);
            }
        }
    }
}
