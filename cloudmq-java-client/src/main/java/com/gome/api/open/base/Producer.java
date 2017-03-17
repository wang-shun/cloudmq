package com.gome.api.open.base;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface Producer extends Admin {
    void start();


    void shutdown();


    SendResult send(Msg msg);


    void sendOneway(Msg msg);
}
