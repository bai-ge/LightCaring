package com.carefor.connect;
/*
 * 消息的解析
 * 调用函数parser()解析出消息头部MessageHeader
 * 如果是服务器端，一般解析到此就应该结束了，接着根据头部的内容再转发这条消息，除非此消息是服务器想要的数据
 * 如果是客户端，可解析剩余部分，实现数据的读取
 */


import android.util.Log;

import com.carefor.data.entity.MessageHeader;
import com.carefor.data.entity.Transinformation;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MessageParser {
    private final static String TAG = MessageParser.class.getCanonicalName();

    public final static String JSON_VERSION = "version";
    public final static String JSON_FROM = "from";
    public final static String JSON_METHOD = "method";
    public final static String JSON_PARAM = "param";

    public static Transinformation parser(String json) {
        // JSONTokener tokener = new JSONTokener(json);
        try {
            JSONObject root = new JSONObject(json);
            Transinformation tranfor = new Transinformation();
            MessageHeader header = tranfor.getMessageHeader();

            header.setVersion(root.getInt(JSON_VERSION));
            header.setFrom(root.getString(JSON_FROM));
            header.setMethod(root.getInt(JSON_METHOD));

            if (root.has(JSON_PARAM)) {
                header.setParam(root.getString(JSON_PARAM));
            }
            parserHeader(header);// 解析头部
            // DOTO 解析剩余部分
            Iterator it = root.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                Object value = root.get(key);
                tranfor.getDateMap().put(key, value);
            }
            return tranfor;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private static void parserHeader(MessageHeader header) {
        switch (header.getVersion()) {
            case 1:
                header.parseParam();
                break;
            case 2:
                header.parseParam();
                break;
            default:
                Log.e(MessageParser.class.getCanonicalName() ,"解析错误，版本不支持" + header.getVersion());
                break;
        }
    }

    public static String construction(MessageHeader header) {
        /*
		JSONObject jsonObject = new JSONObject(header);
		return jsonObject.toString();
		*/
        return getJSON(header);
    }

    /**
     * 获取属性名数组
     */
    private static String[] getFieldName(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            System.out.println(fields[i].getType());
            fieldNames[i] = fields[i].getName();
        }
        return fieldNames;
    }

    public static String getJSON(Object obj) {
        JSONObject jsonObject = new JSONObject();
        Map<String, Object> map = getFieldValueByName(obj);
        Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entity = iterator.next();
            try {
                jsonObject.put(entity.getKey(), entity.getValue());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return jsonObject.toString();
    }

    public static String getJSON(Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();
        Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entity = iterator.next();
            try {
                jsonObject.put(entity.getKey(), entity.getValue());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return jsonObject.toString();
    }

    /**
     * @param obj
     * @return 返回对象的属性名和属性值的哈希表
     */
    public static Map<String, Object> getFieldValueByName(Object obj) {
        String[] fieldNames = getFieldName(obj);
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < fieldNames.length; i++) {
            try {
                String firstLetter = fieldNames[i].substring(0, 1).toUpperCase();
                String getter = "get" + firstLetter + fieldNames[i].substring(1);
                Method method = obj.getClass().getMethod(getter, new Class[]{});
                Object value = method.invoke(obj, new Object[]{});
                map.put(fieldNames[i], value);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return map;
    }





//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		MessageHeader header = new MessageHeader();
//		header.setFrom(UUID.randomUUID().toString());
//		header.setMethod(MessageHeader.Method.LOGOUT);
//		header.addDes(UUID.randomUUID().toString());
//		header.constructionParam();
//		// uuid=?#ip=?#port=?#loginTime=?#status=?#name=?#Img=?
//		// uuid#ip#port#loginTime#status#name#Img
//
//		//header.setParam("uuid=?#ip=?#port=?#loginTime=?#status=?#name=?#Img=?");
//		//header.setMethod(MessageHeader.Method.GET);
//		String json = getJSON(header);
//		System.out.println(json);
//		//parser(jsonObject.toString());
//	}
}
