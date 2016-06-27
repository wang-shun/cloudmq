package com.gome.api.open;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface Producer extends Admin {
    void start();


    void shutdown();


    SendResult send(Message msg);


    void sendOneway(Message msg);
}
