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
    LatLng latLng;
    long time;
    String description;

    public Location(){
        time = System.currentTimeMillis();
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
                StringBuffer des = new StringBuffer();
                if(jsonPos.has(Parm.LOCATION)){
                    des.append(jsonPos.get(Parm.LOCATION)+"\n");
                }
                if(jsonPos.has(Parm.TITLE)){
                    des.append(jsonPos.get(Parm.TITLE));
                }
                if(!Tools.isEmpty(des.toString())){
                    loc.setDescription(des.toString());
                    invalid = false;
                }
                if(jsonPos.has(Parm.TIME)){
                    loc.setTime(Long.valueOf(jsonPos.getString(Parm.TIME)));
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
        buffer.append("time :"+time+"\n");
        buffer.append("latlng :"+latLng+"\n");
        buffer.append("description :"+description);
        buffer.append("}\n");
        return buffer.toString();
    }
}
