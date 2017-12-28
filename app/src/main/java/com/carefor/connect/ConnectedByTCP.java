package com.carefor.connect;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;



public class ConnectedByTCP {
    private final static String TAG = ConnectedByTCP.class.getCanonicalName();
	private Socket mSocket;
	private boolean mIsWork = false;
	private BufferedReader mBufReader = null;
	private PrintWriter mBufWriter = null;
	private String mid = null;
	private long connectTime = 0;
	private OnTCPConnectListener mListener = null;
	private Thread mReceiveThread = null;

	private int timeOut = 400000;

	private long mReceiveTime;

	public ConnectedByTCP(String deviceId, Socket socket, OnTCPConnectListener listener) {
		this.mid = deviceId;
		this.connectTime = System.currentTimeMillis();
		this.mSocket = socket;
		this.mListener = listener;
		try {
			if (socket != null) {
				socket.setKeepAlive(true);
				socket.setSoTimeout(timeOut);//40s 无信息传输断开
				mBufWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())));// 发送数据
				mBufReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream())); // 接收数据
				mIsWork = true;
                mReceiveTime = System.currentTimeMillis();
				receive();
			} else {
				mIsWork = false;
				listener.disconnected(mid);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void receive() {
		mReceiveThread = new Thread() {
			@Override
			public void run() {
				Log.d(TAG, "开启接收线程"+mSocket.getInetAddress().getHostName()+":"+mSocket.getPort());
				try {
					while (mIsWork && mSocket != null) {
						String msg = mBufReader.readLine();
						Log.v(TAG, mid+"接收到信息"+msg);
                        mReceiveTime = System.currentTimeMillis();
						if (msg == null) {
							mIsWork = false;
						} else if (!msg.equals("\n") && !msg.isEmpty()) {
							mListener.receiveMessage(mid, msg);
						}
					}
					Log.d(TAG, "接收线程关闭"+mSocket.getInetAddress().getHostName()+":"+mSocket.getPort());
					
				} catch (Exception e) {
					throwException(e);
				}
				mIsWork = false;
				mListener.disconnected(mid);
			}
		};
		mReceiveThread.start();
	}

	// 同步，只有一个线程能进入
	public synchronized boolean send(String msg) {
		if (mIsWork && mSocket != null && mBufWriter != null) {
			mBufWriter.println(msg);
			mBufWriter.flush();
			Log.d(TAG, "发送数据" + msg.length());
			return true;
		}
		// 发送失败
		Log.d(TAG,  "发送数据失败" + msg.length());
		mListener.disconnected(mid);
		return false;
	}

	public void close() {
        try {
            mIsWork = false;
            mBufReader.close();
            mBufWriter.close();
           if(mSocket != null && !mSocket.isClosed()){
                mSocket.close();
               mSocket = null;
           }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "{deviceId=" + mid + "," + "address=" + mSocket.getInetAddress().getHostAddress() + "," + "port="
				+ mSocket.getPort() + "}";
	}

	private void throwException(Throwable cause) {
		if (mListener != null) {
			mListener.exceptionCaught(this, cause);
		}
	}

	public boolean isWork() {
		return mIsWork && (System.currentTimeMillis() - mReceiveTime) < 60 * 1000;
	}

	public void setIsWork(boolean isWork) {
		this.mIsWork = isWork;
	}

	public String getid() {
		return mid;
	}

	public void setid(String mid) {
		this.mid = mid;
	}

	public long getConnectTime() {
		return connectTime;
	}

	public void setConnectTime(long connectTime) {
		this.connectTime = connectTime;
	}

	public Socket getSocket() {
		return mSocket;
	}

	public void setSocket(Socket socket) {
		this.mSocket = socket;
	}

	public OnTCPConnectListener getmListener() {
		return mListener;
	}

	public void setListener(OnTCPConnectListener listener) {
		this.mListener = listener;
	}

	public interface OnTCPConnectListener{

        public void connectedSuccess(ConnectedByTCP connectedByTCP);

        public void connectedFail();

        public void receiveMessage(String uuid, String msg);

        public void disconnected(String uuid);

        public void reconnected(String uuid);

        public void exceptionCaught(ConnectedByTCP connectedByTCP, Throwable cause);
    }

}
