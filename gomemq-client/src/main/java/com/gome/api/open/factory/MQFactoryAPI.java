package com.gome.api.open.factory;

import com.gome.api.open.base.Consumer;
import com.gome.api.open.transaction.LocalTransactionChecker;
import com.gome.api.open.base.Producer;
import com.gome.api.open.transaction.TransactionProducer;
import com.gome.api.open.order.OrderConsumer;
import com.gome.api.open.order.OrderProducer;

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
