package com.gome.demo.springwithbean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gome.api.open.exception.GomeClientException;
import com.gome.api.open.base.Message;
import com.gome.api.open.base.Producer;
import com.gome.api.open.base.SendResult;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class ProduceWithSpring {
    public static void main(String[] args) {
        /**
         * 生产者Bean配置在producer.xml中,可通过ApplicationContext获取或者直接注入到其他类(比如具体的Controller)中.
         */
        ApplicationContext context = new ClassPathXmlApplicationContext("producer.xml");
        // 获取普通消费者Bean
        Producer producer = (Producer) context.getBean("producer");
        //循环发送消息
        for (int i = 0; i < 10; i++) {
            Message msg = new Message( //
                    // Message Topic
                    "TopicTestMQ",
                    // Message Tag 可理解为Gmail中的标签，对消息进行再归类，方便Consumer指定过滤条件在MQ服务器过滤
                    "TagA",
                    // Message Body 可以是任何二进制形式的数据， MQ不做任何干预，
                    // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                    ("Hello MQ " + i).getBytes());
            // 设置代表消息的业务关键属性，请尽可能全局唯一。（例如订单ID）。
            // 以方便您在无法正常收到消息情况下，可通过MQ 控制台查询消息并补发。
            // 注意：不设置也不会影响消息正常收发
            msg.setKey("ORDERID_100");
            // 发送消息，只要不抛异常就是成功
            // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
            try {
                SendResult sendResult = producer.send(msg);
                assert sendResult != null;
                System.out.println("send success: " + sendResult.getMsgId());
            }catch (GomeClientException e) {
                System.out.println("发送失败");
            }
        }
        System.exit(0);
    }
}
