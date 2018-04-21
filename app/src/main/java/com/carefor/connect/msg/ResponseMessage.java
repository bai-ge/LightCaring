package com.carefor.connect.msg;

import org.json.JSONException;
import org.json.JSONObject;

import com.carefor.util.Tools;

/**
 * Created by baige on 2018/3/26.
 */

public class ResponseMessage {
    private int code;
    private String to;
    private String from;
    private Object data;
    
    

    public String toJson(){
        JSONObject jsonObject = new JSONObject();
        try {
        	if(!Tools.isEmpty(from)){
        		jsonObject.put(Parm.FROM, from);
        	}
        	if(!Tools.isEmpty(to)){
        		jsonObject.put(Parm.TO, to);
        	}
            jsonObject.put(Parm.CODE, code);
            jsonObject.put(Parm.DATA, data);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    
    
    public String getTo() {
		return to;
	}



	public void setTo(String to) {
		this.to = to;
	}



	public String getFrom() {
		return from;
	}



	public void setFrom(String from) {
		this.from = from;
	}



	public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
