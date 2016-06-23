package com.gome.rocketmq.example.tyl.cluster;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.gome.rocketmq.common.MyUtils;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @author: tianyuliang
 * @since: 2016/6/22
 */
public class ClusterProducer {
    public static void main(String[] args) {
        try {
            DefaultMQProducer producer = new DefaultMQProducer(MyUtils.getDefaultCluster());
            producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            producer.start();
            int sendOneTime = 10;
            SendResult sendResult = null;
            Message message = null;
            for (int i = 0; i < sendOneTime; i++) {
                message = new Message("broadcastTopicTest", "tagA", ("data" + i).getBytes());
                sendResult = producer.send(message);
                if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                    System.out.println("send msg. topic=" + message.getTopic() + ",msgId=" + sendResult.getMsgId() + ",queueId=" + sendResult.getMessageQueue().getQueueId() + ",offset=" + sendResult.getQueueOffset());
                }
            }
            producer.shutdown();
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
