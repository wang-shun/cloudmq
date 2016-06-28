package com.gome.common;

import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.api.open.exception.GomeClientException;
import com.gome.api.open.base.MessageAccessor;

import java.util.*;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class MyUtils {
    private static Set<String> ReservedKeySetRMQ = new HashSet();
    private static Set<String> ReservedKeySetGome = new HashSet();

    /**
     * 获取getNamesrvAddr地址
     *
     * @author tantexian
     * @since 2016/6/15
     * @params
     */
    public static String getNamesrvAddr() {
        return System.getProperty("gomemq.namesrv.addr", System.getenv("NAMESRV_ADDR"));
    }

    /**
     * 将gomemq-client的message转换为原生RMQ格式message
     * @author tantexian
     * @since 2016/6/27
     * @params
     */
    public static Message msgConvert(com.gome.api.open.base.Message message) {
        Message msgRMQ = new Message();
        if(message == null) {
            throw new GomeClientException("\'message\' is null");
        } else {
            if(message.getTopic() != null) {
                msgRMQ.setTopic(message.getTopic());
            }

            if(message.getKey() != null) {
                msgRMQ.setKeys(message.getKey());
            }

            if(message.getTag() != null) {
                msgRMQ.setTags(message.getTag());
            }

            if(message.getStartDeliverTime() > 0L) {
                msgRMQ.putUserProperty("__STARTDELIVERTIME", String.valueOf(message.getStartDeliverTime()));
            }

            if(message.getBody() != null) {
                msgRMQ.setBody(message.getBody());
            }

            Properties systemProperties = MessageAccessor.getSystemProperties(message);
            if(systemProperties != null) {
                Iterator userProperties = systemProperties.entrySet().iterator();

                while(userProperties.hasNext()) {
                    Map.Entry it = (Map.Entry)userProperties.next();
                    if(!ReservedKeySetGome.contains(it.getKey().toString())) {
                        com.alibaba.rocketmq.common.message.MessageAccessor.putProperty(msgRMQ, it.getKey().toString(), it.getValue().toString());
                    }
                }
            }

            Properties userProperties1 = message.getUserProperties();
            if(userProperties1 != null) {
                Iterator it1 = userProperties1.entrySet().iterator();

                while(it1.hasNext()) {
                    Map.Entry next = (Map.Entry)it1.next();
                    if(!ReservedKeySetRMQ.contains(next.getKey().toString())) {
                        com.alibaba.rocketmq.common.message.MessageAccessor.putProperty(msgRMQ, next.getKey().toString(), next.getValue().toString());
                    }
                }
            }

            return msgRMQ;
        }
    }

    /**
     * 将RMQ格式message转换为gomemq-client的message
     * @author tantexian
     * @since 2016/6/27
     * @params
     */
    public static com.gome.api.open.base.Message msgConvert(Message msgRMQ) {
        com.gome.api.open.base.Message message = new com.gome.api.open.base.Message();
        if(msgRMQ.getTopic() != null) {
            message.setTopic(msgRMQ.getTopic());
        }

        if(msgRMQ.getKeys() != null) {
            message.setKey(msgRMQ.getKeys());
        }

        if(msgRMQ.getTags() != null) {
            message.setTag(msgRMQ.getTags());
        }

        if(msgRMQ.getBody() != null) {
            message.setBody(msgRMQ.getBody());
        }

        message.setReconsumeTimes(((MessageExt)msgRMQ).getReconsumeTimes());
        Map properties = msgRMQ.getProperties();
        if(properties != null) {
            Iterator it = properties.entrySet().iterator();

            while(it.hasNext()) {
                Map.Entry next = (Map.Entry)it.next();
                if(ReservedKeySetRMQ.contains(((String)next.getKey()).toString())) {
                    MessageAccessor.putSystemProperties(message, ((String)next.getKey()).toString(), ((String)next.getValue()).toString());
                } else {
                    message.putUserProperties(((String)next.getKey()).toString(), ((String)next.getValue()).toString());
                }
            }
        }

        return message;
    }

}
