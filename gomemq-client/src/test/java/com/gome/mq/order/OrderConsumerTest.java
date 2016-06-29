package com.gome.mq.order;

import java.util.Properties;

import com.gome.api.open.base.Message;
import com.gome.api.open.factory.MQFactory;
import com.gome.api.open.order.ConsumeOrderContext;
import com.gome.api.open.order.MessageOrderListener;
import com.gome.api.open.order.OrderAction;
import com.gome.api.open.order.OrderConsumer;
import com.gome.common.PropertiesConst;
import com.gome.mq.MyProperties;


/**
 * 顺序消息的消费者者（顺序消息的消费者与普通消费者使用方法一致）
 *
 * @author tantexian
 * @since 2016/6/27
 */
public class OrderConsumerTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的ConsumerId
        properties.put(PropertiesConst.Keys.ConsumerId, "OrderConsumerTest-Id");
        // 不设置则默认为127.0.0.1:9876
        properties.put(PropertiesConst.Keys.NAMESRV_ADDR, MyProperties.getNameServerAddr());

        // 创建顺序类型消费者（建议尽量使用常规模式，顺序类型会降低性能及可靠性）
        OrderConsumer orderedConsumer = MQFactory.createOrderedConsumer(properties);

        // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
        orderedConsumer.subscribe("TopicOrderTestMQ", "*", new MessageOrderListener() {
            @Override
            public OrderAction consume(Message msg, ConsumeOrderContext consumeOrderContext) {
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
