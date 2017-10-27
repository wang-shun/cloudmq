package com.cloudzone.cloudmq.api.open.factory;

import com.cloudzone.cloudmq.api.open.base.Consumer;
import com.cloudzone.cloudmq.api.open.order.OrderConsumer;
import com.cloudzone.cloudmq.api.open.order.OrderProducer;
import com.cloudzone.cloudmq.api.open.transaction.LocalTransactionChecker;
import com.cloudzone.cloudmq.api.open.transaction.TransactionProducer;
import com.cloudzone.cloudmq.api.open.base.Producer;

import java.util.Properties;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface MQFactoryAPI {
    Producer createProducer(Properties properties);


    Consumer createConsumer(Properties properties);


    OrderProducer createOrderProducer(Properties properties);


    OrderConsumer createOrderedConsumer(Properties properties);


    TransactionProducer createTransactionProducer(Properties properties, LocalTransactionChecker localTransactionChecker);
}
