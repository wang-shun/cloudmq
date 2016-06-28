package com.gome.mq.order;

import com.gome.api.open.base.*;
import com.gome.api.open.factory.MQFactory;
import com.gome.api.open.order.ConsumeOrderContext;
import com.gome.api.open.order.MessageOrderListener;
import com.gome.api.open.order.OrderAction;
import com.gome.api.open.order.OrderConsumer;
import com.gome.common.PropertyKeyConst;
import com.gome.mq.MyProperties;

import java.util.Properties;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public class OrderConsumerTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.ConsumerId, "OrderConsumerTest-Id");// 您在控制台创建的consumer id
        properties.put(PropertyKeyConst.NAMESRV_ADDR, MyProperties.getNameServerAddr()); //不设置则默认为127.0.0.1:9876

        OrderConsumer orderedConsumer = MQFactory.createOrderedConsumer(properties);

        orderedConsumer.subscribe("TopicOrderTestMQ", "*", new MessageOrderListener() {
            @Override
            public OrderAction consume(Message msg, ConsumeOrderContext consumeOrderContext) {
                System.out.println(new String(msg.getBody()));
                return OrderAction.Success;
            }
        });
        orderedConsumer.start();
        System.out.println("order consumer Started");
    }
}
