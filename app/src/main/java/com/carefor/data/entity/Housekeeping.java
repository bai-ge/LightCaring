package com.carefor.data.entity;

import android.util.Log;

import com.carefor.data.source.remote.Parm;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by baige on 2018/3/12.
 */

public class Housekeeping {
    private int id;
    private String servantName;
    private String phone;
    private String serviceTitle;

    //TODO Base64编码
    private String servantImg;
    private String contentText;
    private String serviceWay;
    private String serviceArea;
    private long createTime;
    private String price;

    public String getServantName() {
        return servantName;
    }

    public void setServantName(String servantName) {
        this.servantName = servantName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public void setServiceTitle(String serviceTitle) {
        this.serviceTitle = serviceTitle;
    }

    public String getServantImg() {
        return servantImg;
    }

    public void setServantImg(String servantImg) {
        this.servantImg = servantImg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getServiceWay() {
        return serviceWay;
    }

    public void setServiceWay(String serviceWay) {
        this.serviceWay = serviceWay;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("{");
        stringBuffer.append("id="+id);
        stringBuffer.append(", name="+servantName);
        stringBuffer.append(", phone="+phone);
        stringBuffer.append(", title="+serviceTitle);
        stringBuffer.append(", contentText="+contentText);
        stringBuffer.append(", createTime="+createTime);
        stringBuffer.append(", way="+serviceWay);
        stringBuffer.append(", area="+serviceArea);
        stringBuffer.append(", price="+price);
        stringBuffer.append("}");
        return stringBuffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public static Housekeeping createByJson(JSONObject json) {
        Housekeeping housekeeping = new Housekeeping();
        boolean invalid = true;
        try{
            if(json.has(Parm.ID)){
                housekeeping.setId(json.getInt(Parm.ID));
                invalid = false;
            }
            if(json.has(Parm.SERVANT_NAME)){
                housekeeping.setServantName(json.getString(Parm.SERVANT_NAME));
                invalid = false;
            }
            if(json.has(Parm.SERVANT_IMG)){
                housekeeping.setServantImg(json.getString(Parm.SERVANT_IMG));
                invalid = false;
            }
            if(json.has(Parm.PHONE)){
                housekeeping.setPhone(json.getString(Parm.PHONE));
                invalid = false;
            }
            if(json.has(Parm.SERVICE_PRICE)){
                housekeeping.setPrice(json.getString(Parm.SERVICE_PRICE));
                invalid = false;
            }
            if(json.has(Parm.SERVICE_WAY)){
                housekeeping.setServiceWay(json.getString(Parm.SERVICE_WAY));
                invalid = false;
            }
            if(json.has(Parm.SERVICE_RANGE)){
                housekeeping.setServiceArea(json.getString(Parm.SERVICE_RANGE));
                invalid = false;
            }
            if(json.has(Parm.SERVICE_TITLE)){
                housekeeping.setServiceTitle(json.getString(Parm.SERVICE_TITLE));
                invalid = false;
            }
            if(json.has(Parm.SERVICE_CONTENT)){
                housekeeping.setContentText(json.getString(Parm.SERVICE_CONTENT));
                invalid = false;
            }
            if(json.has(Parm.CREATE_TIME)){
                housekeeping.setCreateTime(json.getLong(Parm.CREATE_TIME));
                invalid = false;
            }
            if(!invalid){
                return housekeeping;
            }
        }catch (JSONException e){
            Log.d("jsonParam",e.getMessage()+"解析失败"+housekeeping.toString());
        }
        return null;
    }

}
