package com.cloudzone.cloudmq.api.open.base;

import java.util.Properties;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class MessageAccessor {
    public MessageAccessor() {
    }

    public static Properties getSystemProperties(Msg msg) {
        return msg.systemProperties;
    }

    public static void setSystemProperties(Msg msg, Properties systemProperties) {
        msg.systemProperties = systemProperties;
    }

    public static void putSystemProperties(Msg msg, String key, String value) {
        msg.putSystemProperties(key, value);
    }

    public static String getSystemProperties(Msg msg, String key) {
        return msg.getSystemProperties(key);
    }
}
