package com.gome.demo.simple;

/**
 * 此处为临时的测试Demo属性类，用于动态获取及设置nameServerAddr
 *
 * @author tantexian
 * @since 2016/6/28
 */
public class MyProperties {
    private static String nameServerAddr = "10.128.31.103:9876";

    public static String getNameServerAddr() {
        return nameServerAddr;
    }
}

