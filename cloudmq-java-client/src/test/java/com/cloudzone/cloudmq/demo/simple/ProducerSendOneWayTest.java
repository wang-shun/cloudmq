package com.cloudzone.cloudmq.demo.simple;

import java.util.Properties;

import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.base.Producer;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;
import com.cloudzone.cloudmq.common.PropertiesConst;

/**
 * sendOneway模式：只管单边发送。由于没有返回消息状态结果逻辑，因此吞吐量及性能相对于send有较大提高。
 * 因此适用于可靠性要求不高，但是吞吐量性能要求很高的业务场景（例如：日志收集处理场景）
 *
 * @author tantexian
 * @since 2016/6/27
 */
public class ProducerSendOneWayTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的生产者组ID（ProducerGroupId）
        properties.put(PropertiesConst.Keys.ProducerGroupId, "SendOneWayProducerGroupId-test");
        // 设置topic名称和认证key
        properties.put(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY,
                "CloudTopicTest-200:02865ea17c4eb4186854ab95bdc07f842");

        Producer producer = MQFactory.createProducer(properties);
        // 在发送消息前，必须调用start方法来启动Producer，只需调用一次即可。
        producer.start();
        // 循环发送消息
        for (int i = 0; i < 10; i++) {
            Msg msg = new Msg( //
                // Msg Topic
                "CloudTopicTest-200",
                // Msg Tag 可理解为Gmail中的标签，对消息进行再归类，方便Consumer指定过滤条件在MQ服务器过滤
                "TagA",
                // Msg Body 可以是任何二进制形式的数据， MQ不做任何干预，
                // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                ("Hello MQ " + i).getBytes());

            // 设置代表消息的业务关键属性，请尽可能全局唯一。（例如订单ID）。
            // 以方便您在无法正常收到消息情况下，可通过MQ控制台查询消息并补发。
            // 注意：不设置也不会影响消息正常收发
            msg.setKey("ORDERID_" + i);

            // 发送消息，只要不抛异常就是成功
            // 建议业务程序自行记录生产及消费log日志，以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
            // sendOneway模式：适用于可靠性要求不高，但是吞吐量性能要求很高的业务场景（例如：日志收集处理场景）
            producer.sendOneway(msg);

        }
        System.out.println("send one way message end.");
        // 在应用退出前，销毁Producer对象
        // 注意：如果不销毁也没有问题
        producer.shutdown();
    }
}
