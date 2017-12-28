package com.carefor.data.entity;

import java.util.LinkedHashMap;
import java.util.Map;

public class MessageData {
	private String md5;
	private byte[] buf;
	private long sendTime;
	private String desIp;
	private int desPort;
	private String srcIp;
	private int srcPort;
	private Map<String, Object> attrMap;
	
	public MessageData() {
		attrMap = new LinkedHashMap<>();
	}
	
	public String getMd5() {
		return md5;
	}



	public void setMd5(String md5) {
		this.md5 = md5;
	}



	public byte[] getBuf() {
		return buf;
	}



	public void setBuf(byte[] buf) {
		this.buf = buf;
	}



	public long getSendTime() {
		return sendTime;
	}



	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}



	public String getDesIp() {
		return desIp;
	}



	public void setDesIp(String desIp) {
		this.desIp = desIp;
	}



	public int getDesPort() {
		return desPort;
	}



	public void setDesPort(int desPort) {
		this.desPort = desPort;
	}



	public String getSrcIp() {
		return srcIp;
	}



	public void setSrcIp(String srcIp) {
		this.srcIp = srcIp;
	}



	public int getSrcPort() {
		return srcPort;
	}



	public void setSrcPort(int srcPort) {
		this.srcPort = srcPort;
	}



	public Map<String, Object> getAttrMap() {
		return attrMap;
	}
	
	public boolean constructionParam() {
		//TODO 根据attrMap生成buf数据
		return false;
	}


	public void setAttrMap(Map<String, Object> attrMap) {
		this.attrMap = attrMap;
	}
	


	
}
