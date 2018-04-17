package com.carefor.connect.msg;

import android.util.Log;


import com.carefor.connect.SocketPacket;
import com.carefor.data.entity.Candidate;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.util.JsonTools;
import com.carefor.util.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

//            Base64 编码
//            无论是编码还是解码都会有一个参数Flags，Android提供了以下几种
//
//            DEFAULT 这个参数是默认，使用默认的方法来加密
//
//            NO_PADDING 这个参数是略去加密字符串最后的”=”
//
//            NO_WRAP 这个参数意思是略去所有的换行符（设置后CRLF就没用了）
//
//            CRLF 这个参数看起来比较眼熟，它就是Win风格的换行符，意思就是使用CR LF这一对作为一行的结尾而不是Unix风格的LF
//
//            URL_SAFE 这个参数意思是加密时不使用对URL和文件名有特殊意义的字符来作为加密字符，具体就是以-和_取代+和/

/**
 * Created by baige on 2017/10/24.
 */

public class MessageManager {

    private final static String TAG = MessageManager.class.getName();

    public final static int VERSION = 2;

    public static String login(String deviceId, String localIp, String localPort, String acceptPort, String localUdpPort) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_LOGIN);
            jsonObject.put(Parm.FROM, deviceId);
            jsonObject.put(Parm.LOCAL_IP, localIp);
            jsonObject.put(Parm.LOCAL_PORT, localPort);
