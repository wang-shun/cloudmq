package com.cloudzone.cloudmq.api.open.bean;

import com.cloudzone.cloudmq.api.open.exception.GomeClientException;
import com.cloudzone.cloudmq.api.open.order.OrderConsumer;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;
import com.cloudzone.cloudmq.api.open.order.MsgOrderListener;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author limin
 * @date 2017/2/22
 */
public class OrderConsumerBean implements OrderConsumer {
    private Properties properties;
    private Map<Subscription, MsgOrderListener> subscriptionTable;
    private OrderConsumer orderConsumer;


    public OrderConsumerBean() {
    }


    public void start() {
        if (null == this.properties) {
            throw new GomeClientException("properties not set");
        }
        else if (null == this.subscriptionTable) {
            throw new GomeClientException("subscriptionTable not set");
        }
        else {
            this.orderConsumer = MQFactory.createOrderedConsumer(this.properties);
            Iterator it = this.subscriptionTable.entrySet().iterator();

            while (true) {
                while (it.hasNext()) {
                    Map.Entry next = (Map.Entry) it.next();
                    if (this.orderConsumer.getClass().getCanonicalName()
                            .equals("ConsumerImpl.ConsumerImpl")
                            && next.getKey() instanceof SubscriptionExt) {
                        SubscriptionExt subscription = (SubscriptionExt) next.getKey();
                        Method[] methods = this.orderConsumer.getClass().getMethods();
                        int len = methods.length;

                        for (int i = 0; i < len; ++i) {
                            Method method = methods[i];
                            if ("subscribeNotify".equals(method.getName())) {
                                try {
                                    method.invoke(this.orderConsumer,
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
                                (MsgOrderListener) next.getValue());
                    }
                }

                this.orderConsumer.start();
                return;
            }
        }
    }


    public void shutdown() {
        if (this.orderConsumer != null) {
            this.orderConsumer.shutdown();
        }

    }


    public void subscribe(String topic, String subExpression, MsgOrderListener listener) {
        if (null == this.orderConsumer) {
            throw new GomeClientException("subscribe must be called after consumerBean started");
        }
        else {
            this.orderConsumer.subscribe(topic, subExpression, listener);
        }
    }


    /*public void unsubscribe(String topic) {
        if (null == this.orderConsumer) {
            throw new GomeClientException("unsubscribe must be called after consumerBean started");
        }
        else {
            this.orderConsumer.unsubscribe(topic);
        }
    }*/


    public Properties getProperties() {
        return this.properties;
    }


    public void setProperties(Properties properties) {
        this.properties = properties;
    }


    public Map<Subscription, MsgOrderListener> getSubscriptionTable() {
        return this.subscriptionTable;
    }


    public void setSubscriptionTable(Map<Subscription, MsgOrderListener> subscriptionTable) {
        this.subscriptionTable = subscriptionTable;
    }


    public boolean isStarted() {
        return this.orderConsumer.isStarted();
    }


    public boolean isClosed() {
        return this.orderConsumer.isClosed();
    }
}
