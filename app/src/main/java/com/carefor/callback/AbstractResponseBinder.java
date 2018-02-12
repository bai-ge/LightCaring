package com.carefor.callback;

import com.carefor.data.source.remote.Parm;

/**
 * Created by baige on 2018/2/13.
 */

public abstract class AbstractResponseBinder {

    public void callBackCode(SeniorCallBack callBack, int code) {
        switch (code) {
            case Parm.SUCCESS_CODE:
                callBack.success();
                break;
            case Parm.FAIL_CODE:
                callBack.fail();
                break;
            case Parm.UNKNOWN_CODE:
                callBack.unknown();
                break;
            case Parm.NOTFIND_CODE:
                callBack.notFind();
                break;
            case Parm.TYPE_CONVERT_CODE:
                callBack.typeConvert();
                break;
            case Parm.EXIST_CODE:
                callBack.exist();
                break;
            case Parm.BLANK_CODE:
                callBack.isBlank();
                break;
            case Parm.TIMEOUT_CODE:
                callBack.timeout();
                break;
            case Parm.INVALID_CODE:
                callBack.invalid();
                break;
            default:
                callBack.unknown();
        }
    }

    public abstract void parse(String json, SeniorCallBack callBack);
}
