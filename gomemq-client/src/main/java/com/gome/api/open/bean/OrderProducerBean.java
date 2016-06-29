package com.gome.api.open.bean;

import java.util.Properties;

import com.gome.api.open.base.Message;
import com.gome.api.open.base.SendResult;
import com.gome.api.open.exception.GomeClientException;
import com.gome.api.open.factory.MQFactory;
import com.gome.api.open.order.OrderProducer;

/**
 * 顺序消息生产者bean
 *
 * @author tantexian
 * @since 2016/6/27
 */
public class OrderProducerBean implements OrderProducer {
    private Properties properties;
    private OrderProducer orderProducer;

    public OrderProducerBean() {
    }

    @Override
    public SendResult send(Message msg, String shardingKey) {
        return this.orderProducer.send(msg, shardingKey);
    }

    public void start() {
        if (null == this.properties) {
            throw new GomeClientException("properties not set");
        } else {
            this.orderProducer = MQFactory.createOrderProducer(this.properties);
            this.orderProducer.start();
        }
    }

    public void shutdown() {
        if (this.orderProducer != null) {
            this.orderProducer.shutdown();
        }

    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public boolean isStarted() {
        return this.orderProducer.isStarted();
    }

    public boolean isClosed() {
        return this.orderProducer.isClosed();
    }

}
