package com.cloudzone.cloudmq.api.open.base;

import com.cloudzone.cloudmq.api.open.exception.GomeClientException;
import com.cloudzone.cloudmq.common.DelayLevelConst;

import java.io.Serializable;
import java.util.Properties;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class Msg extends com.alibaba.rocketmq.common.message.Message implements Serializable {
    private static final long serialVersionUID = -1385924226856188094L;
    Properties systemProperties;
    private String topic;
    private Properties userProperties;
    private byte[] body;

    public Msg() {
        this((String) null, (String) null, "", (byte[]) null);
    }

    public Msg(String topic, String tags, String key, byte[] body) {
        this.topic = topic;
        this.body = body;
        this.setTags(tags);
        this.setKey(key);
    }

    void putSystemProperties(String key, String value) {
        if (null == this.systemProperties) {
            this.systemProperties = new Properties();
        }

        if (key != null && value != null) {
            this.systemProperties.put(key, value);
        }

    }

    public Msg(String topic, String tags, byte[] body) {
        this(topic, tags, "", body);
    }

    public void putUserProperties(String key, String value) {
        if (null == this.userProperties) {
            this.userProperties = new Properties();
        }

        if (key != null && value != null) {
            this.userProperties.put(key, value);
        }

    }

    public String getUserProperties(String key) {
        return null != this.userProperties ? (String) this.userProperties.get(key) : null;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTags() {
        return this.getSystemProperties(SystemPropKey.TAGS);
    }

    String getSystemProperties(String key) {
        return null != this.systemProperties ? this.systemProperties.getProperty(key) : null;
    }

    public void setTags(String tags) {
        this.putSystemProperties(SystemPropKey.TAGS, tags);
        super.setTags(tags);
    }

    public String getKey() {
        return this.getSystemProperties(SystemPropKey.KEY);
    }

    public void setKey(String key) {
        this.putSystemProperties(SystemPropKey.KEY, key);
        super.setKeys(key);
    }

    public String getMsgId() {
        return this.getSystemProperties(SystemPropKey.MSGID);
    }

    public void setMsgId(String msgId) {
        this.putSystemProperties(SystemPropKey.MSGID, msgId);
    }

    public void setTransactionMsgId(String transactionMsgId) {
        this.putSystemProperties(SystemPropKey.TRANSATIONMSGID, transactionMsgId);
    }

    public String getTransactionMsgId() {
        return this.getSystemProperties(SystemPropKey.TRANSATIONMSGID);
    }

    Properties getSystemProperties() {
        return this.systemProperties;
    }

    void setSystemProperties(Properties systemProperties) {
        this.systemProperties = systemProperties;
    }

    public Properties getUserProperties() {
        return this.userProperties;
    }

    public void setUserProperties(Properties userProperties) {
        this.userProperties = userProperties;
    }

    public byte[] getBody() {
        return this.body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getReconsumeTimes() {
        String pro = this.getSystemProperties(SystemPropKey.RECONSUMETIMES);
        return pro != null ? Integer.parseInt(pro) : 0;
    }

    public void setReconsumeTimes(int value) {
        this.putSystemProperties(SystemPropKey.RECONSUMETIMES, String.valueOf(value));
    }

    // 当前版本目前只支持固定等级的消息延时Level 2016/6/28 Add by tantexixan
    /*public long getStartDeliverTime() {
        String pro = this.getSystemProperties("__STARTDELIVERTIME");
        return pro != null?Long.parseLong(pro):0L;
    }

    public void setStartDeliverTime(long value) {
        this.putSystemProperties("__STARTDELIVERTIME", String.valueOf(value));
    }*/

    public void setDelayTimeLevel(int level) {
        //level 必须为DelayLevelConst中的值
        if (level < DelayLevelConst.OneSecond.val() || level > DelayLevelConst.TwoHour.val()) {
            throw new GomeClientException("消息延迟Level值设置错误，请重新设置!!!");
        }
        super.setDelayTimeLevel(level);
    }

    public String toString() {
        return "Msg [topic=" + this.topic
                + ", systemProperties=" + this.systemProperties
                + ", userProperties=" + this.userProperties
                + ", body.length=" + (this.body != null ? this.body.length : 0)
                + ", msgId=" + this.getMsgId() + "]";
    }

    public static class SystemPropKey {
        public static final String TAGS = "__TAGS";
        public static final String KEY = "__KEY";
        public static final String MSGID = "__MSGID";
        public static final String TRANSATIONMSGID = "__TRANSATIONMSGID";
        public static final String RECONSUMETIMES = "__RECONSUMETIMES";
        public static final String STARTDELIVERTIME = "__STARTDELIVERTIME";

        public SystemPropKey() {
        }
    }
}
