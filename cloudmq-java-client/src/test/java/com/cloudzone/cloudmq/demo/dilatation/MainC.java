package com.cloudzone.cloudmq.demo.dilatation;

import com.alibaba.rocketmq.client.producer.SendResult;
import com.cloudzone.cloudmq.api.open.base.*;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;
import com.cloudzone.cloudmq.common.PropertiesConst;

import java.util.Properties;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author yintongjiang
 * @params
 * @since 2017/4/12
 */
public class MainC {
    public static void main(String[] args) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String topicName = "jcpt-cloudmq-topic-test-800";
                final String tag = "A";
                final String group = "testConsumer";

                final AtomicLong msgCount = new AtomicLong(0L);
                Properties properties = new Properties();
                properties.put(PropertiesConst.Keys.ConsumerGroupId, group);
                properties.put(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY,
                        "jcpt-cloudmq-topic-test-800:0dbb92c23bc3a454fa267cf34bff9b37b");
                Consumer consumer = MQFactory.createConsumer(properties);

                consumer.subscribe(topicName, tag, new MsgListener() {
                    @Override
                    public Action consume(Msg msg, ConsumeContext consumeContext) {
                        try {
                            return Action.CommitMessage;
                        } catch (Exception e) {
                            return Action.ReconsumeLater;
                        }
                    }
                });
//                consumer.subscribe("jcpt-cloudmq-topic-test-800", tag, new MsgListener() {
//                    @Override
//                    public Action consume(Msg msg, ConsumeContext consumeContext) {
//                        try {
//                            return Action.CommitMessage;
//                        } catch (Exception e) {
//                            return Action.ReconsumeLater;
//                        }
//                    }
//                });
                // 启动消费者，开始消费
                consumer.start();
                }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String topicName = "jcpt-example-800";
                final String tag = "A";
                final String group = "testConsumer123";

                final AtomicLong msgCount = new AtomicLong(0L);
                Properties properties = new Properties();
                properties.put(PropertiesConst.Keys.ConsumerGroupId, group);
                properties.put(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY,
                        "jcpt-example-800:04684fc825ae54456807d51194ab41714");
                Consumer consumer = MQFactory.createConsumer(properties);

                consumer.subscribe(topicName, tag, new MsgListener() {
                    @Override
                    public Action consume(Msg msg, ConsumeContext consumeContext) {
                        try {
                            return Action.CommitMessage;
                        } catch (Exception e) {
                            return Action.ReconsumeLater;
                        }
                    }
                });
//                consumer.subscribe("jcpt-cloudmq-topic-test-800", tag, new MsgListener() {
//                    @Override
//                    public Action consume(Msg msg, ConsumeContext consumeContext) {
//                        try {
//                            return Action.CommitMessage;
//                        } catch (Exception e) {
//                            return Action.ReconsumeLater;
//                        }
//                    }
//                });
                // 启动消费者，开始消费
                consumer.start();
            }
        }).start();


    }
}
