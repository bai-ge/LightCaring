package com.carefor.data.entity;
/**********************************************************
 * 这是json传输的信息类，初步解析的结果都放在这里
 **********************************************************/
import java.util.LinkedHashMap;

public class Transinformation {
	private MessageHeader messageHeader;
	private LinkedHashMap<String, Object> dateMap;
	
	public Transinformation() {
		messageHeader = new MessageHeader();
		dateMap = new LinkedHashMap<>();
	}
	public MessageHeader getMessageHeader() {
		return messageHeader;
	}
	public void setMessageHeader(MessageHeader messageHeader) {
		this.messageHeader = messageHeader;
	}
	public LinkedHashMap<String, Object> getDateMap() {
		return dateMap;
	}
	public void setDateMap(LinkedHashMap<String, Object> dateMap) {
		this.dateMap = dateMap;
	}
	

}
