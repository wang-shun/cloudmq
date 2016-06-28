package com.gome.api.open.base;

import java.util.Properties;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class MessageAccessor {
    public MessageAccessor() {
    }

    public static Properties getSystemProperties(Message msg) {
        return msg.systemProperties;
    }

    public static void setSystemProperties(Message msg, Properties systemProperties) {
        msg.systemProperties = systemProperties;
    }

    public static void putSystemProperties(Message msg, String key, String value) {
        msg.putSystemProperties(key, value);
    }

    public static String getSystemProperties(Message msg, String key) {
        return msg.getSystemProperties(key);
    }
}
