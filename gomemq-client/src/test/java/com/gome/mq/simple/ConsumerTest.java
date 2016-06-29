package com.gome.mq.simple;

import java.util.Properties;

import com.gome.api.open.base.*;
import com.gome.api.open.factory.MQFactory;
import com.gome.api.open.order.OrderAction;
import com.gome.common.PropertiesConst;
import com.gome.mq.MyProperties;


/**
 * 集群方式订阅消息(所有消费订阅者共同消费消息(分摊)，消息队列默认为集群消费)
 * 注意：集群模式消费则ConsumerId必须相同
 *
 * @author tantexian
 * @since 2016/6/27
 */
public class ConsumerTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的 ConsumerId（集群模式下消费，则该ConsumerId必须相同）
        properties.put(PropertiesConst.Keys.ConsumerId, "ConsumerId-test");
        // 不设置则默认为127.0.0.1:9876
        properties.put(PropertiesConst.Keys.NAMESRV_ADDR, MyProperties.getNameServerAddr());

        // 创建普通类型消费者
        Consumer consumer = MQFactory.createConsumer(properties);
        // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
        consumer.subscribe("TopicTestMQ", "*", new MessageListener() {
            public Action consume(Message message, ConsumeContext context) {
                System.out.println("Receive Message : " + new String(message.getBody()));
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
        System.out.println("consumer Started");
    }
}
