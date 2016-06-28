package com.gome.mq.simple;

import com.gome.api.open.base.*;
import com.gome.api.open.factory.MQFactory;
import com.gome.common.PropertyKeyConst;

import java.util.Properties;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class ConsumerTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.ConsumerId, "ConsumerId-test");// 您在控制台创建的 producer ID
        /*properties.put(PropertyKeyConst.NAMESRV_ADDR, "127.0.0.1:9876"); 不设置则默认为127.0.0.1:9876*/


        Consumer consumer = MQFactory.createConsumer(properties);
        consumer.subscribe("TopicTestMQ", "*", new MessageListener() {
            public Action consume(Message message, ConsumeContext context) {
                System.out.println("Receive: " + new String(message.getBody()));
                return Action.CommitMessage;
            }
        });
        consumer.start();
        System.out.println("consumer Started");
    }
}
