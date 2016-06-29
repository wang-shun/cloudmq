package com.gome.mq.order;

import java.util.Properties;

import com.gome.api.open.base.Message;
import com.gome.api.open.base.SendResult;
import com.gome.api.open.factory.MQFactory;
import com.gome.api.open.order.OrderProducer;
import com.gome.common.PropertiesConst;
import com.gome.mq.MyProperties;


/**
 * 顺序消息的生产者（顺序消息的消费者与普通消费者一致）
 *
 * @author tantexian
 * @since 2016/6/27
 */
public class OrderProducerTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的ProducerId
        properties.put(PropertiesConst.Keys.ProducerId, "OrderProducerTest-Id");
        // 不设置则默认为127.0.0.1:9876
        properties.put(PropertiesConst.Keys.NAMESRV_ADDR, MyProperties.getNameServerAddr());

        // 创建顺序类型生产者（建议尽量使用常规模式，顺序类型会降低性能及可靠性）
        OrderProducer orderProducer = MQFactory.createOrderProducer(properties);
        // 在发送消息前，必须调用start方法来启动Producer，只需调用一次即可。
        orderProducer.start();
        // 循环发送消息
        for (int i = 0; i < 10; i++) {
            Message msg = new Message( //
                // Message Topic
                "TopicOrderTestMQ",
                // Message Tag 可理解为Gmail中的标签，对消息进行再归类，方便Consumer指定过滤条件在MQ服务器过滤
                "TagA",
                // Message Body 可以是任何二进制形式的数据， MQ不做任何干预，
                // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                ("Hello MQ, I'm message " + i).getBytes());
            // 设置代表消息的业务关键属性，请尽可能全局唯一（例如订单ID）。
            // 以方便您在无法正常收到消息情况下，可通过MQ控制台查询消息并补发。
            // 注意：不设置也不会影响消息正常收发
            msg.setKey("ORDERID_" + i);

            // 由于是顺序消息，因此只能选择一个queue生产和消费消息
            // shardingKey用来随机获取集群中的一个queue（可以自由设置该值，建议此处尽可能唯一，便于消息队列分散到不同的queue上）
            String shardingKey = "OrderProducerTestShardingKey";
            // 发送消息，只要不抛异常就是成功
            // 建议业务程序自行记录生产及消费log日志，以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
            SendResult sendResult = orderProducer.send(msg, shardingKey);
            System.out.println(sendResult);
        }
        // 在应用退出前，销毁Producer对象
        // 注意：如果不销毁也没有问题
        orderProducer.shutdown();
    }
}
