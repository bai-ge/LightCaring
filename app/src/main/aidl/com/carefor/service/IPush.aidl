// IPush.aidl
package com.carefor.service;

// Declare any non-default types here with import statements

interface IPush {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     int getConnectState();
     int connectToServer(String ip, String port);
     int sendMessage(String msg);
     int disconnectServer();
     int getPid();
     String getDeviceId();
     String setDeviceId(String deviceId);
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}
