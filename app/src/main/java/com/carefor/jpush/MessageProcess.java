package com.carefor.jpush;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.carefor.broadcast.SendMessageBroadcast;
import com.carefor.callback.BaseCallBack;
import com.carefor.callback.CallbackManager;
import com.carefor.connect.BaseConnector;
import com.carefor.connect.ConnectedByUDP;
import com.carefor.connect.NetServerManager;
import com.carefor.connect.SocketPacket;
import com.carefor.connect.msg.MessageManager;
import com.carefor.connect.msg.Parm;
import com.carefor.connect.msg.ResponseMessage;
import com.carefor.data.entity.Candidate;
import com.carefor.data.entity.DeviceModel;
import com.carefor.data.source.cache.CacheRepository;
import com.carefor.telephone.PhoneActivity;
import com.carefor.telephone.TelePhone;
import com.carefor.telephone.TelePhoneAPI;
import com.carefor.util.JsonTools;
import com.carefor.util.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MessageProcess {

    private final static String TAG = MessageProcess.class.getCanonicalName();


    /**
     * IPush 或JPush 收到数据（TCP连接）
     * @param context
     * @param json
     */
    public static void receive(Context context, JSONObject json) {
        if (context == null || json == null) {
            return;
        }
        Log.v(TAG, "TCP收到 :" + json);
        if (json.has(Parm.CODE) && json.has(Parm.DATA)) {
            response(context, json); //回复信息
        } else {
            String from = null;
            String to = null;
            String name = "";
            DeviceModel deviceModel;
            try {

                if (json.has(Parm.FROM)) {
                    from = json.getString(Parm.FROM);
                }
                if (json.has(Parm.TO)) {
                    to = json.getString(Parm.TO);
                    if (Tools.isEmpty(to) || !to.equals(CacheRepository.getInstance().getDeviceId())) {
                        return;
                    }
                }
                if (json.has(Parm.DATA_TYPE)) {
                    int type = json.getInt(Parm.DATA_TYPE);
                    switch (type) {
                        case Parm.TYPE_CALL_TO:
                            if (json.has(Parm.USERNAME)) {
                                name = json.getString(Parm.USERNAME);
                            }
                            if (!TelePhone.getInstance().isLeisure()) {
                                ResponseMessage responseMessage = new ResponseMessage();
                                responseMessage.setCode(Parm.CODE_BUSY);
                                responseMessage.setData(json);
                                responseMessage.setFrom(to);
                                responseMessage.setTo(from);
                                SendMessageBroadcast.getInstance().sendMessage(responseMessage.toJson());
                            } else {
                                deviceModel = new DeviceModel();
                                deviceModel.setDeviceId(from);

                                if (json.has(Parm.CANDIDATES)) {
                                    JSONArray jsonArray = json.getJSONArray(Parm.CANDIDATES);
                                    ArrayList<Candidate> candidates = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class, jsonArray.getJSONObject(i));
                                        if (candidate != null) {
                                            candidates.add(candidate);
                                        }
                                    }
                                    if (candidates != null && candidates.size() > 0) {
                                        deviceModel.setCandidates(candidates);
                                        Log.d(TAG, "传输Candidates" + candidates.toString());
                                        NetServerManager.getInstance().tryPTPConnect(candidates, from);
                                    }

                                }

                                TelePhone.getInstance().setTalkWithDevice(deviceModel);
                                TelePhone.getInstance().afxBeCall(from, name, new TelePhoneAPI.BaseCallBackAdapter());
                                //TODO 被呼叫
                                Intent intent = new Intent(context, PhoneActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }

                            break;
                        case Parm.TYPE_REPLY_CALL_TO:

                            if(!Tools.isEmpty(from) && Tools.isEquals(from, TelePhone.getInstance().getTalkWithId())){
                                //对方已经收到
                                deviceModel = new DeviceModel();
                                deviceModel.setDeviceId(from);

                                if (json.has(Parm.CANDIDATES)) {
                                    JSONArray jsonArray = json.getJSONArray(Parm.CANDIDATES);
                                    ArrayList<Candidate> candidates = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class, jsonArray.getJSONObject(i));
                                        if (candidate != null) {
                                            candidates.add(candidate);
                                        }
                                    }
                                    if (candidates != null && candidates.size() > 0) {
                                        deviceModel.setCandidates(candidates);
                                        Log.d(TAG, "传输Candidates" + candidates.toString());
                                        NetServerManager.getInstance().tryPTPConnect(candidates, from);
                                    }
                                }

                                TelePhone.getInstance().setTalkWithDevice(deviceModel);
                                JSONObject jsonMsg = MessageManager.sendCandidateTo(from);
                                if (jsonMsg != null) {
                                    SendMessageBroadcast.getInstance().sendMessage(jsonMsg.toString());
                                }
                                if (TelePhone.getInstance().isCalling()) {
                                    TelePhone.getInstance().connectSuccess();
                                }
                            }
                            break;
                        case Parm.TYPE_TRY_PTP:
                            if (json.has(Parm.CANDIDATES)) {
                                JSONArray jsonArray = json.getJSONArray(Parm.CANDIDATES);
                                ArrayList<Candidate> candidates = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class, jsonArray.getJSONObject(i));
                                    if (candidate != null) {
                                        candidates.add(candidate);
                                    }
                                }
                                if (candidates != null && candidates.size() > 0) {
                                    if (TelePhone.getInstance().getTalkWithDevice() != null) {
                                        TelePhone.getInstance().getTalkWithDevice().setCandidates(candidates);
                                        Log.d(TAG, "传输Candidates" + candidates.toString());
                                    }
                                    NetServerManager.getInstance().tryPTPConnect(candidates, from);
                                }
                            }


                            //回复自己的Candidate
                            ResponseMessage responseMessage = new ResponseMessage();
                            responseMessage.setFrom(to);
                            responseMessage.setTo(from);
                            responseMessage.setCode(Parm.CODE_SUCCESS);
                            responseMessage.setData(MessageManager.sendCandidateTo(from));
                            SendMessageBroadcast.getInstance().sendMessage(responseMessage.toJson());
                            Log.v(TAG, "发送PTP:" + responseMessage.toJson());

                            break;
                        case Parm.TYPE_PICK_UP:
                            if(from.equals(TelePhone.getInstance().getTalkWithId())){
                                TelePhone.getInstance().canTalk();
                            }
                            break;
                        case Parm.TYPE_HANG_UP:
                            if(from.equals(TelePhone.getInstance().getTalkWithId())){
                                TelePhone.getInstance().stop();
                            }
                            break;
                        default:
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**IPush 或JPush 收到数据（TCP连接）
     * @param context
     * @param json
     */
    public static void response(Context context, JSONObject json) {
        if (context == null || json == null) {
            return;
        }
        try {
            String from = null;
            String to = null;

            if (json.has(Parm.FROM)) {
                from = json.getString(Parm.FROM);
            }
            if (json.has(Parm.TO)) {
                to = json.getString(Parm.TO);
            }
            if (to != null && !to.equals(CacheRepository.getInstance().getDeviceId())) {
                return;
            }
            int code = json.getInt(Parm.CODE);

            if (json.has(Parm.DATA)) {
                JSONObject dataJson = json.getJSONObject(Parm.DATA);
                if (dataJson.has(Parm.DATA_TYPE)) {
                    int type = dataJson.getInt(Parm.DATA_TYPE);
                    if (dataJson.has(Parm.FROM)) {
                        from = dataJson.getString(Parm.FROM);
                    }
                    if (dataJson.has(Parm.TO)) {
                        to = dataJson.getString(Parm.TO);
                    }
                    switch (type) {
                        case Parm.TYPE_TRY_PTP:
                            from = dataJson.getString(Parm.FROM);
                            to = dataJson.getString(Parm.TO);
                            if (!Tools.isEmpty(to) && to.equals(CacheRepository.getInstance().getDeviceId())) {

                                if (dataJson.has(Parm.CANDIDATES)) {
                                    JSONArray jsonArray = dataJson.getJSONArray(Parm.CANDIDATES);
                                    ArrayList<Candidate> candidates = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class, jsonArray.getJSONObject(i));
                                        if (candidate != null) {
                                            candidates.add(candidate);
                                        }
                                    }
                                    if (candidates != null && candidates.size() > 0) {
                                        if (TelePhone.getInstance().getTalkWithDevice() != null) {
                                            TelePhone.getInstance().getTalkWithDevice().setCandidates(candidates);
                                            Log.d(TAG, "传输Candidates" + candidates.toString());
                                        }
                                        NetServerManager.getInstance().tryPTPConnect(candidates, from);
                                    }
                                    //TODO 尝试建立P2P连接
                                }
                            }
                            break;
                        case Parm.TYPE_CALL_TO:
                            if (code == Parm.CODE_NOT_FIND) {
                                //服务器转发失败

                            } else if (code == Parm.CODE_BUSY) {
                                //对方正在通话中
                                if (TelePhone.getInstance().isCalling()) {
                                    TelePhone.getInstance().oppBusy();
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
                if (dataJson.has(Parm.CALLBACK)) {
                    BaseCallBack callBack = CallbackManager.getInstance().get(dataJson.getString(Parm.CALLBACK));
                    callBack.receiveMessage(json.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**一般是UDP收到数据
     * @param connector
     * @param packet
     */
    //UDP数据 解析头部和内容
    public static void receive(BaseConnector connector, SocketPacket packet) {

        if (connector == null || packet == null) {
            return;
        }
        if (packet.isHeartBeat() || packet.isDisconnected()) {
            return;
        }

        String to = null;
        String from = null;
        if (packet.getHeaderBuf() != null) {
            String msg = Tools.dataToString(packet.getHeaderBuf(), Tools.DEFAULT_ENCODE);
            if (!Tools.isEmpty(msg)) {
                try {
                    JSONObject json = new JSONObject(msg);
                    if (json.has(Parm.DATA_TYPE)) {
                        int type = json.getInt(Parm.DATA_TYPE);
                        if (json.has(Parm.FROM)) {
                            from = json.getString(Parm.FROM);
                        }
                        if (json.has(Parm.TO)) {
                            to = json.getString(Parm.TO);
                        }
                        switch (type) {
                            case Parm.TYPE_VOICE:
                                long sendTime = 0;
                                int delayTime;
                                long diffTime = 0;
                                if (json.has(Parm.SEND_TIME)) {
                                    sendTime = json.getLong(Parm.SEND_TIME);
                                    diffTime = System.currentTimeMillis() - sendTime;
                                    TelePhone.getInstance().setDiffTime(diffTime);
                                }
                                if (json.has(Parm.DELAY_TIME)) {
                                    delayTime = json.getInt(Parm.DELAY_TIME);
                                    if (sendTime > 0 && delayTime != 0) {
                                        TelePhone.getInstance().setDelayTime((long) ((diffTime + delayTime) * 1.0 / 2));
                                    }
                                }
                                if (packet.getContentBuf() != null && packet.getContentBuf().length > 0) {
                                    TelePhone.getInstance().play(packet.getContentBuf());
                                }
                                break;
                            case Parm.TYPE_FILE:
                                break;
                            default:
                                Log.d(TAG, "未处理UDP 数据" + json);
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        } else if (packet.getContentBuf() != null) {
            String msg = Tools.dataToString(packet.getContentBuf(), Tools.DEFAULT_ENCODE);
            if (!Tools.isEmpty(msg)) {
                try {
                    JSONObject json = new JSONObject(msg);
                    receive(connector, json);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    /**一般是UDP收到数据
     * @param connector
     * @param json
     */
    //UDP数据
    public static void receive(BaseConnector connector, JSONObject json) {
        if (connector == null || json == null) {
            return;
        }
        String to = null;
        String from = null;
        String name = "";
        DeviceModel deviceModel = null;
        ResponseMessage responseMessage = null;

        if (json.has(Parm.CODE) && json.has(Parm.DATA)) {
            response(connector, json); //回复信息
        } else {
            try {
                if (json.has(Parm.FROM)) {
                    from = json.getString(Parm.FROM);
                }
                if (json.has(Parm.TO)) {
                    to = json.getString(Parm.TO);
                    if (Tools.isEmpty(to) || !to.equals(CacheRepository.getInstance().getDeviceId())) {
                        return;
                    }
                }
                if (json.has(Parm.DATA_TYPE)) {
                    int type = json.getInt(Parm.DATA_TYPE);
                    switch (type) {
                        case Parm.TYPE_UDP_TEST:
                            responseMessage = new ResponseMessage();
                            responseMessage.setCode(Parm.CODE_SUCCESS);
                            JSONObject dataJson = new JSONObject();
                            if (json.has(Parm.CALLBACK)) {
                                dataJson.put(Parm.CALLBACK, json.getString(Parm.CALLBACK));
                            }
                            if (json.has(Parm.LOCAL_IP)) {
                                dataJson.put(Parm.LOCAL_IP, json.getString(Parm.LOCAL_IP));
                            }
                            if (json.has(Parm.LOCAL_UDP_PORT)) {
                                dataJson.put(Parm.LOCAL_UDP_PORT, json.getString(Parm.LOCAL_UDP_PORT));
                            }
                            if (json.has(Parm.SEND_TIME)) {
                                dataJson.put(Parm.SEND_TIME, json.getString(Parm.SEND_TIME));
                            }

                            dataJson.put(Parm.DATA_TYPE, Parm.TYPE_UDP_TEST);
                            dataJson.put(Parm.REMOTE_IP, connector.getAddress().getRemoteIP());
                            dataJson.put(Parm.REMOTE_UDP_PORT, connector.getAddress().getRemotePort());
                            dataJson.put(Parm.FROM, CacheRepository.getInstance().getDeviceId());
                            dataJson.put(Parm.TO, from);
                            responseMessage.setData(dataJson);
                            connector.sendString(responseMessage.toJson());
                            //TODO 可能是P2P的测试连接，收到该数据表示成功
                            deviceModel = NetServerManager.getInstance().getDeviceModelById(from);
                            if (deviceModel != null && connector instanceof ConnectedByUDP) {
                                deviceModel.setConnectedByUDP((ConnectedByUDP) connector);
                                if (json.has(Parm.LOCAL_IP)) {
                                    deviceModel.setLocalIp(json.getString(Parm.LOCAL_IP));
                                }
                                if (json.has(Parm.LOCAL_UDP_PORT)) {
                                    deviceModel.setLocalUdpPort(json.getInt(Parm.LOCAL_UDP_PORT));
                                }
                                deviceModel.setRemoteUdpPort(connector.getAddress().getRemotePortIntegerValue());
                                deviceModel.setRemoteIp(connector.getAddress().getRemoteIP());

                            }
                            break;
                        case Parm.TYPE_CALL_TO:

                            if (json.has(Parm.USERNAME)) {
                                name = json.getString(Parm.USERNAME);
                            }

                            if (!TelePhone.getInstance().isLeisure()) {
                                responseMessage = new ResponseMessage();
                                responseMessage.setCode(Parm.CODE_BUSY);
                                responseMessage.setData(json);
                                responseMessage.setFrom(to);
                                responseMessage.setTo(from);
                                SendMessageBroadcast.getInstance().sendMessage(responseMessage.toJson());
                            } else {
                                deviceModel = new DeviceModel();
                                deviceModel.setDeviceId(from);

                                if (json.has(Parm.CANDIDATES)) {
                                    JSONArray jsonArray = json.getJSONArray(Parm.CANDIDATES);
                                    ArrayList<Candidate> candidates = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class, jsonArray.getJSONObject(i));
                                        if (candidate != null) {
                                            candidates.add(candidate);
                                        }
                                    }
                                    if (candidates != null && candidates.size() > 0) {
                                        deviceModel.setCandidates(candidates);
                                        Log.d(TAG, "传输Candidates" + candidates.toString());
                                        NetServerManager.getInstance().tryPTPConnect(candidates, from);
                                    }

                                }


                                TelePhone.getInstance().setTalkWithDevice(deviceModel);

                                TelePhone.getInstance().afxBeCall(from, name, new TelePhoneAPI.BaseCallBackAdapter());
                                //TODO 被呼叫
                                TelePhone.getInstance().startActivity(PhoneActivity.class);
                            }

                            break;
                        case Parm.TYPE_REPLY_CALL_TO:
                            //TODO 对方已经收到
                            to = json.getString(Parm.TO);
                            from = json.getString(Parm.FROM);
                            if (!Tools.isEmpty(to) && to.equals(CacheRepository.getInstance().getDeviceId())
                                    && !Tools.isEmpty(from) && from.equals(TelePhone.getInstance().getTalkWithId())) {
                                //TODO 被呼叫
                                deviceModel = new DeviceModel();
                                deviceModel.setDeviceId(from);

                                if (json.has(Parm.CANDIDATES)) {
                                    JSONArray jsonArray = json.getJSONArray(Parm.CANDIDATES);
                                    ArrayList<Candidate> candidates = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class, jsonArray.getJSONObject(i));
                                        if (candidate != null) {
                                            candidates.add(candidate);
                                        }
                                    }
                                    if (candidates != null && candidates.size() > 0) {
                                        deviceModel.setCandidates(candidates);
                                        Log.d(TAG, "传输Candidates" + candidates.toString());
                                        NetServerManager.getInstance().tryPTPConnect(candidates, from);
                                    }
                                }
                                TelePhone.getInstance().setTalkWithDevice(deviceModel);

                                //TODO 分析Candidates
                                JSONObject jsonMsg = MessageManager.sendCandidateTo(from);
                                if (jsonMsg != null) {
                                    connector.sendString(jsonMsg.toString());
                                }
                                if (TelePhone.getInstance().isCalling()) {
                                    TelePhone.getInstance().connectSuccess();
                                }

                            }
                            break;
                        case Parm.TYPE_TRY_PTP:
                            from = json.getString(Parm.FROM);
                            to = json.getString(Parm.TO);
                            if (!Tools.isEmpty(to) && to.equals(CacheRepository.getInstance().getDeviceId())) {

                                if (json.has(Parm.CANDIDATES)) {
                                    JSONArray jsonArray = json.getJSONArray(Parm.CANDIDATES);
                                    ArrayList<Candidate> candidates = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class, jsonArray.getJSONObject(i));
                                        if (candidate != null) {
                                            candidates.add(candidate);
                                        }
                                    }
                                    if (candidates != null && candidates.size() > 0) {
                                        if (TelePhone.getInstance().getTalkWithDevice() != null) {
                                            TelePhone.getInstance().getTalkWithDevice().setCandidates(candidates);
                                            Log.d(TAG, "传输Candidates" + candidates.toString());
                                        }
                                        NetServerManager.getInstance().tryPTPConnect(candidates, from);
                                    }
                                }


                                //回复自己的Candidate
                                responseMessage = new ResponseMessage();
                                responseMessage.setFrom(to);
                                responseMessage.setTo(from);
                                responseMessage.setCode(Parm.CODE_SUCCESS);
                                responseMessage.setData(MessageManager.sendCandidateTo(from));
                                connector.sendString(responseMessage.toJson());
                                Log.v(TAG, "发送PTP:" + responseMessage.toJson());
                                //TODO 尝试建立P2P连接
                            }

                            break;
                        case Parm.TYPE_PICK_UP:
                            if(from.equals(TelePhone.getInstance().getTalkWithId())){
                                TelePhone.getInstance().canTalk();
                            }
                            break;
                        case Parm.TYPE_HANG_UP:
                            if(from.equals(TelePhone.getInstance().getTalkWithId())){
                                TelePhone.getInstance().stop();
                            }
                            break;
                        case Parm.TYPE_TRY_PTP_CONNECT:
                            from = json.getString(Parm.FROM);
                            to = json.getString(Parm.TO);
                            if (to.equals(CacheRepository.getInstance().getDeviceId())) {
                                deviceModel = NetServerManager.getInstance().getDeviceModelById(from);
                                if (deviceModel == null) {
                                    deviceModel = new DeviceModel();
                                    deviceModel.setDeviceId(from);
                                    NetServerManager.getInstance().put(from, deviceModel);
                                }
                                if (connector instanceof ConnectedByUDP) {
                                    deviceModel.setConnectedByUDP((ConnectedByUDP) connector);
                                }
                                responseMessage = new ResponseMessage();
                                responseMessage.setCode(Parm.CODE_SUCCESS);
                                responseMessage.setData(MessageManager.tryPTPConnect(to, from));
                                connector.sendString(responseMessage.toJson());
                                Log.d(TAG, "已经建立P2P 连接：id =" + from + ", connetor =" + connector.getAddress().getStringRemoteAddress());
                                TelePhone.getInstance().showLog("P2P 连接成功 " + connector.getAddress().getStringRemoteAddress());
                                CacheRepository.getInstance().setP2PConnectSuccess(true);
                            }
                            break;
                        default:
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /** 一般是UDP收到数据
     * @param connector
     * @param json
     */
    // 收到回复
    public static void response(BaseConnector connector, JSONObject json) {
        if (connector == null || json == null) {
            return;
        }

        try {
            JSONObject dataJson = json.getJSONObject(Parm.DATA);
            if (dataJson.has(Parm.DATA_TYPE)) {
                int type = dataJson.getInt(Parm.DATA_TYPE);
                String to = null;
                String from = null;
                DeviceModel deviceModel = null;
                switch (type) {
                    case Parm.TYPE_UDP_TEST:
                        Candidate candidate = new Candidate();
                        candidate.setTime(System.currentTimeMillis());
                        if (dataJson.has(Parm.FROM)) {
                            candidate.setFrom(dataJson.getString(Parm.FROM));
                        }
                        if (dataJson.has(Parm.REMOTE_IP)) {
                            candidate.setRemoteIp(dataJson.getString(Parm.REMOTE_IP));
                        }
                        if (dataJson.has(Parm.REMOTE_UDP_PORT)) {
                            candidate.setRemotePort(dataJson.getString(Parm.REMOTE_UDP_PORT));
                        }
                        if (dataJson.has(Parm.LOCAL_IP)) {
                            candidate.setLocalIp(dataJson.getString(Parm.LOCAL_IP));
                        }
                        if (dataJson.has(Parm.LOCAL_UDP_PORT)) {
                            candidate.setLocalPort(dataJson.getString(Parm.LOCAL_UDP_PORT));
                        }
                        if (dataJson.has(Parm.SEND_TIME)) {
                            long time = dataJson.getLong(Parm.SEND_TIME);
                            candidate.setDelayTime(System.currentTimeMillis() - time);
                        }
                        candidate.setRelayIp(connector.getAddress().getRemoteIP());
                        candidate.setRelayPort(connector.getAddress().getRemotePort());
                        CacheRepository.getInstance().add(candidate);
                        if (dataJson.has(Parm.CALLBACK)) {
                            BaseCallBack callBack = CallbackManager.getInstance().get(dataJson.getString(Parm.CALLBACK));
                            callBack.loadCandidate(candidate);
                        }
                        break;
                    case Parm.TYPE_TRY_PTP_CONNECT:
                        from = dataJson.getString(Parm.FROM);
                        to = dataJson.getString(Parm.TO);
                        if (to.equals(CacheRepository.getInstance().getDeviceId())) {
                            deviceModel = NetServerManager.getInstance().getDeviceModelById(from);
                            if (deviceModel == null) {
                                deviceModel = new DeviceModel();
                                deviceModel.setDeviceId(from);
                                NetServerManager.getInstance().put(from, deviceModel);
                            }
                            if (connector instanceof ConnectedByUDP) {
                                deviceModel.setConnectedByUDP((ConnectedByUDP) connector);
                            }
                        }
                        Log.d(TAG, "已经建立P2P 连接：id =" + from + ", connetor =" + connector.getAddress().getStringRemoteAddress());
                        TelePhone.getInstance().showLog("P2P 连接成功 " + connector.getAddress().getStringRemoteAddress());
                        CacheRepository.getInstance().setP2PConnectSuccess(true);
                    default:
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
