package com.gome.api.open.base;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface Consumer extends Admin {
    void start();

    void shutdown();

    void subscribe(String str1, String str2, MessageListener messageListener);

    void unsubscribe(String str1);
}
