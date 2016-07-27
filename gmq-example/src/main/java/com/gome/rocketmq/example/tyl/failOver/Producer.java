package com.gome.rocketmq.example.tyl.failOver;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.gome.rocketmq.common.MyUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: tianyuliang
 * @since: 2016/7/26
 */
public class Producer {

    public static void main(String[] args) {
        try {
            DefaultMQProducer producer = new DefaultMQProducer(MyUtils.getDefaultCluster());
            producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            producer.start();
            int sendOneTime = 100;
            String tags = null;
            SendResult sendResult = null;
            AtomicLong success = new AtomicLong(0L);
            Message message = null;
            for (int i = 0; i < sendOneTime; i++) {
                if (i % 3 == 0) {
                    tags = "tagA";
                } else if (i % 3 == 1) {
                    tags = "tagB";
                } else {
                    tags = "tagC";
                }
                message = new Message("haTopicTest", tags, ("data---" + i).getBytes());
                sendResult = producer.send(message);
                if (sendResult.getSendStatus() == SendStatus.SEND_OK || sendResult.getSendStatus() == SendStatus.SLAVE_NOT_AVAILABLE) {
                    System.out.println("send success. topic=" + message.getTopic() + ",success=" + success.incrementAndGet() + ",body=" + new String(message.getBody()) + ",brokerName=" + sendResult.getMessageQueue().getBrokerName());
                }
            }
        } catch (MQClientException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
