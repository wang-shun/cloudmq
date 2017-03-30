package com.cloudzone.cloudmq.common;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yintongjiang
 * @params
 * @since 2017/3/29
 */
public class TopicAndAuthKey {
    private ConcurrentHashMap<String, String> topicAuthKeyMap;
    private String[] topicArray;
    private ProcessMsgType processMsgType;

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

    public String topicArrayToString() {
        return org.apache.commons.lang.StringUtils.join(topicArray, ",");
    }
}
