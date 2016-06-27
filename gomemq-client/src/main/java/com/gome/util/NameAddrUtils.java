package com.gome.util;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class NameAddrUtils {
    public NameAddrUtils() {
    }

    public static String getNameAdd() {
        return System.getProperty("gomemq.namesrv.addr", System.getenv("NAMESRV_ADDR"));
    }
}
