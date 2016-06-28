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

    /**
     * 顺序消息
     *
     * @author tantexian
     * @since 2016/6/28
     */
    public static OrderConsumer createOrderedConsumer(Properties properties) {
        return mqFactoryInstance.createOrderedConsumer(properties);
    }

    static {
        try {
            Class clazz = MQFactory.class.getClassLoader().loadClass("com.gome.api.impl.base.MQFactoryImpl");
            mqFactoryInstance = (MQFactoryAPI) clazz.newInstance();
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
