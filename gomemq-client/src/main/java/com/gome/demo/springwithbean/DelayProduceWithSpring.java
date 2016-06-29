package com.gome.demo.springwithbean;

import com.gome.common.DelayLevelConst;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gome.api.open.base.Message;
import com.gome.api.open.base.Producer;
import com.gome.api.open.base.SendResult;
import com.gome.api.open.exception.GomeClientException;

import java.util.Date;


/**
 * 延时消费类型生产者(延时消费的消费者与普通消费者一致，延时消费生产者与普通生产者一致，只是在发送消息时，增加延时等级即可)
 *
 * @author tantexian
 * @since 2016/6/27
 */
public class DelayProduceWithSpring {
    public static void main(String[] args) {
        /**
         * 生产者Bean配置在producer.xml中,可通过ApplicationContext获取或者直接注入到其他类(
         * 比如具体的Controller)中.
         */
        ApplicationContext context = new ClassPathXmlApplicationContext("producer.xml");
        // 获取普通消费者Bean
        Producer producer = (Producer) context.getBean("producer");
        // 循环发送消息
        for (int i = 0; i < 10; i++) {
            Message msg = new Message( //
                // Message Topic
                "TopicTestMQ",
                // Message Tag 可理解为Gmail中的标签，对消息进行再归类，方便Consumer指定过滤条件在MQ服务器过滤
                "TagA",
                // Message Body 可以是任何二进制形式的数据， MQ不做任何干预，
                // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                (" {MsgBornTime: " + new Date() + "} {MsgBody: Hello MQ " + i + "}").getBytes());
            // 设置代表消息的业务关键属性，请尽可能全局唯一。（例如订单ID）。
            // 以方便您在无法正常收到消息情况下，可通过MQ 控制台查询消息并补发。
            // 注意：不设置也不会影响消息正常收发
            msg.setKey("ORDERID_10");

            // 延时模式（建议尽量使用常规模式，延时模式会降低性能及可靠性）
            // deliver time level 为延时等级（当前版本只支持固定的延时等级）,具体值参考DelayLevelConst枚举类，
            // 指定一个延时等级，在这个等级延时时刻之后才能被消费，这个例子表示 10s 后才能被消费
            msg.setDelayTimeLevel(DelayLevelConst.TenSecond.val());

            // 发送消息，只要不抛异常就是成功
            // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
            try {
                SendResult sendResult = producer.send(msg);
                assert sendResult != null;
                System.out.println("send success : " + sendResult.getMsgId());
            }
            catch (GomeClientException e) {
                System.out.println("发送失败");
            }
        }
        System.exit(0);
    }
}
