package com.gome.mq.delay;

import java.util.Date;
import java.util.Properties;
import java.util.Random;

import com.gome.api.open.base.Message;
import com.gome.api.open.base.Producer;
import com.gome.api.open.base.SendResult;
import com.gome.api.open.factory.MQFactory;
import com.gome.common.DelayLevelConst;
import com.gome.common.PropertiesConst;


/**
 * @author tantexian
 * @since 2016/6/28
 */
public class ProducerDelayTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的ProducerId
        properties.put(PropertiesConst.Keys.ProducerId, "ProducerDelayTest");
        // 不设置则默认为127.0.0.1:9876
        properties.put(PropertiesConst.Keys.NAMESRV_ADDR, "127.0.0.1:9876");

        Producer producer = MQFactory.createProducer(properties);
        // 在发送消息前，必须调用start方法来启动Producer，只需调用一次即可。
        producer.start();
        // 循环发送消息
        for (int i = 0; i < 10; i++) {
            // 随机0-10s中延时设置
            try {
                Thread.sleep(new Random().nextInt(10)*1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message msg = new Message( //
                // Message Topic
                "TopicTestMQ",
                // Message Tag 可理解为Gmail中的标签，对消息进行再归类，方便Consumer指定过滤条件在MQ服务器过滤
                "TagA",
                // Message Body 可以是任何二进制形式的数据， MQ不做任何干预，
                // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                (" {MsgBornTime: " + new Date() + "} {MsgBody: Hello MQ " + i + "}").getBytes());
            // 设置代表消息的业务关键属性，请尽可能全局唯一。（例如订单ID）。
            // 以方便您在无法正常收到消息情况下，可通过MQ控制台查询消息并补发。
            // 注意：不设置也不会影响消息正常收发
            msg.setKey("ORDERID_" + i);

            // deliver time level 为延时等级（当前版本只支持固定的延时等级）,具体值参考DelayLevelConst枚举类，
            // 指定一个延时等级，在这个等级延时时刻之后才能被消费，这个例子表示 10s 后才能被消费
            msg.setDelayTimeLevel(DelayLevelConst.TenSecond.val());

            // 发送消息，只要不抛异常就是成功
            // 建议业务程序自行记录生产及消费log日志，以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
            SendResult sendResult = producer.send(msg);
            System.out.println(sendResult);
        }
        // 在应用退出前，销毁Producer对象
        // 注意：如果不销毁也没有问题
        producer.shutdown();
    }
}
