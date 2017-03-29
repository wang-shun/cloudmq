package com.cloudzone.cloudmq.api.open.bean;

import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.exception.GomeClientException;
import com.cloudzone.cloudmq.api.open.base.Producer;
import com.cloudzone.cloudmq.api.open.base.SendResult;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;
import com.cloudzone.cloudmq.common.PropertiesConst;

import java.util.Properties;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class ProducerBean implements Producer {
    private Properties properties;
    private Producer producer;

    public ProducerBean() {
    }

    public void start() {
        if (null == this.properties) {
            throw new GomeClientException("properties not set");
        } else {
            this.producer = MQFactory.createProducer(this.properties);
            this.producer.start();
        }
    }

    public void shutdown() {
        if (this.producer != null) {
            this.producer.shutdown();
        }

    }

    public SendResult send(Msg msg) {
        return this.producer.send(msg);
    }

    public void sendOneway(Msg msg) {
        this.producer.sendOneway(msg);
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public boolean isStarted() {
        return this.producer.isStarted();
    }

    public boolean isClosed() {
        return this.producer.isClosed();
    }
}
