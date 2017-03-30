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

    public TopicAndAuthKey(ConcurrentHashMap<String, String> topicAuthKeyMap, String[] topicList) {
        this.topicAuthKeyMap = topicAuthKeyMap;
        this.topicArray = topicList;
    }

    public ConcurrentHashMap<String, String> getTopicAuthKeyMap() {
        return topicAuthKeyMap;
    }

    public void setTopicAuthKeyMap(ConcurrentHashMap<String, String> topicAuthKeyMap) {
        this.topicAuthKeyMap = topicAuthKeyMap;
    }

    public String topicArrayToString() {

        return org.apache.commons.lang.StringUtils.join(topicArray, ",");
    }
}
