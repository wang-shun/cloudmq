package com.cloudzone.cloudmq.common;

/**
 * @author tantexian
 * @since 2017/1/6
 */
public class AuthKey {

    private String authKey;
    // authkey类型枚举
    private AuthkeyStatus authkeyStatus;

    private String ipAndPort;

    private TopicAndAuthKey topicAndAuthKey;

    public AuthKey(String authKey, AuthkeyStatus authkeyStatus, String ipAndPort) {
        this.authKey = authKey;
        this.authkeyStatus = authkeyStatus;
        this.ipAndPort = ipAndPort;
    }

    public AuthKey(String authKey, String ipAndPort) {
        this.authKey = authKey;
        this.ipAndPort = ipAndPort;
    }

    public AuthKey(String ipAndPort, TopicAndAuthKey topicAndAuthKey) {
        this.ipAndPort = ipAndPort;
        this.topicAndAuthKey = topicAndAuthKey;
    }

    public String getIpAndPort() {
        return ipAndPort;
    }


    public void setIpAndPort(String ipAndPort) {
        this.ipAndPort = ipAndPort;
    }


    public AuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getAuthKey() {
        return authKey;
    }


    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public AuthkeyStatus getAuthkeyStatus() {
        return authkeyStatus;
    }

    public void setAuthkeyStatus(AuthkeyStatus authkeyStatus) {
        this.authkeyStatus = authkeyStatus;
    }

    public TopicAndAuthKey getTopicAndAuthKey() {
        return topicAndAuthKey;
    }

    public void setTopicAndAuthKey(TopicAndAuthKey topicAndAuthKey) {
        this.topicAndAuthKey = topicAndAuthKey;
    }

}
