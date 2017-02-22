package com.gome.demo.transaction;

import com.gome.api.open.base.*;
import com.gome.api.open.factory.MQFactory;
import com.gome.common.PropertiesConst;

import java.util.Properties;

/**
 * 集群方式订阅消息(所有消费订阅者共同消费消息(分摊)，消息队列默认为集群消费)
 * 注意：集群模式消费则ConsumerGroupId必须相同。
 * 其次为了保证消息队列性能，消息队列自身并不保证消息不会重复消费(在某些异常情况下偶尔会出现极少数重复消息)，
 * 若业务系统使用在非常严格的不允许消息重复的业务场景，则需要业务系统自身处理重复消息幂等
 *
 * @author leiyuanjie
 * @since 2017-02-17.
 */
public class TransactionConsumerTest {

    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的消费者组ID（ConsumerGroupId）
        // 集群模式下消费，该ConsumerGroupId必须相同
        properties.put(PropertiesConst.Keys.ConsumerGroupId, "TransactionConsumerGroupId-test");
        // 设置nameserver地址，不设置则默认为127.0.0.1:9876
        properties.put(PropertiesConst.Keys.NAMESRV_ADDR, "127.0.0.1:9876");

        Consumer consumer = MQFactory.createConsumer(properties);
        // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，
        // 以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
        consumer.subscribe("TransactionTopicTestMQ", "*", new MsgListener() {
            public Action consume(Msg msg, ConsumeContext context) {
                // 此处为线程池调用，使用过程中请注意线程安全问题！！！
                System.out.println(Thread.currentThread().getName() + "Receive Msg : " + new String(msg.getBody()));
                try {
                    // do something..
                    return Action.CommitMessage;
                }
                catch (Exception e) {
                    // 消费失败，返回ReconsumeLater，消息被放置到重试队列，延时后下次重新消费
                    return Action.ReconsumeLater;
                }
            }
        });
        // 启动消费者，开始消费
        consumer.start();
        System.out.println("transaction consumer Started");
    }
}