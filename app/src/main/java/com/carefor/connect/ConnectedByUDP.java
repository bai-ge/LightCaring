package com.carefor.connect;

import android.util.Log;

import com.carefor.data.entity.MessageData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;


public class ConnectedByUDP {

    private final static String TAG = ConnectedByUDP.class.getCanonicalName();

    public static int localPort;

    private final static int DATA_LEN = 1024;

    private DatagramSocket mSocket;

    private boolean mIsWork = false;

    private boolean mIsRuning;

    private Thread mReceiveThread = null;

    private OnUDPConnectListener mListener = null;

    public ConnectedByUDP() {
        try {
            mSocket = new DatagramSocket();
            localPort = mSocket.getLocalPort();
            Log.d(TAG, "UDP监听端口:" + localPort + "成功");
            mIsWork = true;
            mIsRuning = false;
        } catch (SocketException e) {
            Log.d(TAG, "UDP监听端口:" + localPort + "失败");
            mIsWork = false;
            e.printStackTrace();
        }
    }

    public ConnectedByUDP(int localPort) {
        try {
            this.localPort = localPort;
            mSocket = new DatagramSocket(localPort);
            Log.d(TAG, "UDP监听端口:" + localPort + "成功");
            mIsWork = true;
            mIsRuning = false;
        } catch (SocketException e) {
            Log.d(TAG, "UDP监听端口:" + localPort + "失败");
            mIsWork = false;
            e.printStackTrace();
        }
    }

    public void setOnUDPConnectListener(OnUDPConnectListener listener) {
        this.mListener = listener;
    }

    public void start() {
        if (mIsWork && !mIsRuning) {
            mIsRuning = true;
            receive();
        }
    }
    public boolean isWork(){
        return mIsWork;
    }

    private void receive() {
        mReceiveThread = new Thread() {
            @Override
            public void run() {
                while (mIsWork && mSocket != null) {
                    DatagramPacket packet = new DatagramPacket(new byte[DATA_LEN], DATA_LEN);
                    try {
                        mSocket.receive(packet);
                        if (mListener != null && packet.getLength() > 0) {
                            mListener.receiveMessage(packet);
                        }
                        if (packet.getLength() >= 4) {
                           if(mListener != null){
                               mListener.receiveMessage(packet);
                           }
                        }
                        packet = null;
                    } catch (IOException e) {
                        mIsWork = false;
                        e.printStackTrace();
                    }

                }
                mIsRuning = false;
                mIsWork = false;
                if (mListener != null) {
                    mListener.doNotWork();
                }
            }
        };
        mReceiveThread.start();
    }


    public synchronized boolean send(DatagramPacket packet) {
        if (packet == null) {
            return false;
        }
        if (mSocket == null || !mIsWork) {
            return false;
        }
        try {
            mSocket.send(packet);
            return true;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    public void close() {
        mIsWork = false;
        if (mSocket != null && !mSocket.isClosed()) {
            mSocket.close();
            mSocket = null;
        }
    }

    public interface OnUDPConnectListener {
        void connectedSuccess(String userId, String ip, int port);

        void receiveMessage(DatagramPacket packet);

        void doNotWork();
    }
}
