package com.cloudzone.cloudmq.common;

import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yintongjiang
 * @params toipc和authKey用于消息处理
 * @since 2017/3/29
 */
public class TopicAndAuthKey {

    /**
     * topic和authkey的map
     */
    private ConcurrentHashMap<String, String> topicAuthKeyMap; // topic<authkey>

    /**
     * 仅用于认证的topic和发送或消费的topic不匹配进行错误的输出
     */
    private String[] topicArray;

    /**
     * 消息类型的枚举
     */
    private ProcessMsgType processMsgType;


    public String[] getTopicArray() {
        return topicArray;
    }


    public void setTopicArray(String[] topicArray) {
        this.topicArray = topicArray;
    }


    public TopicAndAuthKey(ConcurrentHashMap<String, String> topicAuthKeyMap, String[] topicList,
            ProcessMsgType processMsgType) {
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


    /**
     * 将数组中的转成字符串并以逗号隔开
     * 
     * @return
     */
    public String topicArrayToString() {
        return StringUtils.join(topicArray, ",");
    }


    @Override
    public String toString() {
        String format = "TopicAndAuthKey {topicAuthKeyMap={%s}, topicArray=[%s], processMsgType=%s}";
        Iterator<Map.Entry<String, String>> itor = topicAuthKeyMap.entrySet().iterator();

        String topicAuthKey = "";
        List<String> topicAuthKeys = new ArrayList<>();
        while (itor.hasNext()) {
            Map.Entry<String, String> entry = itor.next();
            topicAuthKey = String.format("[%s:%s]", entry.getKey(), entry.getValue());
            topicAuthKeys.add(topicAuthKey);
        }

        String topicAuthKeyData = StringUtils.join(topicAuthKeys, ",");
        return String.format(format, topicAuthKeyData, topicArrayToString(), processMsgType.toString());
    }
}
