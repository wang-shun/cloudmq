package com.gome.api.open;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface OrderConsumer extends Admin {
    void start();

    void shutdown();

    void subscribe(String str1, String str2, MessageOrderListener messageOrderListener);
}
