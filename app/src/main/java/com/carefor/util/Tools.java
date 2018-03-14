package com.carefor.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.carefor.connect.Connector;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

/**
 * Created by baige on 2017/12/27.
 */

public class Tools {


    public static String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes("utf-8"));
            return toHex(bytes);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String ramdom(){
        int number = (int) (Math.random() * 900 + 100);
        return System.currentTimeMillis() + "_"+number;
    }

    public static String toHex(byte[] bytes) {

        final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i=0; i<bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }
    @TargetApi(Build.VERSION_CODES.N)
    public static byte[] toByte(long data) {
        byte[] buf = new byte[Long.BYTES];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) ((data >> (i * 8)) & 0xff);
        }
        return buf;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static byte[] toByte(int data) {
        byte[] buf = new byte[Integer.BYTES];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) ((data >> (i * 8)) & 0xff);
        }
        return buf;
    }

    public static long toLong(byte buf[]) {
        long data = 0x00;
        for (int i = buf.length - 1; i >= 0; i--) {
            data <<= 8;
            data |= (buf[i] & 0xff);
        }
        return data;
    }

    public static boolean isEmpty(String s) {
        if (null == s)
            return true;
        if (s.length() == 0)
            return true;
        if (s.trim().length() == 0)
            return true;
        return false;
    }
    public static String formatTime(long time){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date(time));
    }

    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            // 获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取NetworkInfo对象
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            //判断NetworkInfo对象是否为空
            if (networkInfo != null)
                return networkInfo.isAvailable();
        }
        return false;
    }

    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return
     */
    public static void checkNetwork(Context context){
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.isConnected()) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    Connector.getInstance().setWifiValid(true);
                    Log.d("network", "当前WiFi连接可用 ");
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    // connected to the mobile provider's data plan
                    Connector.getInstance().setNetworkValid(true);
                    Log.d("network", "当前移动网络连接可用 ");
                }
                //TODO 尝试连接服务器
                Connector.getInstance().afxConnectServer();
            } else {
                Log.d("network", "当前没有网络连接，请确保你已经打开网络 ");
                Connector.getInstance().setWifiValid(false);
                Connector.getInstance().setNetworkValid(false);
            }


            Log.d("network", "info.getTypeName()" + activeNetwork.getTypeName());
            Log.d("network", "getSubtypeName()" + activeNetwork.getSubtypeName());
            Log.d("network", "getState()" + activeNetwork.getState());
            Log.d("network", "getDetailedState()"
                    + activeNetwork.getDetailedState().name());
            Log.d("network", "getDetailedState()" + activeNetwork.getExtraInfo());
            Log.d("network", "getType()" + activeNetwork.getType());
        } else {   // not connected to the internet
            Log.d("network", "当前没有网络连接，请确保你已经打开网络 ");
            Connector.getInstance().setWifiValid(false);
            Connector.getInstance().setNetworkValid(false);
        }
    }
}
