package com.carefor.data.entity;

import android.support.annotation.NonNull;

import com.baidu.mapapi.model.LatLng;
import com.carefor.data.source.remote.Parm;
import com.carefor.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/2/18.
 */

public class Location implements Comparable<Location>{

    String name;
    int uid;
    String deviceId;
    float batteryPercent ; //0 ~ 1.0
    float accuracy; //位置的精度，可作为信号的参考依据 (大于0 数值越大，说明精度越差)

    LatLng latLng;
    long time;
    String area; //大致的地区
    String description; //详细描述






    public Location(){
        time = System.currentTimeMillis();
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getBatteryPercent() {
        return batteryPercent;
    }

    public void setBatteryPercent(float batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static Location createByJson(JSONObject json){
        Location loc = new Location();
        boolean invalid = true;
        long time = System.currentTimeMillis();
        try{
            if(json.has(Parm.TIME)){
                time = Long.valueOf(json.getString(Parm.TIME));
            }
            loc.setTime(time);

            if(json.has(Parm.UID)){
                loc.setUid(json.getInt(Parm.UID));
            }
            if(json.has(Parm.USER_NAME)){
                loc.setName(json.getString(Parm.USER_NAME));
            }
            if(json.has(Parm.DEVICE_ID)){
                loc.setDeviceId(Parm.DEVICE_ID);
            }


            if(json.has(Parm.POSITION) && !Tools.isEmpty(json.getString(Parm.POSITION))){
                String content = json.getString(Parm.POSITION);
                JSONObject jsonPos = new JSONObject(content);
                if(jsonPos.has(Parm.JWD)){
                    String jwd = jsonPos.getString(Parm.JWD);
                    String [] list = jwd.split(",");
                    double lng = Double.valueOf(list[0]);
                    double  lat = Double.valueOf(list[1]);
                    loc.setLatLng(new LatLng(lat, lng));
                    invalid = false;
                }
                if(jsonPos.has(Parm.LNG) && jsonPos.has(Parm.LAT)){
                    LatLng latLng = new LatLng(jsonPos.getDouble(Parm.LAT), jsonPos.getDouble(Parm.LNG));
                    loc.setLatLng(latLng);
                    invalid = false;
                }
                if(jsonPos.has(Parm.LOCATION)){
                    loc.setArea(jsonPos.getString(Parm.LOCATION));
                    invalid = false;
                }
                if(jsonPos.has(Parm.TITLE)){
                    loc.setDescription(jsonPos.getString(Parm.TITLE));
                    invalid = false;
                }

                if(jsonPos.has(Parm.TIME)){
                    loc.setTime(Long.valueOf(jsonPos.getString(Parm.TIME)));
                    invalid = false;
                }
                if(jsonPos.has(Parm.ACCURACY)){
                    loc.setAccuracy((float) jsonPos.getDouble(Parm.ACCURACY));
                    invalid = false;
                }
                if(jsonPos.has(Parm.BATTERY_PERCENT)){
                    loc.setBatteryPercent((float) jsonPos.getDouble(Parm.BATTERY_PERCENT));
                    invalid = false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!invalid){
            return loc;
        }
        return null;
    }


    @Override
    public int compareTo(@NonNull Location loc) {
        checkNotNull(loc);
        return (int) (this.getTime() - loc.getTime());
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("name ="+name+"\n");
        buffer.append("time :"+time+"\n");
        buffer.append("latlng :"+latLng+"\n");
        buffer.append("description :"+description);
        buffer.append("}\n");
        return buffer.toString();
    }
}
