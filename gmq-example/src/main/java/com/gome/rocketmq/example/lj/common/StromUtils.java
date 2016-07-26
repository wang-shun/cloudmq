package com.gome.rocketmq.example.lj.common;

/**
 * @author jerrylou
 * @params
 * @since 2016/7/22 0022
 */
public class StromUtils {

    // 使用测试机环境 2016/7/22 0022 Add by jerrylou
    private final static String namesrvAddr =
            "10.128.31.103:9876;10.128.31.104:9876;10.128.31.105:9876;10.128.31.106:9876";
    private final static String topicName = "topic_storm_2";
    private final static String tags = "*";
    private final static String producerGroup = "producerStromGroup";
    private final static String consumerGroup = "consumerStromGroup";
    private final static int topicQueueNums = 8;

    public static String getNamesrvAddr() {
        return namesrvAddr;
    }

    public static String getTopicName() {
        return topicName;
    }


    public static String getTags() {
        return tags;
    }

    public static String getProducerGroup() {
        return producerGroup;
    }

    public static String getConsumerGroup() {
        return consumerGroup;
    }

    public static int getTopicQueueNums() {
        return topicQueueNums;
    }
}
