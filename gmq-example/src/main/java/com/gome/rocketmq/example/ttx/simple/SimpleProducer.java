package com.gome.rocketmq.example.ttx.simple;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.gome.rocketmq.common.MyUtils;

import java.util.Date;


/**
 * producer，发送消息
 * 
 */
public class SimpleProducer {
    public static void main(String[] args) throws MQClientException, InterruptedException {
        
        DefaultMQProducer producer = new DefaultMQProducer("SimpleProducer");
        
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());

        producer.start();

        try {
            // 订单消息A
            Message msg = new Message("SimpleOrderTopic", // topic
                "TagA", // tag
                ("订单创建-步骤A --Time:" + new Date()).getBytes()// body
            );
            producer.send(msg, new SendCallback() {

                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println("onSuccess and sendResult = " + sendResult);
                    
                }
                
                
                @Override
                public void onException(Throwable e) {
                    System.out.println("onException");
                    
                }
            });
            
            
            // 订单消息B
            msg = new Message("SimpleOrderTopic", // topic
                "TagB", // tag
                ("订单付款-步骤B --Time:" + new Date()).getBytes()// body
            );
            SendResult sendResult = producer.send(msg);
            System.out.println(sendResult);
            
            // 订单消息C
            msg = new Message("SimpleOrderTopic", // topic
                "TagC", // tag
                ("订单完成-步骤C  --Time:" + new Date()).getBytes()// body
            );
            sendResult = producer.send(msg);
            System.out.println(sendResult);
            
            // 库存消息A
            msg = new Message("SimplePayTopic", // topic
                "TagA", // tag
                ("库存配送-步骤 A --Time:" + new Date()).getBytes()// body
            );
            sendResult = producer.send(msg);
            System.out.println(sendResult);
            
            // 测试消息消费完成回调
            msg = new Message("RPCHookTopic", // topic
                "TagA", // tag
                ("消息消费完成回调- --Time:" + new Date()).getBytes()// body
            );
            sendResult = producer.send(msg);
            System.out.println(sendResult);
            
            // 测试SimplePullConsumer
            msg = new Message("SimplePullConsumerTopic", // topic
                "TagA", // tag
                ("SimplePullConsumer --Time:" + new Date()).getBytes()// body
            );
            sendResult = producer.send(msg);
            
            System.out.println(sendResult);
            
            

           /* System.setProperty(MixAll.NAMESRV_ADDR_PROPERTY, namesrvAddr);
            
            DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();
            defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
            
            defaultMQAdminExt.start();
            MessageExt msgExt = defaultMQAdminExt.viewMessage(sendResult.getMsgId());
            // 增加消息消费轨迹跟踪 add by tantexian 2016-6-2 08:53:03
            List<MessageTrack> messageTrackList = defaultMQAdminExt.messageTrackDetail(msgExt);
            System.out.println("messageTrackList == " + messageTrackList);*/
            
            
        }
        catch (Exception e) {
            e.printStackTrace();
            Thread.sleep(1000);
        }

        System.out.println("消息发送完毕！！！");
        producer.shutdown();
        
        
    }
}
