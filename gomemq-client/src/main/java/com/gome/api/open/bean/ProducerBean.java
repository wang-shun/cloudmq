package com.gome.api.open.bean;

import com.gome.api.open.base.Message;
import com.gome.api.open.base.Producer;
import com.gome.api.open.base.SendResult;
import com.gome.api.open.exception.GomeClientException;
import com.gome.api.open.factory.MQFactory;

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
        if(null == this.properties) {
            throw new GomeClientException("properties not set");
        } else {
            this.producer = MQFactory.createProducer(this.properties);
            this.producer.start();
        }
    }

    public void shutdown() {
        if(this.producer != null) {
            this.producer.shutdown();
        }

    }

    public SendResult send(Message message) {
        return this.producer.send(message);
    }

    public void sendOneway(Message message) {
        this.producer.sendOneway(message);
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
