package com.cloudzone.cloudmq.common;


import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yintongjiang
 * @params toipc和authKey用于消息处理
 * @since 2017/3/29
 */
public class TopicAndAuthKey {
    // topic和authkey的map
    private ConcurrentHashMap<String, String> topicAuthKeyMap;
    // 仅用于认证的topic和发送或消费的topic不匹配进行错误的输出
    private String[] topicArray;
    //  消息类型的枚举
    private ProcessMsgType processMsgType;

    public String[] getTopicArray() {
        return topicArray;
    }

    public void setTopicArray(String[] topicArray) {
        this.topicArray = topicArray;
    }

    public TopicAndAuthKey(ConcurrentHashMap<String, String> topicAuthKeyMap, String[] topicList, ProcessMsgType processMsgType) {
        this.topicAuthKeyMap = topicAuthKeyMap;
        this.topicArray = topicList;
        this.processMsgType = processMsgType;
    }

    public ConcurrentHashMap<String, String> getTopicAuthKeyMap() {
        return topicAuthKeyMap;
    }

    public void setTopicAuthKeyMap(ConcurrentHashMap<String, String> topicAuthKeyMap) {
        this.topicAuthKeyMap = topicAuthKeyMap;
    }

    public ProcessMsgType getProcessMsgType() {
        return processMsgType;
    }

    public void setProcessMsgType(ProcessMsgType processMsgType) {
        this.processMsgType = processMsgType;
    }

    // 将数组中的转成字符串并以逗号隔开
    public String topicArrayToString() {
        return org.apache.commons.lang.StringUtils.join(topicArray, ",");
    }

    @Override
    public String toString() {
        return "TopicAndAuthKey{" +
                "topicAuthKeyMap=" + topicAuthKeyMap +
                ", topicArray=" + Arrays.toString(topicArray) +
                ", processMsgType=" + processMsgType +
                '}';
    }
}
