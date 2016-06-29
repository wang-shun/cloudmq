package com.gome.demo.simple;

import java.util.Properties;

import com.gome.api.open.base.*;
import com.gome.api.open.factory.MQFactory;
import com.gome.common.PropertiesConst;


/**
 * 广播方式订阅消息(所有消费订阅者都能收到消息)
 * 注意：为了保证消息队列性能，消息队列自身并不保证消息不会重复消费(在某些异常情况下偶尔会出现极少数重复消息)，
 * 若业务系统使用在非常严格的不允许消息重复的业务场景，则需要业务系统自身处理重复消息幂等
 *
 * @author tantexian
 * @since 2016/6/27
 */
public class BroadcastingConsumerTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的 ConsumerId
        properties.put(PropertiesConst.Keys.ConsumerId, "ConsumerId-test");
        // 设置为广播消费模式（不设置则默认为集群消费模式）
        properties.put(PropertiesConst.Keys.MessageModel, PropertiesConst.Values.BROADCASTING);
        // 不设置则默认为127.0.0.1:9876
        properties.put(PropertiesConst.Keys.NAMESRV_ADDR, MyProperties.getNameServerAddr());

        // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
        Consumer consumer = MQFactory.createConsumer(properties);
        consumer.subscribe("TopicTestMQ", "*", new MessageListener() {
            public Action consume(Message message, ConsumeContext context) {
                System.out.println("Receive: " + new String(message.getBody()));
                try {
                    // do something...
                    return Action.CommitMessage;
                }
                catch (Exception e) {
                    // 消费失败，返回ReconsumeLater，消息被放置到重试队列，延时后下次重新消费
                    return Action.ReconsumeLater;
                }
            }
        });
        // 消费者启动，开始消费消息
        consumer.start();
        System.out.println("consumer Started");
    }
}
