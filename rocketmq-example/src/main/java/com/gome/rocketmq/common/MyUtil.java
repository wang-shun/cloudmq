package com.gome.rocketmq.common;

public class MyUtil {

    private static String namesrvAddr =
            "10.128.31.103:9876;10.128.31.104:9876;10.128.31.105:9876;10.128.31.106:9876";


    public static String getNamesrvAddr() {
        return namesrvAddr;
    }
}
