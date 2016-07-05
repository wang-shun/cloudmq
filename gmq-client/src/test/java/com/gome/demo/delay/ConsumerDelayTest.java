package com.gome.demo.delay;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.gome.api.open.base.*;
import com.gome.api.open.factory.MQFactory;
import com.gome.common.PropertiesConst;
import com.gome.demo.simple.MyProperties;


/**
 * 延时消费类型消费者(延时消费的消费者与普通消费者一致)
 * 注意：为了保证消息队列性能，消息队列自身并不保证消息不会重复消费(在某些异常情况下偶尔会出现极少数重复消息)，
 * 若业务系统使用在非常严格的不允许消息重复的业务场景，则需要业务系统自身处理重复消息幂等
 *
 * @author tantexian
 * @since 2016/6/27
 */
public class ConsumerDelayTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的消费者组ID（ConsumerGroupId）
        properties.put(PropertiesConst.Keys.ConsumerGroupId, "DelayConsumerGroupId-test");
        // 设置nameserver地址，不设置则默认为127.0.0.1:9876
        properties.put(PropertiesConst.Keys.NAMESRV_ADDR, "127.0.0.1:9876");

        // 创建普通类型消费者
        Consumer consumer = MQFactory.createConsumer(properties);
        // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
        // 延时模式（建议尽量使用常规模式，延时模式会降低性能及可靠性）
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        consumer.subscribe("TopicTestMQ", "*", new MsgListener() {
            public Action consume(Msg msg, ConsumeContext context) {
                System.out.println("[NowTime:" + sdf.format(new Date())
                        + "] Receive Msg : " + new String(msg.getBody()));
                try {
                    // do something..
                    return Action.CommitMessage;
                } catch (Exception e) {
                    // 消费失败，返回ReconsumeLater，消息被放置到重试队列，延时后下次重新消费
                    return Action.ReconsumeLater;
                }
            }
        });
        // 启动消费者，开始消费
        consumer.start();
        System.out.println("ConsumerDelayTest consumer Started");
    }
}
