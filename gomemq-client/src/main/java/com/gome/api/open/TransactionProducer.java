package com.gome.api.open;

import com.alibaba.rocketmq.client.producer.SendResult;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface TransactionProducer extends Admin {
    void start();

    void shutdown();

    SendResult send(Message msg, LocalTransactionExecuter localTransactionExecuter, Object obj);
}
