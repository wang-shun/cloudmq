package com.wish.rocketmq.example.ttx;

import java.util.Date;
import java.util.Random;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;


/**
 * Producer，发送消息
 * 
 */
public class ClusterProducer {
    public static void main(String[] args) throws MQClientException, InterruptedException {
        
        DefaultMQProducer producer = new DefaultMQProducer("ClusterProducergroup");
        
        producer.setNamesrvAddr(MyUtil.getNamesrvAddr());

        producer.start();

        try {
            for (int i = 0; i < 10; i++) {
                // 测试集群消费 2016/6/8 Add by tantexixan
                Message message = new Message("clusterTopic", // topic
                        "TagA", // tag
                        (i + "-" + new Random().nextInt(1000)+ "集群消费测试 --Time:" + new Date()).getBytes()// body
                );
                SendResult sendResult = producer.send(message);
                System.out.println(sendResult);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            Thread.sleep(1000);
        }

        System.out.println("消息发送完毕！！！");
        producer.shutdown();
        
        
    }
}
