package com.gome.rocketmq.example.ttx.order;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.MQProducer;
import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.remoting.exception.RemotingException;

import java.util.List;
import java.util.Random;


/**
 * producer，发送顺序消息
 */
public class OrderMsgProducer {
    public static void main(String[] args) {
        try {
            MQProducer producer = new DefaultMQProducer("OrderMsg_producer");

            producer.start();
            
            int orderId = 1;
            String topic = "orderTopic2";
            String key = orderId + "";
        
            String body = null;
            String tag = null;
            Message message = null;
            SendResult sendResult = null;
            for (int i = 0; i < 10000; i++) {
                body = "创建订单-第" + i + "步";
                tag ="TagA";
                message = new Message(topic, tag, key, body.getBytes());
                sendResult = producer.send(message, new MessageQueueSelector() {
                    
                    @Override
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                        Integer id = (Integer) arg;
                        int index = id % mqs.size();
                        return mqs.get(index);
                    }
                }, orderId);
                System.out.println(sendResult);
                Thread.sleep(new Random().nextInt(5000));
            }

            System.out.println("订单创建消息发送完成！！！");
            
            producer.shutdown();
        }
        catch (MQClientException e) {
            e.printStackTrace();
        }
        catch (RemotingException e) {
            e.printStackTrace();
        }
        catch (MQBrokerException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
