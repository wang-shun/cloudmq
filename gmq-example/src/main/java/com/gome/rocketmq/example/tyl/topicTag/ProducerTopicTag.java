package com.gome.rocketmq.example.tyl.topicTag;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.gome.rocketmq.common.MyUtils;

/**
 * producer topic for topicTag
 *
 * @author tianyuliang
 * @date 2016/6/22
 */
public class ProducerTopicTag {
    public static void main(String[] args) {
        try {
            DefaultMQProducer producer = new DefaultMQProducer(MyUtils.getDefaultCluster());
            producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            producer.start();
            int sendOneTime = 5;
            String tags = null;
            SendResult sendResult = null;

            Message message = null;
            for (int i = 0; i < sendOneTime; i++) {
                if (i % 3 == 0) {
                    tags = "tagA";
                } else if (i % 3 == 1) {
                    tags = "tagB";
                } else {
                    tags = "tagC";
                }
                message = new Message("orderTopicTest", tags, ("data" + i).getBytes());
                sendResult = producer.send(message);
                if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                    System.out.println("send msg success. topic=" + message.getTopic() + ",tags=" + message.getTags() + ",body=" + new String(message.getBody()) + ",result=" + sendResult);
                }
            }

            /*for (int i = 0; i < sendOneTime; i++) {
                if (i % 3 == 0) {
                    tags = "tagA";
                } else if (i % 3 == 1) {
                    tags = "tagB";
                } else {
                    tags = "tagC";
                }
                for (int j = 0; j < 3; j++) {
                    message = new Message(("orderTopic00" + j), tags, ("data" + i).getBytes());
                    sendResult = producer.send(message);
                    if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                        System.out.println("send msg diff topic success. topic=" + message.getTopic() + ",tags=" + message.getTags() + ",body=" + new String(message.getBody()));
                    }
                }
            }*/
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
