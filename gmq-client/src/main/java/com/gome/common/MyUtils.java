package com.gome.common;

import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.api.open.base.Msg;
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
        return System.getProperty("gmq.namesrv.addr", System.getenv("NAMESRV_ADDR"));
    }

    /**
     * 将gmq-client的message转换为原生RMQ格式message
     * @author tantexian
     * @since 2016/6/27
     * @params
     */
    public static Message msgConvert(Msg msg) {
        Message msgRMQ = new Message();
        if(msg == null) {
            throw new GomeClientException("\'msg\' is null");
        } else {
            if(msg.getTopic() != null) {
                msgRMQ.setTopic(msg.getTopic());
            }

            if(msg.getKey() != null) {
                msgRMQ.setKeys(msg.getKey());
            }

            if(msg.getTag() != null) {
                msgRMQ.setTags(msg.getTag());
            }

            /*if(msg.getStartDeliverTime() > 0L) {
                msgRMQ.putUserProperty("__STARTDELIVERTIME", String.valueOf(msg.getStartDeliverTime()));
            }*/

            if(msg.getBody() != null) {
                msgRMQ.setBody(msg.getBody());
            }

            Properties systemProperties = MessageAccessor.getSystemProperties(msg);
            if(systemProperties != null) {
                Iterator userProperties = systemProperties.entrySet().iterator();

                while(userProperties.hasNext()) {
                    Map.Entry it = (Map.Entry)userProperties.next();
                    if(!ReservedKeySetGome.contains(it.getKey().toString())) {
                        com.alibaba.rocketmq.common.message.MessageAccessor.putProperty(msgRMQ, it.getKey().toString(), it.getValue().toString());
                    }
                }
            }

            Properties userProperties1 = msg.getUserProperties();
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
     * 将RMQ格式message转换为gmq-client的message
     * @author tantexian
     * @since 2016/6/27
     * @params
     */
    public static Msg msgConvert(Message msgRMQ) {
        Msg msg = new Msg();
        if(msgRMQ.getTopic() != null) {
            msg.setTopic(msgRMQ.getTopic());
        }

        if(msgRMQ.getKeys() != null) {
            msg.setKey(msgRMQ.getKeys());
        }

        if(msgRMQ.getTags() != null) {
            msg.setTag(msgRMQ.getTags());
        }

        if(msgRMQ.getBody() != null) {
            msg.setBody(msgRMQ.getBody());
        }

        msg.setReconsumeTimes(((MessageExt)msgRMQ).getReconsumeTimes());
        Map properties = msgRMQ.getProperties();
        if(properties != null) {
            Iterator it = properties.entrySet().iterator();

            while(it.hasNext()) {
                Map.Entry next = (Map.Entry)it.next();
                if(ReservedKeySetRMQ.contains(((String)next.getKey()).toString())) {
                    MessageAccessor.putSystemProperties(msg, ((String)next.getKey()).toString(), ((String)next.getValue()).toString());
                } else {
                    msg.putUserProperties(((String)next.getKey()).toString(), ((String)next.getValue()).toString());
                }
            }
        }

        return msg;
    }

}
