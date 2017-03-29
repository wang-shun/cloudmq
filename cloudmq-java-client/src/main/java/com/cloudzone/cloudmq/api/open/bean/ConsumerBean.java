package com.cloudzone.cloudmq.api.open.bean;

import com.cloudzone.cloudmq.api.open.base.Consumer;
import com.cloudzone.cloudmq.api.open.base.MsgListener;
import com.cloudzone.cloudmq.api.open.exception.GomeClientException;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public class ConsumerBean implements Consumer {
    private Properties properties;
    private Map<Subscription, MsgListener> subscriptionTable;
    private Consumer consumer;


    public ConsumerBean() {
    }


    public void start() {
        if (null == this.properties) {
            throw new GomeClientException("properties not set");
        }
        else if (null == this.subscriptionTable) {
            throw new GomeClientException("subscriptionTable not set");
        }
        else {
            this.consumer = MQFactory.createConsumer(this.properties);
            Iterator it = this.subscriptionTable.entrySet().iterator();

            while (true) {
                while (it.hasNext()) {
                    Map.Entry next = (Map.Entry) it.next();
                    if (this.consumer.getClass().getCanonicalName()
                        .equals("ConsumerImpl.ConsumerImpl")
                            && next.getKey() instanceof SubscriptionExt) {
                        SubscriptionExt subscription = (SubscriptionExt) next.getKey();
                        Method[] methods = this.consumer.getClass().getMethods();
                        int len = methods.length;

                        for (int i = 0; i < len; ++i) {
                            Method method = methods[i];
                            if ("subscribeNotify".equals(method.getName())) {
                                try {
                                    method.invoke(this.consumer,
                                        new Object[] { subscription.getTopic(), subscription.getExpression(),
                                                       Boolean.valueOf(subscription.isPersistence()),
                                                       next.getValue() });
                                    break;
                                }
                                catch (Exception e) {
                                    throw new GomeClientException("subscribeNotify invoke exception", e);
                                }
                            }
                        }
                    }
                    else {
                        this.subscribe(((Subscription) next.getKey()).getTopic(),
                            ((Subscription) next.getKey()).getExpression(),
                            (MsgListener) next.getValue());
                    }
                }

                this.consumer.start();
                return;
            }
        }
    }


    public void shutdown() {
        if (this.consumer != null) {
            this.consumer.shutdown();
        }

    }


    public void subscribe(String topic, String subExpression, MsgListener listener) {
        if (null == this.consumer) {
            throw new GomeClientException("subscribe must be called after consumerBean started");
        }
        else {
            this.consumer.subscribe(topic, subExpression, listener);
        }
    }


    public void unsubscribe(String topic) {
        if (null == this.consumer) {
            throw new GomeClientException("unsubscribe must be called after consumerBean started");
        }
        else {
            this.consumer.unsubscribe(topic);
        }
    }


    public Properties getProperties() {
        return this.properties;
    }


    public void setProperties(Properties properties) {
        this.properties = properties;
    }


    public Map<Subscription, MsgListener> getSubscriptionTable() {
        return this.subscriptionTable;
    }


    public void setSubscriptionTable(Map<Subscription, MsgListener> subscriptionTable) {
        this.subscriptionTable = subscriptionTable;
    }


    public boolean isStarted() {
        return this.consumer.isStarted();
    }


    public boolean isClosed() {
        return this.consumer.isClosed();
    }
}
