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
        if (null == this.subscriptionTable) {
            throw new GomeClientException("subscriptionTable not set");
        }

        this.consumer = MQFactory.createConsumer(this.properties);
        Iterator it = this.subscriptionTable.entrySet().iterator();

        while (true) {
            while (it.hasNext()) {
                Map.Entry next = (Map.Entry) it.next();
                Class clazz = this.consumer.getClass();
                boolean isConsumerImpl = clazz.getCanonicalName().equals("ConsumerImpl.ConsumerImpl");
                boolean isSubscriptionExt = next.getKey() instanceof SubscriptionExt;

                MsgListener msgListener = (MsgListener) next.getValue();
                SubscriptionExt subscription = (SubscriptionExt) next.getKey();
                String topic = subscription.getTopic();
                String expression = subscription.getExpression();

                if (isConsumerImpl && isSubscriptionExt) {
                    Method[] methods = clazz.getMethods();
                    int len = methods.length;
                    for (int i = 0; i < len; ++i) {
                        Method method = methods[i];
                        boolean subscribeNotify = method.getName().equals("subscribeNotify");
                        if (subscribeNotify) {
                            invokeSubscribeNotify(method, subscription, msgListener);
                            break;
                        }
                    }
                }
                else {
                    this.subscribe(topic, expression, msgListener);
                }
            }

            this.consumer.start();
            return;
        }
    }


    private void invokeSubscribeNotify(Method method, SubscriptionExt subscription, MsgListener msgListener) {
        String topic = subscription.getTopic();
        String expression = subscription.getExpression();
        boolean isPersistence = Boolean.valueOf(subscription.isPersistence());

        try {
            Object obj = new Object[] { topic, expression, isPersistence, msgListener };
            method.invoke(this.consumer, obj);
        }
        catch (Exception e) {
            throw new GomeClientException("subscribeNotify invoke exception", e);
        }
    }


    public void shutdown() {
        if (this.consumer != null) {
            this.consumer.shutdown();
        }
    }


    public void subscribe(String topic, String subExpression, MsgListener listener) {
        if (null == this.consumer) {
            String errMsg = "unsubscribe must be called after consumerBean started";
            throw new GomeClientException(errMsg);
        }

        this.consumer.subscribe(topic, subExpression, listener);
    }


    public void unsubscribe(String topic) {
        if (null == this.consumer) {
            String errMsg = "unsubscribe must be called after consumerBean started";
            throw new GomeClientException(errMsg);
        }

        this.consumer.unsubscribe(topic);
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
