package com.carefor.data.entity;

import android.util.Log;

import com.carefor.data.source.remote.Parm;

import org.json.JSONException;
import org.json.JSONObject;



/*
 * Created by baige on 2017/12/22.
 */

public class User{

    private int uid; //远程服务器主键
    private String name;
    private String alias;
    private String psw;
    private String tel;
    private String imgPath;
    private int type;   //监护人或被监护人
    private long registerTime;

    private String deviceId;

    public User(){

    }
    public User(String name, String psw){
        this.name = name;
        this.psw = psw;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPsw() {
        return psw;
    }

    public void setPsw(String psw) {
        this.psw = psw;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    @Override
    public String toString() {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("{");
        stringBuffer.append("uid="+uid);
        stringBuffer.append(", name="+name);
        stringBuffer.append(", psw="+psw);
        stringBuffer.append(", type="+type);
        stringBuffer.append(", tel="+tel);
        stringBuffer.append(", deviceId="+deviceId);
        stringBuffer.append("}");
        return stringBuffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        User user = (User)obj;
        if(user == null || name == null || user.getName() == null){
            return false;
        }
        return user.getName().equals(name);
    }

    public static User createByJson(JSONObject json) {
        User user = new User();
        boolean invalid = true;
        try{
            if(json.has(Parm.UID)){
                user.setUid(json.getInt(Parm.UID));
                invalid = false;
            }
            if(json.has(Parm.TELEPHONE)){
                user.setTel(json.getString(Parm.TELEPHONE));
                invalid = false;
            }
            if(json.has(Parm.TYPE)){
                user.setType(json.getInt(Parm.TYPE));
                invalid = false;
            }
            if(json.has(Parm.DEVICE_ID)){
                user.setDeviceId(json.getString(Parm.DEVICE_ID));
                invalid = false;
            }
            if(json.has(Parm._DEVICE_ID)){
                user.setDeviceId(json.getString(Parm._DEVICE_ID));
                invalid = false;
            }
            if(json.has(Parm.USER_NAME)){
                user.setName(json.getString(Parm.USER_NAME));
                invalid = false;
            }
            if(json.has(Parm.NAME)){
                user.setName(json.getString(Parm.NAME));
                invalid = false;
            }
            if(json.has(Parm.IMAGE)){
                user.setImgPath(json.getString(Parm.IMAGE));
                invalid = false;
            }
            if(json.has(Parm.REGISTER_TIME)){
                user.setRegisterTime(json.getLong(Parm.REGISTER_TIME));
                invalid = false;
            }
            if(!invalid){
                return user;
            }
        }catch (JSONException e){
            Log.d("jsonParam",e.getMessage()+"解析失败"+user.toString());
        }
        return null;
    }



}
