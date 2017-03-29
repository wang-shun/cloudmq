package com.cloudzone.cloudmq.demo.transaction;

import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.base.TransactionSendResult;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;
import com.cloudzone.cloudmq.api.open.transaction.LocalTransactionChecker;
import com.cloudzone.cloudmq.api.open.transaction.LocalTransactionExecuter;
import com.cloudzone.cloudmq.api.open.transaction.TransactionProducer;
import com.cloudzone.cloudmq.common.PropertiesConst;

import java.util.Properties;

/**
 * @author leiyuanjie
 * @since 2017-02-17.
 */
public class TransactionProducerTest {

    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的生产者组ID（ProducerGroupId）
        properties.put(PropertiesConst.Keys.ProducerGroupId, "TransactionProducerGroupId-test");
        properties.put(PropertiesConst.Keys.TOPIC_NAME, "bbb-800");
        properties.put(PropertiesConst.Keys.AUTH_KEY, "493e507a4fedb4dce923ddd030a7649a5");

        //LocalTransactionChecker: 服务器回查客户端
        LocalTransactionChecker checker = new LocalTransactionCheckerImpl();
        //LocalTransactionExecuter: 执行本地事务
        LocalTransactionExecuter executer = new LocalTransactionExecuterImpl();

        //获取 TransactionProducer 对象
        TransactionProducer transactionProducer = MQFactory.createTransactionProducer(properties, checker);
        //在发送消息前必须调用start方法来启动生产者
        transactionProducer.start();
        // 循环发送消息
        for (int i = 0; i < 10; i++) {
            Msg msg = new Msg(
                    // Msg Topic
                    "bbb-800",
                    // Msg Tag 可理解为Gmail中的标签，对消息进行再归类，
                    // 方便Consumer指定过滤条件在MQ服务器过滤
                    "TagA",
                    // Msg Body 可以是任何二进制形式的数据， MQ不做任何干预，
                    // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                    ("hello transaction " + i).getBytes());

            // 设置代表消息的业务关键属性，请尽可能全局唯一。（例如订单ID）。
            // 以方便您在无法正常收到消息情况下，可通过MQ控制台查询消息并补发。
            // 注意：不设置也不会影响消息正常收发
            msg.setKey("transaction_" + i);

            // 发送消息，只要不抛异常就是成功
            // 建议业务程序自行记录生产及消费log日志，
            // 以方便您在无法正常收到消息情况下，
            // 可通过MQ控制台或者MQ日志查询消息并补发。
            try {
                TransactionSendResult transactionSendResult = transactionProducer.send(msg, executer, null);
                System.out.println("send success, msgId=" + transactionSendResult.getMsgId());
            } catch (Exception e) {
                System.out.println("send error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("transaction producer send end.");
        // 在应用退出前，销毁TransactionProducer对象
        // 注意：如果不销毁也没有问题
        transactionProducer.shutdown();
    }
}
