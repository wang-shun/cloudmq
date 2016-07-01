package com.gome.demo.order;

import java.util.Properties;

import com.gome.api.open.base.Msg;
import com.gome.api.open.factory.MQFactory;
import com.gome.api.open.order.ConsumeOrderContext;
import com.gome.api.open.order.MsgOrderListener;
import com.gome.api.open.order.OrderAction;
import com.gome.api.open.order.OrderConsumer;
import com.gome.common.PropertiesConst;
import com.gome.demo.simple.MyProperties;


/**
 * 顺序消息的消费者者（顺序消息的消费者与普通消费者使用方法一致）
 * 注意：为了保证消息队列性能，消息队列自身并不保证消息不会重复消费(在某些异常情况下偶尔会出现极少数重复消息)，
 * 若业务系统使用在非常严格的不允许消息重复的业务场景，则需要业务系统自身处理重复消息幂等
 *
 * @author tantexian
 * @since 2016/6/27
 */
public class OrderConsumerTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的ConsumerId
        properties.put(PropertiesConst.Keys.ConsumerId, "OrderConsumerTest-Id");
        // 设置nameserver地址，不设置则默认为127.0.0.1:9876
        properties.put(PropertiesConst.Keys.NAMESRV_ADDR, "127.0.0.1:9876");

        // 创建顺序类型消费者（建议尽量使用常规模式，顺序类型会降低性能及可靠性）
        OrderConsumer orderedConsumer = MQFactory.createOrderedConsumer(properties);

        // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
        orderedConsumer.subscribe("TopicOrderTestMQ", "*", new MsgOrderListener() {
            @Override
            public OrderAction consume(Msg msg, ConsumeOrderContext consumeOrderContext) {
                System.out.println(new String(msg.getBody()));
                try {
                    // do something..
                    return OrderAction.Success;
                }
                catch (Exception e) {
                    // 消费失败，Suspend，消息被放置到重试队列，延时后下次重新消费
                    return OrderAction.Suspend;
                }
            }
        });
        // 启动消费者，开始消费
        orderedConsumer.start();
        System.out.println("order consumer Started");
    }
}
