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
//    @Deprecated
    public static TransactionProducer createTransactionProducer(Properties properties,
                                                                LocalTransactionChecker checker) {
        return mqFactoryInstance.createTransactionProducer(properties, checker);
    }


    /**
     * 注意：为了保证消息队列性能，消息队列自身并不保证消息不会重复消费(在某些异常情况下偶尔会出现极少数重复消息)，
     * 若业务系统使用在非常严格的不允许消息重复的业务场景，则需要业务系统自身处理重复消息幂等
     *
     * @author tantexian
     * @since 2016/6/29
     * @params
     */
    public static Consumer createConsumer(Properties properties) {
        return mqFactoryInstance.createConsumer(properties);
    }

    /**
     * 顺序消息（建议尽量使用常规模式，顺序类型会降低性能及可靠性）
     * 注意：为了保证消息队列性能，消息队列自身并不保证消息不会重复消费(在某些异常情况下偶尔会出现极少数重复消息)，
     * 若业务系统使用在非常严格的不允许消息重复的业务场景，则需要业务系统自身处理重复消息幂等
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
