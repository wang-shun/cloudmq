package com.gome.mq;

/**
 * @author tantexian
 * @since 2016/6/28
 */
public class MyProperties {
    private static String nameServerAddr = "10.128.31.103:9876";

    public static String getNameServerAddr() {
        return nameServerAddr;
    }

    public static void setNameServerAddr(String nameServerAddr) {
        nameServerAddr = nameServerAddr;
    }
}

