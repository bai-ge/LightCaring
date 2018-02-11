package com.carefor.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.security.MessageDigest;

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

}
