package com.carefor.data.entity;

public class Candidate {
	private long time;
	private String from; //回复的主机
	
	private String relayIp;//回复主机的Ip
	private String relayPort;//回复主机的端口
	
	private String localIp;
	private String localPort;
	
	private String remoteIp;
	private String remotePort;
	
	private long delayTime;
	
	
	public long getDelayTime() {
		return delayTime;
	}
	public void setDelayTime(long delayTime) {
		this.delayTime = delayTime;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getRelayIp() {
		return relayIp;
	}
	public void setRelayIp(String relayIp) {
		this.relayIp = relayIp;
	}
	public String getRelayPort() {
		return relayPort;
	}
	public void setRelayPort(String relayPort) {
		this.relayPort = relayPort;
	}
	public String getLocalIp() {
		return localIp;
	}
	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}
	public String getLocalPort() {
		return localPort;
	}
	public void setLocalPort(String localPort) {
		this.localPort = localPort;
	}
	public String getRemoteIp() {
		return remoteIp;
	}
	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}
	public String getRemotePort() {
		return remotePort;
	}
	public void setRemotePort(String remotePort) {
		this.remotePort = remotePort;
	}
	@Override
	public String toString() {
		return "Candidate [time=" + time + ", from=" + from + ", relayIp=" + relayIp + ", relayPort=" + relayPort
				+ ", localIp=" + localIp + ", localPort=" + localPort + ", remoteIp=" + remoteIp + ", remotePort="
				+ remotePort + ", delayTime=" + delayTime + "]";
	}
	
	
}
