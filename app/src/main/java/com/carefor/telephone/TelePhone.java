package com.carefor.telephone;


import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.carefor.broadcast.SendMessageBroadcast;
import com.carefor.callback.CallbackManager;
import com.carefor.callback.SeniorCallBack;
import com.carefor.connect.ConnectedByUDP;
import com.carefor.connect.NetServerManager;
import com.carefor.connect.SocketPacket;
import com.carefor.connect.msg.MessageManager;
import com.carefor.data.entity.Candidate;
import com.carefor.data.entity.DeviceModel;
import com.carefor.data.entity.User;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.util.IPUtil;
import com.carefor.util.Tools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/10/30.
 *
 * TODO 即将改为服务，通过发送广播控制通话服务器
 */

public class TelePhone implements SpeexTalkRecorder.OnRecorderListener, SpeexTalkPlayer.OnPlayerListener, TelePhoneAPI {

    private static final String TAG = TelePhone.class.getCanonicalName();

    private static TelePhone INSTANCE = null;

    private SpeexTalkRecorder recorder;

    private SpeexTalkPlayer player;

    private OnTelePhoneListener mListener;

    private int mStatus;

    private String mTalkWith; //通话对方的ID
    private DeviceModel mTalkWithDevice = null;

    private String mTalkWithName = ""; //通话对方的名字

    private static ExecutorService fixedThreadPool = null;

    private long mDelayTime;//真正的网络延时

    private long mDiffTime; //两个系统时间的差值，包括网络延时

    private long mPlayTime;

    private ByteBuffer voiceBuf;

    private static MediaPlayer mMediaPlayer;

    private Context mContext;

    private ArrayList<LogBean> logs = new ArrayList<>();

