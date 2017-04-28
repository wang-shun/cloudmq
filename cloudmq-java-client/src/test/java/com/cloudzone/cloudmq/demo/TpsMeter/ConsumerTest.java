package com.cloudzone.cloudmq.demo.TpsMeter;

import com.cloudzone.cloudmq.api.open.base.*;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;
import com.cloudzone.cloudmq.common.PropertiesConst;

import java.util.Properties;

/**
 * 集群方式订阅消息(所有消费订阅者共同消费消息(分摊)，消息队列默认为集群消费)
 * 注意：集群模式消费则 ConsumerGroupId 必须相同。
 * 其次为了保证消息队列性能，消息队列自身并不保证消息不会重复消费(在某些异常情况下偶尔会出现极少数重复消
 * 息)，
 * 若业务系统使用在非常严格的不允许消息重复的业务场景，则需要业务系统自身处理重复消息幂等
 *
 * @author tantexian
 * @since 2016/6/27
 */
public class ConsumerTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的消费者组 ID（ConsumerGroupId）
        // 集群模式下消费，该 ConsumerGroupId 必须相同
        properties.put(PropertiesConst.Keys.ConsumerGroupId, "SimpleConsumerGroupId-test");
        // 设置邮件反馈的 topic 名称
        // TOPIC_NAME_AND_AUTH_KEY 的值, 即为 Topic 和 AuthKey 的键值对
        properties.put(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY,
                "ttx-test-tps-200:0592547a64ceb483b9173da139440a53e");
        // 创建普通类型消费者
        Consumer consumer = MQFactory.createConsumer(properties);
        // 消费者订阅消费，建议业务程序自行记录生产及消费 log 日志，
        // 以方便您在无法正常收到消息情况下，可通过 MQ 控制台或者 MQ 日志查询消息并补发。
        consumer.subscribe("ttx-test-tps-200", "*", new MsgListener() {
            public Action consume(Msg msg, ConsumeContext context) {
                //TODO: 此处为线程池调用，使用过程中请注意线程安全问题！！！
                System.out.println(Thread.currentThread().getName() + "Receive Msg : " +
                        new String(msg.getBody()));
                try {
                    // do something..
                    return Action.CommitMessage;
                } catch (Exception e) {
                    // 消费失败，返回 ReconsumeLater，消息被放置到重试队列，延时后下次重新消费
                    return Action.ReconsumeLater;
                }
            }
        });
        // 启动消费者，开始消费
        consumer.start();
        System.out.println("Simple Push consumer Started");
    }
}
