package com.gome.demo.springwithbean;

import com.gome.api.open.base.Msg;
import com.gome.api.open.base.Producer;
import com.gome.api.open.base.SendResult;
import com.gome.api.open.exception.GomeClientException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author tianyuliang
 * @date 2016/7/8
 */
public class ProduceWithSpring {
    public static void main(String[] args) {
        /**
         * 生产者Bean配置在producer.xml中,可通过ApplicationContext获取或者直接注入到其他类(比如具体的Controller)中.
         * 如果项目本身已经集成spring、则直接使用项目已有的spring配置bean、获取bean方式，不需要使用下述ClassPathXmlApplicationContext类方法来获取bean
         */
        ApplicationContext context = new ClassPathXmlApplicationContext("producer.xml");
        // 获取普通生产者Bean
        Producer producer = (Producer) context.getBean("producer");
        assert producer != null;
        //循环发送消息
        for (int i = 0; i < 10; i++) {
            Msg msg = new Msg( //
                    // Msg Topic
                    "TopicTestMQ",
                    // Msg Tag 可理解为Gmail中的标签，对消息进行再归类，方便Consumer指定过滤条件在MQ服务器过滤
                    "A",
                    // Msg Body 可以是任何二进制形式的数据， MQ不做任何干预，
                    // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                    ("(ProduceWithSpring) Hello MQ " + i).getBytes());
            // 设置代表消息的业务关键属性，请尽可能全局唯一。（例如订单ID）。
            // 以方便您在无法正常收到消息情况下，可通过MQ 控制台查询消息并补发。
            // 注意：不设置也不会影响消息正常收发
            msg.setKey("ORDERID_100");
            // 发送消息，只要不抛异常就是成功
            // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
            try {
                SendResult sendResult = producer.send(msg);
                assert sendResult != null;
                System.out.println("send success. msgId=" + sendResult.getMsgId());
            } catch (GomeClientException e) {
                System.out.println("send error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("ProducerWithSpring send message end.");
        System.exit(0);
    }

}
