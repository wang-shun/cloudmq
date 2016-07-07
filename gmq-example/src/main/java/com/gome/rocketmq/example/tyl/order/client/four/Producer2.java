package com.gome.rocketmq.example.tyl.order.client.four;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.gome.rocketmq.common.MyUtils;

import java.util.List;

/**
 * @author tianyuliang
 * @date 2016/7/1
 */
public class Producer2 {


    private static final String topic = "orderTopic_4";
    private static final String groupName = "FourProducerGroup";

    public static void main(String[] args) {
        try {
            Integer orderId = 27; // 确保两个prodcuer端的的数据可以发到不相同的queue
            DefaultMQProducer producer = new DefaultMQProducer(groupName);
            producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            producer.start();
            System.out.println("producerGroup=" + groupName + ", instanceName=" + producer.getInstanceName() + ",orderId=" + orderId + ", producer started.");
            int sendOneTime = 10;
            SendResult result = null;
            Message message = null;
            for (int i = 0; i < sendOneTime; i++) {
                message = new Message(topic, "B", ("two-B-" + i).getBytes());
                result = producer.send(message, new MessageQueueSelector() {
                    @Override
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                        Integer id = (Integer) arg;
                        int index = id % mqs.size();
                        return mqs.get(index);
                    }
                }, orderId);
                if (result.getSendStatus() == SendStatus.SEND_OK) {
                    System.out.println("topic=" + message.getTopic() + ",offset=" + result.getQueueOffset()
                            + ",queueId=" + result.getMessageQueue().getQueueId() + ",brokerName=" + result.getMessageQueue().getBrokerName()
                            + ",msgId=" + result.getMsgId() + ",body=" + new String(message.getBody()));
                }
                sleepTime(1);
            }
            System.out.println("send message end. total=" + sendOneTime);
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

    private static void sleepTime(int second) {
        try {
            Thread.sleep(second * 1000L);
        } catch (Exception e) {
            System.out.println("error:" + e.getMessage());
            e.printStackTrace();
        }
    }

}
