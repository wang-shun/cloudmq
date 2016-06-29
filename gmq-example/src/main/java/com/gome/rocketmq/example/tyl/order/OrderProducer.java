package com.gome.rocketmq.example.tyl.order;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.gome.rocketmq.common.MyUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tianyuliang
 * @date 2016/6/28
 */
public class OrderProducer {

    static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static void main(String[] args) {
        try {
            AtomicLong success = new AtomicLong();
            DefaultMQProducer producer = new DefaultMQProducer(MyUtils.getDefaultCluster());
            producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            producer.start();
            int sendOneTime = 1000000000;
            SendResult sendResult = null;
            Message message = null;
            long begin = 0L;
            for (int i = 0; i < sendOneTime; i++) {
                message = new Message("orderTopicTest", "tagA", ("data" + i).getBytes());
                begin = System.currentTimeMillis();
                sendResult = producer.send(message);
                if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                    long diff = UtilAll.computeEclipseTimeMilliseconds(begin);
                    //System.out.println(format.format(Calendar.getInstance().getTime()) + "," + sendResult.getMessageQueue() + ",body=" + new String(message.getBody()) + ",success=" + success.incrementAndGet());
                    System.out.println(format.format(Calendar.getInstance().getTime()) + ",diff=" + diff + "ms," + sendResult.getMessageQueue() + ",body=" + new String(message.getBody()) + ",success=" + success.incrementAndGet());
                } else {
                    System.out.println("error: " + sendResult);
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
