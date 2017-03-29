package com.cloudzone.cloudmq.demo.springwithbean;

import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.base.TransactionSendResult;
import com.cloudzone.cloudmq.api.open.transaction.LocalTransactionExecuter;
import com.cloudzone.cloudmq.api.open.transaction.TransactionProducer;
import com.cloudzone.cloudmq.demo.transaction.LocalTransactionExecuterImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author leiyuanjie
 * @since 2017-02-17.
 */
public class TransactionProducerWithSpring {
    public static void main(String[] args) {
        /**
         * 生产者Bean配置在transactionProducer.xml中,可通过ApplicationContext获取
         * 或者直接注入到其他类(比如具体的Controller)中.
         * 如果项目本身已经集成spring、则直接使用项目已有的spring配置bean、
         * 获取bean方式，不需要使用下述ClassPathXmlApplicationContext类方法来获取bean
         */
        ApplicationContext context = new ClassPathXmlApplicationContext("transactionProducer.xml");
        // 获取事务生产者Bean
        TransactionProducer transactionProducer = (TransactionProducer) context.getBean("transactionProducer");
        assert transactionProducer != null;
        //LocalTransactionExecuter: 执行本地事务
        LocalTransactionExecuter executer = new LocalTransactionExecuterImpl();
        //循环发送消息
        for (int i = 0; i < 10; i++) {
            Msg msg = new Msg( //
                    // Msg Topic
                    "TransactionTopicTestMQ",
                    // Msg Tag 可理解为Gmail中的标签，对消息进行再归类，
                    // 方便Consumer指定过滤条件在MQ服务器过滤
                    "TagA",
                    // Msg Body 可以是任何二进制形式的数据， MQ不做任何干预，
                    // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                    ("(TransactionProducerWithSpring) Hello message " + i).getBytes());

            // 设置代表消息的业务关键属性，请尽可能全局唯一。（例如订单ID）。
            // 以方便您在无法正常收到消息情况下，可通过MQ 控制台查询消息并补发。
            // 注意：不设置也不会影响消息正常收发
            msg.setKey("transactionProducer_" + i);

            // 发送消息，只要不抛异常就是成功
            // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，
            // 以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
            try {
                TransactionSendResult transactionSendResult = transactionProducer.send(msg, executer, null);
                System.out.println("send success, msgId=" + transactionSendResult.getMsgId());
            } catch (Exception e) {
                System.out.println("send error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("TransactionProducerWithSpring send message end.");
        System.exit(0);
    }

}
