package com.gome.api.open;

import java.util.Properties;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public class MQFactory {
    private static MQFactoryAPI mqFactoryInstance = null;


    public MQFactory() {
    }


    public static Producer createProducer(Properties properties) {
        return mqFactoryInstance.createProducer(properties);
    }


    public static OrderProducer createOrderProducer(Properties properties) {
        return mqFactoryInstance.createOrderProducer(properties);
    }

    // 当前版本没有实现事务消息功能 2016/6/27 Add by tantexixan
    @Deprecated
    public static TransactionProducer createTransactionProducer(Properties properties,
            LocalTransactionChecker checker) {
        return mqFactoryInstance.createTransactionProducer(properties, checker);
    }


    public static Consumer createConsumer(Properties properties) {
        return mqFactoryInstance.createConsumer(properties);
    }


    public static OrderConsumer createOrderedConsumer(Properties properties) {
        return mqFactoryInstance.createOrderedConsumer(properties);
    }

    static {
        try {
            Class clazz = MQFactory.class.getClassLoader().loadClass("com.gome.api.impl.MQFactoryImpl");
            mqFactoryInstance = (MQFactoryAPI) clazz.newInstance();
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
