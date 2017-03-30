package com.cloudzone.cloudmq.demo.dilatation;

import com.cloudzone.cloudmq.api.open.base.*;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;
import com.cloudzone.cloudmq.common.PropertiesConst;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class DilatationOldTopicConsumer {


    public static void main(String[] args) {
        final String topicName = args.length >= 1 ? args[0] : "b-300";
        final String tag = args.length >= 2 ? args[1] : "A";
        final String group = args.length >= 3 ? args[2] : "testConsumer";

        final AtomicLong msgCount = new AtomicLong(0L);
        Properties properties = new Properties();
        properties.put(PropertiesConst.Keys.ConsumerGroupId, group);
        properties.put(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY,
                "jcpt-t-500:062489eca3c394a15b70fe500cdd942a0;a-200:2130d474c2c6a487eae496ee22dfbb878;b-300:" +
                        "38b60e940b83745a994a99520a93b295f;aa-800:1d2ac86e038cf4f058d59d144aef8065f");
        Consumer consumer = MQFactory.createConsumer(properties);

        // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，
        // 以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
        consumer.subscribe(topicName, tag, new MsgListener() {
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
