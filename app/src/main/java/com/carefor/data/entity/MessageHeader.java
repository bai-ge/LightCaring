package com.carefor.data.entity;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MessageHeader {
	private int version;
	private String from;
	private int method;
	private String param;

	private Map<String, String> paramMap;
	private ArrayList<String> deslist;

	
	public MessageHeader() {
		version = 1;
		from = null;
		method = Method.NULL;
		param = null;
		paramMap = new LinkedHashMap<>();
		deslist = new ArrayList<>();
	}

	public void parseParam() {
		if(param == null || param.equals("null")||param.equals("")){
			return;
		}
		String[] properties = param.split("#");
		for (String property : properties) {
			parseProperty(property);
		}
	}
    public void putParam(String key, String value){
        paramMap.put(key, value);
    }
	public boolean constructionParam(){
		
		if(paramMap == null || paramMap.isEmpty()){
			param = null;
		}
		Iterator<Entry<String, String>> iterator = paramMap.entrySet().iterator();
		StringBuffer stringBuffer = new StringBuffer();
		//添加目的地
		if(deslist != null && !deslist.isEmpty()){
			stringBuffer.append(Param.DES+"="+deslist.get(0));
			for (int i = 1; i < deslist.size(); i++) {
				stringBuffer.append(","+deslist.get(i));
			}
		}
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			if(entry.getKey() == null || entry.getKey().isEmpty()){
				continue;
			}
			if(stringBuffer.length() == 0){
				if(entry.getValue() == null || entry.getValue().isEmpty()){
					stringBuffer.append(entry.getKey());
				}else{
					stringBuffer.append(entry.getKey()+"="+entry.getValue());
				}
				
			}else{
				if(entry.getValue() == null || entry.getValue().isEmpty()){
					stringBuffer.append("#"+entry.getKey());
				}else{
					stringBuffer.append("#"+entry.getKey()+"="+entry.getValue());
				}
			}
		}
		param = stringBuffer.toString();
		return true;
	}
	
	private void parseProperty(String property) {
		int index = property.indexOf('=');
		if (index != -1) {
			String key = property.substring(0, index);
			String value = property.substring(index + 1);
			paramMap.put(key, value);
			//解析目的地
			if(key.equals(Param.DES)) {
                parseDes(value);
            }
		}else{
			paramMap.put(property, "");
		}
	}
	private void parseDes(String desString) {
		deslist.clear();
		if(desString == null || desString.isEmpty()){
			return;
		}
		String[] des = desString.split(",");
		for (int i = 0; i < des.length; i++) {
			deslist.add(des[i]);
		}
	}
	
	public void addDes(String id){
		if(deslist == null){
			deslist = new ArrayList<>();
		}
		deslist.add(id);
	}
	public void clearDes(){
		if(deslist == null){
			deslist = new ArrayList<>();
			return;
		}
		deslist.clear();
	}

    public ArrayList<String> getDeslist() {
        return deslist;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return MessageHeader.class.getCanonicalName() + "{version=" + version + ",from=" + from + ",method=" + method
				+ ",param=" + param + "}";
	}

	public class Method {
		public final static int NULL = 0;
		public final static int GET = 1;
		public final static int POST = 2;
		public final static int LOGIN = 4;
		public final static int LOGOUT = 8;
        public final static int CALL_TO = 16;
        public final static int PICK_UP = 32;
        public final static int HANG_UP = 64;
        public final static int REPLY_CALL_TO = 128;
        public final static int REPLY_CHECK = 256;
	}
	public class Param{
        public final static String DES = "des";
        public final static String UUID = "uuid";
        public final static String LOCAL_IP = "local_ip";
        public final static String REMOTE_IP = "remote_ip";
        public final static String LOCAL_PORT = "local_port";
        public final static String REMOTE_PORT = "remote_port";
        public final static String LOCAL_UDP_PORT = "local_udp_port";
        public final static String REMOTE_UDP_PORT = "remote_udp_port";
        public final static String LOGIN_TIME = "login_time";
        public final static String STATUS = "status";
        public final static String NAME = "name";
        public final static String IMG = "img";
        public final static String USERS = "users";
        public final static String HANG_UP = "hang_up";
        public final static String PICK_UP = "pick_up";
        public final static String SERVERS = "servers";
	}

}
