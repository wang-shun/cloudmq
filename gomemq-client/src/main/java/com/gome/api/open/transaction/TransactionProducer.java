package com.gome.api.open.transaction;

import com.alibaba.rocketmq.client.producer.SendResult;
import com.gome.api.open.base.Admin;
import com.gome.api.open.base.Message;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface TransactionProducer extends Admin {
    void start();

    void shutdown();

    SendResult send(Message msg, LocalTransactionExecuter localTransactionExecuter, Object obj);
}
