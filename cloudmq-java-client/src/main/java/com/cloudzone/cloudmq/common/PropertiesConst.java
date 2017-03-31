package com.cloudzone.cloudmq.common;


/**
 * Properties常量表
 *
 * @author tantexian
 * @since 2016/6/27
 */
public class PropertiesConst {

    public static class Keys {
        public static final String ProducerGroupId = "ProducerGroupId";
        public static final String ConsumerGroupId = "ConsumerGroupId";
        public static final String NAMESRV_ADDR = "NAMESRV_ADDR";
        public static final String MessageModel = "MessageModel";
        // 下述值，当前版本还不支持使用 2016/6/28 Add by tantexixan
        public static final String AccessKey = "AccessKey";
        public static final String SecretKey = "SecretKey";
        public static final String SendMsgTimeoutMillis = "SendMsgTimeoutMillis";
        public static final String ONSAddr = "ONSAddr";
        public static final String ConsumeThreadNums = "ConsumeThreadNums";
        public static final String OnsChannel = "OnsChannel";
        public static final String MQType = "MQType";
        public static final String isVipChannelEnabled = "isVipChannelEnabled";
        public static final String SuspendTimeMillis = "suspendTimeMillis";
        public static final String MaxReconsumeTimes = "maxReconsumeTimes";
        public static final String ConsumeTimeout = "consumeTimeout";
        public static final String MqttMessageId = "mqttMessageId";
        public static final String MqttMessage = "mqttMessage";
        public static final String MqttPublishRetain = "mqttRetain";
        public static final String MqttPublishDubFlag = "mqttPublishDubFlag";
        public static final String MqttSecondTopic = "mqttSecondTopic";
        public static final String MqttClientId = "mqttClientId";
        public static final String MqttQOS = "qoslevel";
        //客户端加topic认证功能
        public static final String TOPIC_NAME = "appMetaName";
        public static final String AUTH_KEY = "authKey";
        public static final String TYPE = "appType";
        public static final String PROPERTIES_PATH = "client.properties";
        public static final String WSADDR_AUTH = "wsaddrAuth";
        //解决客户端多个订阅的问题
        public static final String TOPIC_NAME_AND_AUTH_KEY = "TOPIC_NAME_AND_AUTH_KEY";
        //内部用
        public static final String InnerTopicAndAuthKey = "InnerTopicAndAuthKey";


        private Keys() {
        }

        ;
    }

    public class Values {
        // 广播订阅模式
        public static final String BROADCASTING = "BROADCASTING";
        // 集群订阅模式（默认为该模式）
        public static final String CLUSTERING = "CLUSTERING";

        public static final int CLOUDMQ = 1;

        private Values() {
        }

        ;
    }


    private PropertiesConst() {
    }

    ;
}
