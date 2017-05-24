package com.cloudzone.cloudmq.api.impl.producer;

import com.cloudzone.cloudmq.api.open.base.Producer;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;
import com.cloudzone.cloudmq.common.PropertiesConst;

import java.util.Properties;

/**
 * @author yintongjiang
 * @params
 * @since 2017/4/12
 */
public class ProducerFactory {
    private ProducerFactory() {
    }

    private final static Properties PROPERTIES = new Properties();
    public final static String TOPIC = "jcpt-client-to-cloudzone-800";
    public final static String TAG = "mq";


    static {
        PROPERTIES.put(PropertiesConst.Keys.ProducerGroupId, "innerMQSDKGroupId");
        PROPERTIES.put(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY,
                "jcpt-client-to-cloudzone-800:047561f2375564b8eb506b76e6023a8a6");
    }

    private static volatile Producer producer;

    public static Producer getProducerSingleton() {
        if (null == producer)
            synchronized (ProducerFactory.class) {
                if (null == producer)
                    producer = MQFactory.createProducer(PROPERTIES);
            }
        return producer;
    }
}
