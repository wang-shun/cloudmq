package com.cloudzone.cloudmq.demo.dilatation;

import com.cloudzone.cloudmq.api.open.base.*;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;
import com.cloudzone.cloudmq.common.PropertiesConst;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class DilatationOldTopicConsumer {


    public static void main(String[] args) {
        final String topicName = args.length >= 1 ? args[0] : "CloudTopicTest-200";
        final String tag = args.length >= 2 ? args[1] : "A";
        final String group = args.length >= 3 ? args[2] : "testConsumer";

        final AtomicLong msgCount = new AtomicLong(0L);
        Properties properties = new Properties();
        properties.put(PropertiesConst.Keys.ConsumerGroupId, group);
        properties.put(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY,
                "CloudTopicTest-200:02865ea17c4eb4186854ab95bdc07f842");
        Consumer consumer = MQFactory.createConsumer(properties);

        // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，
        // 以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
//        consumer.subscribe(topicName, tag, new MsgListener() {
//            @Override
//            public Action consume(Msg msg, ConsumeContext consumeContext) {
//                msgCount.incrementAndGet();
//                System.out.println(
//                        "topic=" + msg.getTopic() + ",body=" + new String(msg.getBody()) +
//                                ",tags=" + msg.getTags() + ",msgKey=" + msg.getKey() + ",msgCount=" + msgCount.get());
//                try {
//                    return Action.CommitMessage;
//                } catch (Exception e) {
//                    return Action.ReconsumeLater;
//                }
//            }
//        });
        consumer.subscribe(topicName, "*", new MsgListener() {
            @Override
            public Action consume(Msg msg, ConsumeContext consumeContext) {
                msgCount.incrementAndGet();
                System.out.println(
                        "topic=" + msg.getTopic() + ",body=" + new String(msg.getBody()) +
                                ",tags=" + msg.getTags() + ",msgKey=" + msg.getKey() + ",msgCount=" + msgCount.get());
                try {
                    return Action.CommitMessage;
                } catch (Exception e) {
                    return Action.ReconsumeLater;
                }
            }
        });
        // 启动消费者，开始消费
        consumer.start();
        System.out.println("consumer started");
    }
}
