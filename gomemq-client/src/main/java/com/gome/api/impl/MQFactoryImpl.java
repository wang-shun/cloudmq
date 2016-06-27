package com.gome.api.impl;

import com.alibaba.rocketmq.client.producer.LocalTransactionState;
import com.alibaba.rocketmq.client.producer.TransactionCheckListener;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.api.open.*;
import com.gome.util.MyUtils;

import java.util.Properties;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class MQFactoryImpl implements MQFactoryAPI {
    public MQFactoryImpl() {
    }

    public Producer createProducer(Properties properties) {
        ProducerImpl producer = new ProducerImpl(properties);
        return producer;
    }

    public Consumer createConsumer(Properties properties) {
        ConsumerImpl consumer = new ConsumerImpl(properties);
        return consumer;
    }

    public OrderProducer createOrderProducer(Properties properties) {
        OrderProducerImpl producer = new OrderProducerImpl(properties);
        return producer;
    }

    public OrderConsumer createOrderedConsumer(Properties properties) {
        OrderConsumerImpl consumer = new OrderConsumerImpl(properties);
        return consumer;
    }

    public TransactionProducer createTransactionProducer(Properties properties, final LocalTransactionChecker checker) {
        TransactionProducerImpl transactionProducer = new TransactionProducerImpl(properties, new TransactionCheckListener() {
            public LocalTransactionState checkLocalTransactionState(MessageExt msg) {
                String msgId = msg.getProperty("__transactionId__");
                Message message = MyUtils.msgConvert(msg);
                message.setMsgID(msgId);
                TransactionStatus check = checker.check(message);
                return TransactionStatus.CommitTransaction == check?LocalTransactionState.COMMIT_MESSAGE:(TransactionStatus.RollbackTransaction == check?LocalTransactionState.ROLLBACK_MESSAGE:LocalTransactionState.UNKNOW);
            }
        });
        return transactionProducer;
    }
}
