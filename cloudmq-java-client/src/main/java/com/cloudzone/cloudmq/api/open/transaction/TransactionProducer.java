package com.cloudzone.cloudmq.api.open.transaction;


import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.base.Admin;
import com.cloudzone.cloudmq.api.open.base.TransactionSendResult;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface TransactionProducer extends Admin {
    void start();

    void shutdown();

    TransactionSendResult send(Msg msg, LocalTransactionExecuter localTransactionExecuter, Object obj);
}
