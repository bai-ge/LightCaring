package com.carefor.data.source.remote;

/**
 * Created by baige on 2017/12/22.
 */

public class Parm {
    public static final String USER_NAME = "username";
    public static final String NAME = "name";
    public static final String PASSWORD = "password";
    public static final String CODE = "code";
    public static final String MSG = "msg";
    public static final String DATA = "data";

    public static final String MESSAGE_TYPE = "MessageType";
    public static final String ROUTER = "Router";
    public static final String Callback = "Callback";

    public static final String MEANING = "meanning";
    public static final String OBJ = "object";
    public static final String IMAGE = "image";
    public static final String REGISTER_TIME = "register-time";
    public static final String UID = "u_id";
    public static final String TEL = "tel";
    public static final String TELEPHONE = "telphone";
    public static final String TYPE = "type";
    public static final String DEVICE_ID = "device_id";
    public static final String _DEVICE_ID = "device-id";
    public static final String GUARD_ID = "gid";
    public static final String PUP_ID = "bgid";
    public static final String GRANT = "grant";
    public static final String OTHER_ID = "otherid";
    public static final String INUM = "iNum";
    public static final String DESCRIPTION ="description";
    public static final String SENDU_ID = "sendUid";
    public static final String RECEIVEU_ID = "receiveUid";
    public static final String CONTENT = "content";

    public static final String ACCURACY = "accuracy";
    public static final String BATTERY_PERCENT = "battery_percent";
    public static final String SHOW_NOTIFICATION = "show_notification";


    public static final String LOCATION = "Location";
    public static final String JWD = "JWD";
    public static final String TITLE = "title";
    public static final String TIME = "time";
    public static final String POS = "pos";
    public static final String LNG = "lng"; //经度
    public static final String LAT = "lat"; //维度
    public static final String POSITION = "position";
    public static final String USER_INFO_ID = "user_info_id";
    public static final String REMAIN_TIME = "RemainTime";
    public static final String DRUG = "drug";

    //家政服务
    public static final String ID = "id";
    public static final String SERVANT_NAME = "servantName";
    public static final String SERVANT_IMG = "servantImg";
    public static final String PHONE = "phone";
    public static final String SERVICE_PRICE = "servicePrice";
    public static final String SERVICE_NAME = "serviceName";
    public static final String SERVICE_WAY = "serviceWay";
    public static final String SERVICE_RANGE = "serviceRange";
    public static final String SERVICE_TITLE = "serviceTitle";
    public static final String SERVICE_CONTENT = "serviceContent";
    public static final String CREATE_TIME = "createTime";

    public static final String KEYWORD = "keyword";

    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String MESSAGE = "message";

    public static final String DROP_SWITCH = "drop_switch";


    public static final int SUCCESS_CODE = 200;
    public static final int FAIL_CODE = 500;
    public static final int UNKNOWN_CODE = 999;
    public static final int NOTFIND_CODE = 404;
    public static final int TYPE_CONVERT_CODE = 1001;
    public static final int EXIST_CODE = 1002;
    public static final int BLANK_CODE = 1003;
    public static final int TIMEOUT_CODE = 1004;
    public static final int INVALID_CODE = 1005;

    public static final int MSG_TYPE_ASK_LOCATION = 2;
    public static final int MSG_TYPE_LOCATION = 3;

    public static final int MSG_TYPE_CUSTOM = 100;
    public static final int MSG_TYPE_BACK = MSG_TYPE_CUSTOM + 1;
    public static final int MSG_TYPE_DROP_ASK = MSG_TYPE_CUSTOM + 2; //询问是否打开摔倒检测
    public static final int MSG_TYPE_DROP_SWITCH = MSG_TYPE_CUSTOM + 3; //返回结果
    public static final int MSG_TYPE_DROP_MESSAGE = MSG_TYPE_CUSTOM + 4; //摔倒信息(包括时间，地点，人)

}
