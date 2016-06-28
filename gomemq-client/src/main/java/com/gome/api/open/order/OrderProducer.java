package com.gome.api.open.order;


import com.gome.api.open.base.Admin;
import com.gome.api.open.base.Message;
import com.gome.api.open.base.SendResult;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface OrderProducer extends Admin {
    void start();

    void shutdown();

    SendResult send(Message msg, String shardingKey);
}
