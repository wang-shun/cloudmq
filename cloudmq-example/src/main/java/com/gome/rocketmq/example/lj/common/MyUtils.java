package com.gome.rocketmq.example.lj.common;

/**
 * @author jerrylou
 * @params
 * @since 2016/7/21 0021
 */
public class MyUtils {

    // 学习cloudmq的本机测试 2016/7/21 0021 Add by jerrylou
    private final static String namesrvAddr = "127.0.0.1:9876";
    private final static String topicName = "topic_lj";
    private final static String tags = "tag_lj";
    private final static String producerGroup = "producerGroup";
    private final static String consumerGroup = "consumerGroup";

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
}
