package com.cloudzone.cloudmq.common;

import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.cloudzone.cloudmq.api.open.base.MessageAccessor;
import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.exception.GomeClientException;

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
     * @params
     * @since 2016/6/15
     */
    public static String getNamesrvAddr() {
        return System.getProperty("cloudmq.namesrv.addr", System.getenv("NAMESRV_ADDR"));
    }

    /**
     * 将cloudmq-client的message转换为原生RMQ格式message
     *
     * @author tantexian
     * @params
     * @since 2016/6/27
     */
    public static Message msgConvert(Msg msg) {
        Message msgRMQ = new Message();
        if (msg == null) {
            throw new GomeClientException("\'msg\' is null");
        } else {
            if (msg.getTopic() != null) {
                msgRMQ.setTopic(msg.getTopic());
            }

            if (msg.getKey() != null) {
                msgRMQ.setKeys(msg.getKey());
            }

            if (msg.getTags() != null) {
                msgRMQ.setTags(msg.getTags());
            }

            /*if(msg.getStartDeliverTime() > 0L) {
                msgRMQ.putUserProperty("__STARTDELIVERTIME", String.valueOf(msg.getStartDeliverTime()));
            }*/

            if (msg.getBody() != null) {
                msgRMQ.setBody(msg.getBody());
            }

            Properties systemProperties = MessageAccessor.getSystemProperties(msg);
            if (systemProperties != null) {
                Iterator userProperties = systemProperties.entrySet().iterator();

                while (userProperties.hasNext()) {
                    Map.Entry it = (Map.Entry) userProperties.next();
                    if (!ReservedKeySetGome.contains(it.getKey().toString())) {
                        com.alibaba.rocketmq.common.message.MessageAccessor.putProperty(msgRMQ, it.getKey().toString(), it.getValue().toString());
                    }
                }
            }

            Properties userProperties1 = msg.getUserProperties();
            if (userProperties1 != null) {
                Iterator it1 = userProperties1.entrySet().iterator();

                while (it1.hasNext()) {
                    Map.Entry next = (Map.Entry) it1.next();
                    if (!ReservedKeySetRMQ.contains(next.getKey().toString())) {
                        com.alibaba.rocketmq.common.message.MessageAccessor.putProperty(msgRMQ, next.getKey().toString(), next.getValue().toString());
                    }
                }
            }

            return msgRMQ;
        }
    }

    /**
     * 将RMQ格式message转换为cloudmq-client的message
     *
     * @param msgRMQ RMQ格式message
     * @param msgId  消息ID
     * @return
     * @author tantexian
     * @since 2016/6/27
     */
    public static Msg msgConvert(Message msgRMQ, String msgId) {
        Msg msg = new Msg();
        if (msgRMQ.getTopic() != null) {
            msg.setTopic(msgRMQ.getTopic());
        }

        if (msgRMQ.getKeys() != null) {
            msg.setKey(msgRMQ.getKeys());
        }

        if (msgRMQ.getTags() != null) {
            msg.setTags(msgRMQ.getTags());
        }

        if (msgRMQ.getBody() != null) {
            msg.setBody(msgRMQ.getBody());
        }

        if (msgId != null) {
            msg.setMsgId(msgId);
        }

        msg.setReconsumeTimes(((MessageExt) msgRMQ).getReconsumeTimes());
        Map properties = msgRMQ.getProperties();
        if (properties != null) {
            Iterator it = properties.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry next = (Map.Entry) it.next();
                if (ReservedKeySetRMQ.contains(((String) next.getKey()).toString())) {
                    MessageAccessor.putSystemProperties(msg, ((String) next.getKey()).toString(), ((String) next.getValue()).toString());
                } else {
                    msg.putUserProperties(((String) next.getKey()).toString(), ((String) next.getValue()).toString());
                }
            }
        }

        return msg;
    }

}
