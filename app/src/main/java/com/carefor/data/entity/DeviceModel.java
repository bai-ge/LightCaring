package com.carefor.data.entity;

import com.carefor.connect.ConnectedByTCP;

public class DeviceModel {
	private String deviceId;
	
	private int status;

	private long loginTime;

	private String localIp;
	private String remoteIp;

	private int localPort;
	private int remotePort;

	private int localUdpPort;
	private int remoteUdpPort;

	private ConnectedByTCP connector;

	public DeviceModel() {
		loginTime = System.currentTimeMillis();
	}

	public DeviceModel(String deviceid, String userid) {
		this();
		this.deviceId = deviceid;
	}

	public String getDeviceidId() {
		return deviceId;
	}

	public void setDeviceidId(String deviceid) {
		this.deviceId = deviceid;
	}
	
	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public String getLocalIp() {
		return localIp;
	}

	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public int getLocalUdpPort() {
		return localUdpPort;
	}

	public void setLocalUdpPort(int localUdpPort) {
		this.localUdpPort = localUdpPort;
	}

	public int getRemoteUdpPort() {
		return remoteUdpPort;
	}

	public void setRemoteUdpPort(int remoteUdpPort) {
		this.remoteUdpPort = remoteUdpPort;
	}

	public ConnectedByTCP getConnector() {
		return connector;
	}

	public void setConnector(ConnectedByTCP connector) {
		this.connector = connector;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "{deviceId=" + deviceId + ", localAddress=" + localIp + ":"
				+ localPort + ",remoteAddress=" + remoteIp + ":" + remotePort + ",localUdpPort="
				+ localUdpPort + ",remoteUdpPort=" + remoteUdpPort + "}";
	}

}
