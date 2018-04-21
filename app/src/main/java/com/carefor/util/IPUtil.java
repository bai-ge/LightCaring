package com.carefor.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * IPUtil
 * AndroidSocketClient <com.vilyever.socketclient.util>
 * Created by vilyever on 2016/3/30.
 * Feature:
 */
public class IPUtil {
    final IPUtil self = this;

    
    /* Constructors */


    /* Public Methods */
    public static String getLocalIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : inetAddresses) {
                    if (!address.isLoopbackAddress()) {
                        String sAddr = address.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    /* Public Methods */
    public static List<String> getAllLocalIPAddress(boolean useIPv4) {
        ArrayList<String> addressList = new ArrayList<>();
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : inetAddresses) {
                    if (!address.isLoopbackAddress()) {
                        String sAddr = address.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4 && isIPv4) {
                            addressList.add(sAddr);
                        } else if (!useIPv4 && !isIPv4) {
                            int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                            String ip6 = delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            addressList.add(ip6);

                        }
                    }
                }
            }
        } catch (Exception ex) {

        } // for now eat exceptions
        return addressList;
    }
    
    /* Properties */
    
    
    /* Overrides */
     
     
    /* Delegates */
     
     
    /* Private Methods */

}