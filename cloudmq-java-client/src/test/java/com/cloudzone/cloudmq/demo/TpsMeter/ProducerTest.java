package com.cloudzone.cloudmq.demo.TpsMeter;

import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.base.Producer;
import com.cloudzone.cloudmq.api.open.base.SendResult;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;
import com.cloudzone.cloudmq.common.PropertiesConst;

import java.util.Properties;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public class ProducerTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的生产者组 ID（ ProducerGroupId）
        properties.put(PropertiesConst.Keys.ProducerGroupId, "SimpleProducerGroupId-test");
        // TOPIC_NAME_AND_AUTH_KEY 的值, 即为 Topic 和 AuthKey 的键值对
        properties.put(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY,
                "ttx-test-tps-200:0592547a64ceb483b9173da139440a53e");
        Producer producer = MQFactory.createProducer(properties);
        // 在发送消息前，必须调用 start 方法来启动 Producer，只需调用一次即可。
        producer.start();
        // 循环发送消息
        for (int i = 0; i < 10; i++) {

            Msg msg = new Msg(
                    // Msg Topic
                    "ttx-test-tps-200",
                    // Msg Tag 可理解为 Gmail 中的标签，对消息进行再归类，方便 Consumer 指定过滤条件在 MQ 服务器过滤
                    "TagA",
                    // Msg Body 可以是任何二进制形式的数据， MQ 不做任何干预，
                    // 需要 Producer 与 Consumer 协商好一致的序列化和反序列化方式
                    ("Hello MQ " + i).getBytes());
            // 设置代表消息的业务关键属性，请尽可能全局唯一。（例如订单 ID）。
            // 以方便您在无法正常收到消息情况下，可通过 MQ 控制台查询消息并补发。
            // 注意：不设置也不会影响消息正常收发
            msg.setKey("ORDERID_" + i);
            // 发送消息，只要不抛异常就是成功
            // 建议业务程序自行记录生产及消费 log 日志，
            // 以方便您在无法正常收到消息情况下，可通过 MQ 控制台或者 MQ 日志查询消息并补发。
            SendResult sendResult = producer.send(msg);
            System.out.println(sendResult);
        }
        System.out.println("simple producer send end.");
        // 在应用退出前，销毁 Producer 对象
        // 注意：如果不销毁也没有问题
        producer.shutdown();
    }
}