    private TelePhone() {
        mStatus = Status.LEISURE;
        voiceBuf = ByteBuffer.allocate(20 * 20);//20ms * 20个
        mMediaPlayer = new MediaPlayer();
        fixedThreadPool = Executors.newFixedThreadPool(5);//创建最多能并发运行5个线程的线程池

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(isCalling() || beCalled()){
                    mMediaPlayer.start();
                }
            }
        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.start();
            }
        });
    }

    public static TelePhone getInstance() {
        if (INSTANCE == null) {
            synchronized (TelePhone.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new TelePhone();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context){
        mContext = checkNotNull(context);
    }

    public void startActivity(Class activity){
        if(mContext == null){
            new IllegalStateException("Telephone is not init");
        }
        if(activity != null){
            Intent intent = new Intent(mContext, PhoneActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }


    public String getTalkWithId() {
        return mTalkWith;
    }

    public void setTalkWithId(String mTalkWith) {
        this.mTalkWith = mTalkWith;
    }

    public String getTalkWithName() {
        return mTalkWithName;
    }

    public void setTalkWithName(String talkWithName) {
        this.mTalkWithName = talkWithName;
    }

    public void setOnTelePhoneListener(OnTelePhoneListener listener) {
        this.mListener = listener;
    }

    public void play(byte[] recordData) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(recordData);
        byte[] voice = new byte[20];
        if (player != null) {
            mPlayTime = System.currentTimeMillis();
            while (byteBuffer.remaining() >= voice.length) {
                byteBuffer.get(voice);
                synchronized (TelePhone.class) {
                    if (player != null) {
                        player.play(voice);
                    }else {
                        return;
                    }
                }
            }
        }
        if(mListener != null){
           mListener.showDelay(mDelayTime);
        }
    }

    public void ring(Context context, Uri uri){
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(context, uri);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            ring();
        }
    }
    public void ring(){
        mMediaPlayer.reset();
        try {
            if(mContext == null){
                new IllegalStateException("Telephone is not init");
            }
            AssetManager assetManager = mContext.getAssets();
            AssetFileDescriptor fileDescriptor = assetManager.openFd("mi_ring.ogg");
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stopRing(){
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
        }
    }
    public int getStatus() {
        return mStatus;
    }

    private void setStatus(int status) {
        //TODO 进行严格的状态转换
        mStatus = status;
        if(mListener != null){
            mListener.onChange(status);
        }
    }

    public long getDelayTime() {
        return mDelayTime;
    }

    public void setDelayTime(long delayTime) {
        this.mDelayTime = delayTime;
    }

    public long getDiffTime() {
        return mDiffTime;
    }

    public void setDiffTime(long diffTime) {
        this.mDiffTime = diffTime;
    }

    @Override
    public void exceptionCaught(Throwable cause) {
        if (mListener != null) {
            mListener.exceptionCaught(cause);
        }
    }

    @Override
    public void handleRecordData(byte[] recordData) {
//        if (mStatus == Status.IS_WORKING && mConnectedByUDP != null) {
//            mConnectedByUDP.send(ConnectedByUDP.MessageTag.VOICE, recordData);
//        }
        Log.d(TAG, "录音数据" + recordData.length);
        NetServerManager netServerManager = NetServerManager.getInstance();
        CacheRepository cacheRepository = CacheRepository.getInstance();
        ConnectedByUDP connectedByUDP;
        if(System.currentTimeMillis() - mPlayTime >= 10 * 1000){
            //掉线了
            showTip("对方已经掉线");
            if(mListener != null){
                mListener.exceptionCaught(new IOException());
            }
            stop();
        }
        if(voiceBuf.remaining() == recordData.length){
            voiceBuf.put(recordData);
            SocketPacket socketPacket = MessageManager.voice(cacheRepository.getDeviceId(), getTalkWithId(), voiceBuf.array(), (int) getDiffTime());
            if(!socketPacket.isPacket()){
                socketPacket.packet();
            }
            if(cacheRepository.isP2PConnectSuccess()) {
                connectedByUDP = netServerManager.getUDPConnectorById(mTalkWith);
                if(connectedByUDP != null && connectedByUDP.isConnected()){
                    connectedByUDP.sendPacket(socketPacket);
                }else{
                    netServerManager.sendMessage(cacheRepository.getServerIp(), cacheRepository.getServerUdpPort(), socketPacket.getAllBuf());
                }
            } else{
                netServerManager.sendMessage(cacheRepository.getServerIp(), cacheRepository.getServerUdpPort(), socketPacket.getAllBuf());
            }
            voiceBuf.position(0);
        }else if(voiceBuf.remaining() < recordData.length){
            int pos = voiceBuf.position();
            byte[] voice = new byte[pos];
            voiceBuf.position(0);
            voiceBuf.get(voice);
            SocketPacket socketPacket = MessageManager.voice(cacheRepository.getDeviceId(), getTalkWithId(), voiceBuf.array(), (int) getDiffTime());
            if(!socketPacket.isPacket()){
                socketPacket.packet();
            }

            if(cacheRepository.isP2PConnectSuccess()) {
                connectedByUDP = netServerManager.getUDPConnectorById(mTalkWith);
                if(connectedByUDP != null && connectedByUDP.isConnected()){
                    connectedByUDP.sendPacket(socketPacket);
                }else{
                    netServerManager.sendMessage(cacheRepository.getServerIp(), cacheRepository.getServerUdpPort(), socketPacket.getAllBuf());
                }
            } else{
                netServerManager.sendMessage(cacheRepository.getServerIp(), cacheRepository.getServerUdpPort(), socketPacket.getAllBuf());
            }

            voiceBuf.position(0);
            voiceBuf.put(recordData);
        }else {
            voiceBuf.put(recordData);
        }
      //  buf = null;//手动释放，数据量太大了
    }


    private void afxCheckUdpConnector() {
        fixedThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                checkUdpConnector();
            }
        });
    }

    private void checkUdpConnector() {
        showLog("检查网络状态");
        showLog("server IP ="+CacheRepository.getInstance().getServerIp());
        showLog("server tcp port ="+CacheRepository.getInstance().getServerPort());
        showLog("server udp port ="+CacheRepository.getInstance().getServerUdpPort());
        NetServerManager netServerManager = NetServerManager.getInstance();
        CacheRepository cacheRepository = CacheRepository.getInstance();
        SeniorCallBack callBack = new SeniorCallBack(){
            @Override
            public synchronized void loadCandidate(Candidate candidate) {
                showLog("From "+candidate.getFrom());
                showLog("Local "+candidate.getLocalIp()+":"+candidate.getLocalPort());
                showLog("Remote "+candidate.getRemoteIp()+":"+candidate.getRemotePort());
                showLog("Relay "+candidate.getRelayIp()+":"+candidate.getRelayPort());
                setDelayTime((long) (candidate.getDelayTime() * 1.0 / 2));
                if(mListener != null){
                    mListener.showDelay((long) (candidate.getDelayTime() * 1.0 / 2));
                }
                super.loadCandidate(candidate);
            }
        };
        String callId = Tools.ramdom();
        callBack.setTimeout(5000);
        callBack.setId(callId);
        CallbackManager.getInstance().put(callBack);
        String msg = MessageManager.udpTest(cacheRepository.getDeviceId(), callId, IPUtil.getLocalIPAddress(true), cacheRepository.getLocalUdpPort()+"");
        netServerManager.tryUdpTest(msg);
//        CacheRepository cacheRepository = CacheRepository.getInstance();
//        SocketPacket socketPacket = new SocketPacket();
//        String msg = MessageManager.udpTest(cacheRepository.who().getDeviceId(), IPUtil.getLocalIPAddress(true), ""+netServerManager.getUdpPort());
//        socketPacket.setContentBuf(msg.getBytes());
//        socketPacket.packet();
//
//        netServerManager.sendMessage(cacheRepository.getServerIp(), cacheRepository.getServerUdpPort(), socketPacket.getAllBuf());
//            //TODO 检测NAT类型
//        netServerManager.sendMessage("120.78.148.180", 12059, socketPacket.getAllBuf());
    }

    @Override
    public void afxBeCall(final String deviceId, final String name, final BaseCallBack callBack) {
        fixedThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                beCall(deviceId, name, callBack);
            }
        });
    }

    @Override
    public void beCall(String deviceId, String name, BaseCallBack callBack) {
        checkNotNull(deviceId);
        checkNotNull(callBack);
        if (mStatus == Status.LEISURE) {
            showLog("id = "+ CacheRepository.getInstance().getDeviceId());
            showLog("be call ="+deviceId);
            setTalkWithName(name);
            if(mListener != null){
                mListener.showName(name);
            }
            mTalkWith = deviceId;
            setStatus(Status.CALLED);
            String ringUri = CacheRepository.getInstance().getRingUri();
            if(!Tools.isEmpty(ringUri)){
                ring(mContext, Uri.parse(ringUri));
            }else{
                ring();
            }
            checkUdpConnector();
            String msg = MessageManager.replyCallTo(deviceId);
            SendMessageBroadcast.getInstance().sendMessage(msg);
        } else {
            showLog("错误状态, 当前" + mStatus);
            callBack.isBusy();
        }
    }

