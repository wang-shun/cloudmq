package com.gome.api.open;

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
