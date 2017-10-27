package com.cloudzone.cloudmq.api.open.order;

import com.cloudzone.cloudmq.api.open.base.Admin;
import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.base.SendResult;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface OrderProducer extends Admin {

    void start();


    void shutdown();


    SendResult send(Msg msg, String shardingKey);
}