//    /** The audio stream for phone calls */
//    public static final int STREAM_VOICE_CALL = AudioSystem.STREAM_VOICE_CALL;
//    /** The audio stream for system sounds */
//    public static final int STREAM_SYSTEM = AudioSystem.STREAM_SYSTEM;
//    /** The audio stream for the phone ring */
//    public static final int STREAM_RING = AudioSystem.STREAM_RING;
//    /** The audio stream for music playback */
//    public static final int STREAM_MUSIC = AudioSystem.STREAM_MUSIC;
//    /** The audio stream for alarms */
//    public static final int STREAM_ALARM = AudioSystem.STREAM_ALARM;
//    /** The audio stream for notifications */
//    public static final int STREAM_NOTIFICATION = AudioSystem.STREAM_NOTIFICATION;
//    /** @hide The audio stream for phone calls when connected to bluetooth */
//    public static final int STREAM_BLUETOOTH_SCO = AudioSystem.STREAM_BLUETOOTH_SCO;
//    /** @hide The audio stream for enforced system sounds in certain countries (e.g camera in Japan) */
//    public static final int STREAM_SYSTEM_ENFORCED = AudioSystem.STREAM_SYSTEM_ENFORCED;
//    /** The audio stream for DTMF Tones */
//    public static final int STREAM_DTMF = AudioSystem.STREAM_DTMF;
//    /** @hide The audio stream for text to speech (TTS) */
//    public static final int STREAM_TTS = AudioSystem.STREAM_TTS;

    @Override
    public void connectSuccess() {
        mMediaPlayer.reset();
        try {
            if(mContext == null){
                new IllegalStateException("Telephone is not init");
            }
            AssetManager assetManager = mContext.getAssets();
            AssetFileDescriptor fileDescriptor = assetManager.openFd("connect_success.mp3");
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showLog("对方连接成功");
    }

    @Override
    public void oppBusy() {
        mMediaPlayer.reset();
        try {
            if(mContext == null){
                new IllegalStateException("Telephone is not init");
            }
            AssetManager assetManager = mContext.getAssets();
            AssetFileDescriptor fileDescriptor = assetManager.openFd("busy.mp3");
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showLog("对方正忙");
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(8000);
                    INSTANCE.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void afxCallTo(final String deviceId, final String name, final BaseCallBack callBack) {
        fixedThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                callTo(deviceId, name,  callBack);
            }
        });
    }

    @Override
    public synchronized void callTo(String deviceId, String name,  BaseCallBack callBack) {
        checkNotNull(deviceId);
        checkNotNull(callBack);
        if (mStatus == Status.LEISURE) {
            showLog("id = "+ CacheRepository.getInstance().getDeviceId());
            showLog("call to ="+deviceId);
            setTalkWithName(name);
            if(mListener != null){
                mListener.showName(name);
            }
            mTalkWith = deviceId;
            setStatus(Status.CALLING);
            checkUdpConnector();

            //TODO 传输自己的名字
            User user = CacheRepository.getInstance().who();
            name = deviceId;
            if(user != null){
                name = user.getName();
            }
            String msg = MessageManager.callTo(deviceId, name);
            SendMessageBroadcast.getInstance().sendMessage(msg);
        } else {
            showLog("错误状态, 当前" + mStatus);
            callBack.isBusy();
        }
    }

    @Override
    public void onHangUp(BaseCallBack callBack) {
        checkNotNull(callBack);
        stop();
        SendMessageBroadcast.getInstance().sendMessage(MessageManager.onHangUp(mTalkWith));
    }

    @Override
    public void onPickUp(BaseCallBack callBack) {
        checkNotNull(callBack);
        canTalk();
        SendMessageBroadcast.getInstance().sendMessage(MessageManager.onPickUp(mTalkWith));
    }


    @Override
    public void onNetworkChange() {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        NetServerManager netServerManager = NetServerManager.getInstance();
        ConnectedByUDP connectedByUDP = null;
        if(cacheRepository.isP2PConnectSuccess()) {
            connectedByUDP = netServerManager.getUDPConnectorById(mTalkWith);
            if(connectedByUDP != null && connectedByUDP.isConnected()){
               if(mListener != null){
                   mListener.showAddress(connectedByUDP.getAddress().getStringRemoteAddress());
               }
            }else{
                if(mListener != null) {
                    mListener.showAddress(cacheRepository.getServerIp() + ":" + cacheRepository.getServerUdpPort());
                }
            }
        } else{
            if(mListener != null) {
                mListener.showAddress(cacheRepository.getServerIp() + ":" + cacheRepository.getServerUdpPort());
            }
        }
    }

    @Override
    public void canTalk() {
        stopRing();
        mPlayTime = System.currentTimeMillis();
        if (recorder == null) {
            synchronized (TelePhone.class) {
                if (recorder == null) {
                    recorder = new SpeexTalkRecorder(TelePhone.this); // 创建录音对象
                    recorder.start(); // 开始录音
                    setStatus(Status.BUSY);
                }
            }
        }
        if (player == null) {
            synchronized (TelePhone.class) {
                if (player == null) {
                    player = new SpeexTalkPlayer();         // 创建播放器对象
                    player.setOnPlayerListener(this);
                    setStatus(Status.BUSY);
                }
            }
        }
       showLog("开始通话");
    }

    public DeviceModel getTalkWithDevice() {
        return mTalkWithDevice;
    }

    public void setTalkWithDevice(DeviceModel mTalkWithDevice) {
        this.mTalkWithDevice = mTalkWithDevice;
    }

    public void stop() {
        synchronized (TelePhone.class) {
            stopRing();
            if (mStatus == Status.BUSY) {
                if (recorder != null) {
                    recorder.stop();    // 停止录音
                }
                if (player != null) {
                    player.stop();      // 停止播放
                }
                recorder = null;
                player = null;
            }
            setStatus(Status.LEISURE);
            mDelayTime = 0;
            mDiffTime = 0;
            showLog("停止通话");

            setTalkWithDevice(null);
            CacheRepository.getInstance().setP2PConnectSuccess(false);
            if(mListener != null){
                mListener.onStop();
            }
            ConnectedByUDP connector = NetServerManager.getInstance().getUDPConnectorById(mTalkWith);
            if(connector != null){
                connector.disconnect();
            }

            clearLogs();
        }
    }


    public void showLog(String text) {
        if (mListener != null) {
            mListener.showLog(text);
        }
        LogBean logBean = new LogBean(text);
        logs.add(logBean);
    }
    public void clearLogs(){
        logs.clear();
    }

    @Override
    public void showTip(String text) {
        if (mListener != null) {
            mListener.showTip(text);
        }
    }

    public interface OnTelePhoneListener {

        void showTip(String text);

        void showLog(String text);

        void showName(String name);

        void showDelay(long delay);

        void showAddress(String address);

        void exceptionCaught(Throwable cause);

        void onChange(int status);

        void onStop();
    }

    public ArrayList<LogBean> getLogs() {
        return logs;
    }

    public void setLogs(ArrayList<LogBean> logs) {
        this.logs = logs;
    }


    public class LogBean{
        long time;
        String log;

        public LogBean(){
            time = System.currentTimeMillis();

        }
        public LogBean(String text){
            time = System.currentTimeMillis();
            this.log = text;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getLog() {
            return log;
        }

        public void setLog(String log) {
            this.log = log;
        }
    }

    @Override
    public boolean isLeisure() {
        return getStatus() == Status.LEISURE;
    }

    @Override
    public boolean isCalling() {
        return getStatus() == Status.CALLING;
    }

    @Override
    public boolean beCalled() {
        return getStatus() == Status.CALLED;
    }

    @Override
    public boolean isBusy() {
        return getStatus() == Status.BUSY;
    }

    public class Status {
        public static final int LEISURE = 0;
        public static final int CALLING = 1;
        public static final int CALLED = 2;
        public static final int BUSY = 3;
        public static final int ERROR = 5;
    }
}
