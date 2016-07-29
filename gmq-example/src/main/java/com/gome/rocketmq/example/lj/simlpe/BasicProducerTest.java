package com.gome.rocketmq.example.lj.simlpe;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.gome.rocketmq.example.lj.common.MyUtils;

/**
 * @author jerrylou
 * @params
 * @since 2016/7/21 0021
 */
public class BasicProducerTest {


    public static void main(String[] args) throws MQClientException {

        DefaultMQProducer producer = new DefaultMQProducer(MyUtils.getProducerGroup());
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        producer.start();

        try {
            for (int i = 0; i < 10; i++) {
                final Message message = new Message(MyUtils.getTopicName(),
                        MyUtils.getTags(), ("hello: " + i).getBytes());
                message.setKeys("s" + i);
                SendResult sendResult = producer.send(message);
                System.out.println(sendResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("basic producer send end.");
        producer.shutdown();
    }

}