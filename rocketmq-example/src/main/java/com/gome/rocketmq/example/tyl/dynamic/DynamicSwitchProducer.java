package com.gome.rocketmq.example.tyl.dynamic;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.alibaba.rocketmq.store.CommitLog;
import com.gome.rocketmq.common.MyUtils;
import org.apache.commons.lang.time.FastDateFormat;

/**
 * @author tianyuliang
 * @date 2016/6/27
 */
public class DynamicSwitchProducer {

    public static void main(String[] args) {
        try {
            DefaultMQProducer producer = new DefaultMQProducer(MyUtils.getDefaultCluster());
            producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            producer.start();
            int sendOneTime = 20;
            SendResult result = null;
            Message message = null;
            for (int i = 0; i < sendOneTime; i++) {
                message = new Message("dynamicSwitchTopicTest", "A", ("data" + i).getBytes());
                result = producer.send(message);
                System.out.println("send msg success. topic=" + message.getTopic() + ",body=" + new String(message.getBody())
                        + ",offset=" + result.getQueueOffset()
                        + ",queueId=" + result.getMessageQueue().getQueueId() + ",brokerName=" + result.getMessageQueue().getBrokerName() + ",result=" + result.getSendStatus());

                if (result.getSendStatus() == SendStatus.SEND_OK) {
                 }
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
}
