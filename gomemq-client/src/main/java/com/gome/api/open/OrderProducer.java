package com.gome.api.open;

import com.alibaba.rocketmq.client.producer.SendResult;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface OrderProducer extends Admin {
    void start();

    void shutdown();

    SendResult send(Message var1, String var2);
}
