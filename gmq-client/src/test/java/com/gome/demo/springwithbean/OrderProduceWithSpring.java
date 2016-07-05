package com.gome.demo.springwithbean;

import com.gome.api.open.order.OrderProducer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gome.api.open.base.Msg;
import com.gome.api.open.base.SendResult;
import com.gome.api.open.exception.GomeClientException;


/**
 * 顺序消息的生产者（顺序消息的消费者与普通消费者使用方法一致）
 *
 * @author tantexian
 * @since 2016/6/27
 */
public class OrderProduceWithSpring {
    public static void main(String[] args) {
        /**
         * 生产者Bean配置在producer.xml中,可通过ApplicationContext获取或者直接注入到其他类(
         * 比如具体的Controller)中.
         */
        ApplicationContext context = new ClassPathXmlApplicationContext("orderproducer.xml");
        // 获取顺序消费者Bean
        OrderProducer orderProducer = (OrderProducer) context.getBean("orderProducer");
        assert orderProducer != null;
        // 循环发送消息
        for (int i = 0; i < 10; i++) {
            Msg msg = new Msg( //
                // Msg Topic
                "TopicTestMQ",
                // Msg Tag 可理解为Gmail中的标签，对消息进行再归类，方便Consumer指定过滤条件在MQ服务器过滤
                "TagA",
                // Msg Body 可以是任何二进制形式的数据， MQ不做任何干预，
                // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                ("Hello MQ " + i).getBytes());
            // 设置代表消息的业务关键属性，请尽可能全局唯一。（例如订单ID）。
            // 以方便您在无法正常收到消息情况下，可通过MQ 控制台查询消息并补发。
            // 注意：不设置也不会影响消息正常收发
            msg.setKey("ORDERID_10");
            // 发送消息，只要不抛异常就是成功
            // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
            try {
                /*
                 * 顺序发送的顺序消息的shardingKey值必须相同。由于是顺序消息，因此只能选择一个queue生产和消费消息
                 * shardingKey用来随机获取集群中的一个queue（可以自由设置该值，建议此处尽可能唯一，
                 * 便于消息队列分散到不同的queue上）
                 */
                String shardingKey = "orderProducerShardingKey125";

                SendResult sendResult = orderProducer.send(msg, shardingKey);
                assert sendResult != null;
                System.out.println("send success: " + sendResult.getMsgId()
                        + ",offset=" + sendResult.getQueueOffset()
                        + ",brokerName=" + sendResult.getMessageQueue().getBrokerName()
                        + ",queueId=" + sendResult.getMessageQueue().getQueueId());
            }
            catch (GomeClientException e) {
                e.printStackTrace();
                System.out.println("发送失败");
            }
        }
        System.out.println("OrderWithSpring send message end.");
        System.exit(0);
    }
}
