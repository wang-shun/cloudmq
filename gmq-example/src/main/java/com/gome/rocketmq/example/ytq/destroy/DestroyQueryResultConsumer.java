package com.gome.rocketmq.example.ytq.destroy;

import com.alibaba.rocketmq.client.QueryResult;
import com.alibaba.rocketmq.client.consumer.DefaultMQPullConsumer;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.gome.rocketmq.common.MyUtils;

/**
 * Created by yintongjiang on 2016/7/18.
 */
public class DestroyQueryResultConsumer {
    private final static String group = "destroyConsumerGroup";
    private final static String topicName = "destroyTopic_14";

    public static void main(String[] args) throws MQClientException, InterruptedException {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(group);
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.start();
        QueryResult queryResult = consumer.queryMessage(topicName, "msgId:111", 10, 1000000L, System.currentTimeMillis());
        System.out.println("#####queryResult=" + queryResult);
        consumer.shutdown();
    }
}
