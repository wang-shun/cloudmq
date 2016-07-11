package com.gome.rocketmq.example.tyl.retryConsumer;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.gome.rocketmq.common.MyUtils;

/**
 * @author tianyuliang
 * @since 2016/7/11
 */
public class Producer {

    public static void main(String[] args) {
        try {
            DefaultMQProducer producer = new DefaultMQProducer(MyUtils.getDefaultCluster());
            producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            producer.start();
            int sendOneTime = 1;
            SendResult sendResult = null;
            Message message = null;
            for (int i = 0; i < sendOneTime; i++) {
                message = new Message("retryDemoTopic", "tagA", ("data-" + i).getBytes());
                sendResult = producer.send(message);
                if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                    System.out.println("send success. topic=" + message.getTopic() + ",msgId=" + sendResult.getMsgId() + ",body=" + new String(message.getBody()));
                }
            }
            System.out.println("send message end. total=" + sendOneTime);
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