//            jsonObject.put(Parm.ACCEPT_PORT, acceptPort);
//            jsonObject.put(Parm.LOCAL_UDP_PORT, localUdpPort);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String loginSuccess(String serverId, String deviceId, String remoteIp, String remotePort, String localUdpPort) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_LOGIN);
            jsonObject.put(Parm.FROM, serverId);
            jsonObject.put(Parm.DEVICE_ID, deviceId);
            jsonObject.put(Parm.REMOTE_IP, remoteIp);
            jsonObject.put(Parm.REMOTE_PORT, remotePort);
            jsonObject.put(Parm.LOCAL_UDP_PORT, localUdpPort);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String udpTest(String deviceId, String callbackId, String localIp, String localUdpPort){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_UDP_TEST);
            jsonObject.put(Parm.FROM, deviceId);
            jsonObject.put(Parm.CALLBACK, callbackId);
            jsonObject.put(Parm.LOCAL_IP, localIp);
            jsonObject.put(Parm.LOCAL_UDP_PORT, localUdpPort);
            jsonObject.put(Parm.SEND_TIME, System.currentTimeMillis());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String udpTest(String deviceId, String localIp, String localUdpPort) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_UDP_TEST);
            jsonObject.put(Parm.FROM, deviceId);
            jsonObject.put(Parm.LOCAL_IP, localIp);
            jsonObject.put(Parm.LOCAL_UDP_PORT, localUdpPort);
            jsonObject.put(Parm.SEND_TIME, System.currentTimeMillis());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject udpTestResponse(String serverId, String deviceId, String remoteIp, String remoteUdpPort) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_UDP_TEST);
            jsonObject.put(Parm.FROM, serverId);
            jsonObject.put(Parm.DEVICE_ID, deviceId);
            jsonObject.put(Parm.REMOTE_IP, remoteIp);
            jsonObject.put(Parm.REMOTE_UDP_PORT, remoteUdpPort);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String logout(String deviceId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_LOGOUT);
            jsonObject.put(Parm.FROM, deviceId);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 注意需要告诉对方自己使用的通话端口
     */
    public static String callTo(String talkWith, String name) {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, cacheRepository.getDeviceId());
            jsonObject.put(Parm.USERNAME, name);
            jsonObject.put(Parm.TO, talkWith);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_CALL_TO);
            ArrayList<Candidate> candidates = cacheRepository.getCandidates();
            if (candidates != null && candidates.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                for (Candidate candidate : candidates){
                    jsonArray.put(JsonTools.getJSON(candidate));
                }
                jsonObject.put(Parm.CANDIDATES, jsonArray);
            }
            Log.d(TAG, "call To MSG:"+jsonObject.toString());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String replyCallTo(String talkWith) {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, cacheRepository.getDeviceId());
            jsonObject.put(Parm.TO, talkWith);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_REPLY_CALL_TO);
            ArrayList<Candidate> candidates = cacheRepository.getCandidates();
            if (candidates != null && candidates.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                for (Candidate candidate : candidates){
                    jsonArray.put(JsonTools.getJSON(candidate));
                }
                jsonObject.put(Parm.CANDIDATES, jsonArray);
            }
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static JSONObject sendCandidateTo(String to){
        CacheRepository cacheRepository = CacheRepository.getInstance();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, cacheRepository.getDeviceId());
            jsonObject.put(Parm.TO, to);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_TRY_PTP);
            ArrayList<Candidate> candidates = cacheRepository.getCandidates();
            if (candidates != null && candidates.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                for (Candidate candidate : candidates){
                    jsonArray.put(JsonTools.getJSON(candidate));
                }
                jsonObject.put(Parm.CANDIDATES, jsonArray);
            }
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject tryPTPConnect(String from, String to){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, from);
            jsonObject.put(Parm.TO, to);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_TRY_PTP_CONNECT);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 注意需要告诉对方自己使用的通话端口
     */
    public static String onPickUp(String talkWith) {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, cacheRepository.getDeviceId());
            jsonObject.put(Parm.TO, talkWith);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_PICK_UP);

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String onHangUp(String talkWith) {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, cacheRepository.getDeviceId());
            jsonObject.put(Parm.TO, talkWith);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_HANG_UP);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SocketPacket voice(String from, String to, byte[] voice, int delayTime) {
        if(voice == null || voice.length == 0){
            return  null;
        }
        SocketPacket socketPacket = new SocketPacket();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, from);
            jsonObject.put(Parm.TO, to);
            jsonObject.put(Parm.SEND_TIME, System.currentTimeMillis());
            jsonObject.put(Parm.DELAY_TIME, delayTime);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_VOICE);
            socketPacket.setHeaderBuf(jsonObject.toString().getBytes());
            socketPacket.setContentBuf(voice);
            return socketPacket;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAllServers() {
//        String msg;
//        CacheRepository cacheRepository = CacheRepository.getInstance();
//        MessageHeader header = new MessageHeader();
//        header.setVersion(VERSION);
//        header.setFrom(cacheRepository.getDeviceId());
//        header.setMethod(MessageHeader.Method.GET);
//        header.setParam(MessageHeader.Param.SERVERS);
//        msg = MessageParser.getJSON(header);
//        return msg;
        return null;
    }

    /*
    * UDP数据结构

                    字节
        版本号：	    1
        类型：	    1
        数据总长度	2
        数据buf		n=数据长度

        数据：
        TAG			1
        lenght		2
        buf			n=lenght

    * */
    public static byte[] udpLogin(String deviceId) {
        checkNotNull(deviceId);
        int dataLen = deviceId.length() + 3;
        ByteBuffer byteBuffer = ByteBuffer.allocate(dataLen + 4);
        // 头部
        byteBuffer.put((byte) VERSION);
        byteBuffer.put((byte) TYPE_LOGIN);
        byteBuffer.putShort((short) (dataLen));

        // TAG
        byteBuffer.put((byte) TAG_DEVICE_ID);
        byteBuffer.putShort((short) deviceId.length());
        byteBuffer.put(deviceId.getBytes());

        return byteBuffer.array();
    }

    public static byte[] udpP2P(String deviceId) {
        checkNotNull(deviceId);
        int dataLen = deviceId.length() + 3;
        ByteBuffer byteBuffer = ByteBuffer.allocate(dataLen + 4);
        // 头部
        byteBuffer.put((byte) VERSION);
        byteBuffer.put((byte) TYPE_P2P);
        byteBuffer.putShort((short) (dataLen));

        // TAG
        byteBuffer.put((byte) TAG_DEVICE_ID);
        byteBuffer.putShort((short) deviceId.length());
        byteBuffer.put(deviceId.getBytes());

        return byteBuffer.array();
    }

    public static byte[] udpData(int type, int tag, byte[] data) {
        checkNotNull(data);
        if (getTypeName(type).isEmpty() || getTagName(tag).isEmpty()) {
            return null;
        }
        int dataLen = data.length + 3;
        ByteBuffer byteBuffer = ByteBuffer.allocate(dataLen + 4);
        // 头部
        byteBuffer.put((byte) VERSION);
        byteBuffer.put((byte) type);
        byteBuffer.putShort((short) (dataLen));

        // TAG
        byteBuffer.put((byte) tag);
        byteBuffer.putShort((short) data.length);
        byteBuffer.put(data);
        return byteBuffer.array();
    }

    public static byte[] udpData(boolean needTime, int type, int tag, byte[] data) {
        checkNotNull(data);
        if (getTypeName(type).isEmpty() || getTagName(tag).isEmpty()) {
            return null;
        }
        long startTime = System.currentTimeMillis();
        int timeLen = 0;
        if (needTime) {
            timeLen = 4;
            type |= 0x80;
        }
        int dataLen = data.length + 3;
        ByteBuffer byteBuffer = ByteBuffer.allocate(timeLen + dataLen + 4);
        // 头部
        byteBuffer.put((byte) VERSION);
        byteBuffer.put((byte) type);
        if (needTime) {
            int now = (int) (System.currentTimeMillis() & 0xFFFFFFFFL);
            byteBuffer.put(Tools.toByte(now));
        }
        byteBuffer.putShort((short) (dataLen));

        // TAG
        byteBuffer.put((byte) tag);
        byteBuffer.putShort((short) data.length);
        byteBuffer.put(data);
        Log.d("Connector", "装包时间" + String.valueOf(System.currentTimeMillis() - startTime));
        return byteBuffer.array();
    }

    public static byte[] udpServerTranf(String deviceId, String desId, int type, int tag, byte[] data) {
        checkNotNull(deviceId);
        checkNotNull(desId);
        checkNotNull(data);

        if (getTypeName(type).isEmpty() || getTagName(tag).isEmpty()) {
            return null;
        }


        int dataLen = deviceId.length() + desId.length() + data.length + 9;
        ByteBuffer byteBuffer = ByteBuffer.allocate(dataLen + 4);
        // 头部
        byteBuffer.put((byte) VERSION);
        byteBuffer.put((byte) type);
        byteBuffer.putShort((short) (dataLen));

        // TAG
        byteBuffer.put((byte) TAG_DEVICE_ID);
        byteBuffer.putShort((short) deviceId.length());
        byteBuffer.put(deviceId.getBytes());

        byteBuffer.put((byte) TAG_DES_ID);
        byteBuffer.putShort((short) desId.length());
        byteBuffer.put(desId.getBytes());

        byteBuffer.put((byte) tag);
        byteBuffer.putShort((short) data.length);
        byteBuffer.put(data);

        return byteBuffer.array();
    }

    public static byte[] udpServerTranf(boolean needTime, String deviceId, String desId, int type, int tag, byte[] data) {
        checkNotNull(deviceId);
        checkNotNull(desId);
        checkNotNull(data);

        if (getTypeName(type).isEmpty() || getTagName(tag).isEmpty()) {
            return null;
        }
        int timeLen = 0;
        if (needTime) {
            timeLen = 4;
            type |= 0x80;
        }
        int dataLen = deviceId.length() + desId.length() + data.length + 9;
        ByteBuffer byteBuffer = ByteBuffer.allocate(timeLen + dataLen + 4);
        // 头部
        byteBuffer.put((byte) VERSION);
        byteBuffer.put((byte) type);
        if (needTime) {
            int now = (int) (System.currentTimeMillis() & 0xFFFFFFFFL);
            byteBuffer.put(Tools.toByte(now));
        }
        byteBuffer.putShort((short) (dataLen));

        // TAG
        byteBuffer.put((byte) TAG_DEVICE_ID);
        byteBuffer.putShort((short) deviceId.length());
        byteBuffer.put(deviceId.getBytes());

        byteBuffer.put((byte) TAG_DES_ID);
        byteBuffer.putShort((short) desId.length());
        byteBuffer.put(desId.getBytes());

        byteBuffer.put((byte) tag);
        byteBuffer.putShort((short) data.length);
        byteBuffer.put(data);

        return byteBuffer.array();
    }

    public static Map<String, Object> messageParser(DatagramPacket packet) {
        byte buf[] = packet.getData();
        Map<String, Object> attr = new LinkedHashMap<>();
        ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
        int version = byteBuffer.get();
        int type;
        int size;
        long startTime = System.currentTimeMillis();
        if (version == MessageManager.VERSION) {
            type = byteBuffer.get();
            attr.put("type", type & 0x7f);
            if ((type & 0x80) > 0) {
                byte[] timeBuf = new byte[4];
                byteBuffer.get(timeBuf);
                long sendTime = Tools.toLong(timeBuf);
                long now = System.currentTimeMillis() & 0xFFFFFFFFL;
                attr.put("delay", now - sendTime);
            }
            size = byteBuffer.getShort();
            if (size < byteBuffer.remaining()) {
                // 解析剩余的TAG
                while (byteBuffer.position() < size + 4) {
                    int tag = byteBuffer.get();
                    int len = byteBuffer.getShort();
                    String name = MessageManager.getTagName(0xff & tag);
                    Log.d(TAG, "name=" + name + ",len=" + len);
                    if (!name.isEmpty()) {
                        byte[] buffer = new byte[len];
                        byteBuffer.get(buffer);
                        attr.put(name, buffer);
                    } else {
                        Log.d(TAG, "不存在该TAG" + tag);
                        return null;
                    }
                }
            } else {
                System.out.println("长度过长，数据混乱" + size);
                return null;
            }
        }
        Log.d("Connector", "解包时间" + String.valueOf(System.currentTimeMillis() - startTime));
        return attr;
    }

    private final static int TAGCODE = 0x00A0;
    public final static int TAG_DEVICE_ID = TAGCODE + 0;
    public final static int TAG_VOICE = TAGCODE + 2;
    public final static int TAG_JSON = TAGCODE + 3;
    public final static int TAG_DES_ID = TAGCODE + 4;
    public final static int TAG_TIME = TAGCODE + 5;

    private final static int MSG_TYPE = 0x0010;
    public final static int TYPE_LOGIN = MSG_TYPE + 1; //登录
    public final static int TYPE_TRANF = MSG_TYPE + 2; //中转
    public final static int TYPE_P2P = MSG_TYPE + 3; //尝试连接的数据
    public final static int TYPE_VOICE = MSG_TYPE + 4; //通话的声音
    public final static int TYPE_CHECK = MSG_TYPE + 5;//通话前检查udp连接

    public static String getTagName(int tag) {
        switch (tag) {
            case TAG_DEVICE_ID:
                return "device_id";
            case TAG_VOICE:
                return "voice";
            case TAG_JSON:
                return "json";
            case TAG_DES_ID:
                return "des_id";
            case TAG_TIME:
                return "time";
            default:
                return "";
        }
    }

    public static String getTypeName(int type) {
        switch (type) {
            case TYPE_CHECK:
                return "check";
            case TYPE_LOGIN:
                return "login";
            case TYPE_TRANF:
                return "tranf";
            case TYPE_P2P:
                return "p2p";
            case TYPE_VOICE:
                return "voice";

            default:
                return "";
        }
    }

}
