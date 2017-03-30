package com.cloudzone.cloudmq.api.impl.consumer;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cloudzone.cloudmq.api.open.base.Consumer;
import com.cloudzone.cloudmq.api.open.exception.AuthFailedException;
import com.cloudzone.cloudmq.api.open.exception.GomeClientException;
import com.cloudzone.cloudmq.common.PropertiesConst;
import com.cloudzone.cloudmq.common.TopicAndAuthKey;
import org.slf4j.Logger;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.cloudzone.cloudmq.api.impl.base.MQClientAbstract;
import com.cloudzone.cloudmq.api.open.base.Action;
import com.cloudzone.cloudmq.api.open.base.ConsumeContext;
import com.cloudzone.cloudmq.api.open.base.MsgListener;
import com.cloudzone.cloudmq.common.MQTraceConstants;
import com.cloudzone.cloudmq.common.MyUtils;
import com.cloudzone.cloudmq.log.GClientLogger;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public class ConsumerImpl extends MQClientAbstract implements Consumer {
    private static final Logger log = GClientLogger.getLog();
    private final DefaultMQPushConsumer defaultMQPushConsumer;
    private final ConcurrentHashMap<String, MsgListener> subscribeTable = new ConcurrentHashMap();
    private final AtomicBoolean started = new AtomicBoolean(false);


    public ConsumerImpl(Properties properties) {
        super(properties);
        this.defaultMQPushConsumer = new DefaultMQPushConsumer();
        String consumerGroup = properties.getProperty("ConsumerGroupId");
        if (null == consumerGroup) {
            throw new GomeClientException("\'ConsumerGroupId\' property is null");
        }

        String messageModel = properties.getProperty("MessageModel", "CLUSTERING");
        this.defaultMQPushConsumer.setMessageModel(MessageModel.valueOf(messageModel));
        this.defaultMQPushConsumer.setConsumerGroup(consumerGroup);
        this.defaultMQPushConsumer.setInstanceName(this.buildIntanceName());
        this.defaultMQPushConsumer.setNamesrvAddr(this.getNameServerAddr());
        if (properties.containsKey("ConsumeThreadNums")) {
            this.defaultMQPushConsumer.setConsumeThreadMin(
                    Integer.valueOf(properties.get("ConsumeThreadNums").toString()).intValue());
            this.defaultMQPushConsumer.setConsumeThreadMax(
                    Integer.valueOf(properties.get("ConsumeThreadNums").toString()).intValue());
        }

        try {
            Properties e = new Properties();
            e.put("MaxMsgSize", "128000");
            e.put("AsyncBufferSize", "2048");
            e.put("MaxBatchNum", "1");
            e.put("WakeUpNum", "1");
            e.put(MQTraceConstants.NAMESRV_ADDR, this.getNameServerAddr());
            e.put("InstanceName", this.buildIntanceName());
            // this.traceDispatcher.start(appender,
            // this.defaultMQPushConsumer.getInstanceName());
        } catch (Throwable throwable) {
            log.error("system mqtrace hook init failed ,maybe can\'t send msg trace data");
        }

    }


    public void start() {
        this.defaultMQPushConsumer.registerMessageListener(new ConsumerImpl.MessageListenerImpl());

        try {
            if (this.started.compareAndSet(false, true)) {
                this.defaultMQPushConsumer.start();
            }

        } catch (Exception e) {
            throw new GomeClientException(e.getMessage());
        }
    }


    public void shutdown() {
        if (this.started.compareAndSet(true, false)) {
            this.defaultMQPushConsumer.shutdown();
        }

    }


    public void subscribe(String topic, String subExpression, MsgListener listener) {
        if (null == topic) {
            throw new GomeClientException("topic is null");
        } else if (null == listener) {
            throw new GomeClientException("listener is null");
        } else {
            try {
                TopicAndAuthKey topicAndAuthKey = (TopicAndAuthKey) this.properties.get(PropertiesConst.Keys.TopicAndAuthKey);
                if (!topicAndAuthKey.getTopicAuthKeyMap().containsKey(topic)) {
                    throw new AuthFailedException("申请的topic和消费的topic不匹配,申请的topic为[" + topicAndAuthKey.topicArrayToString() + "],发送的topic为[" + topic + "]");
                }
                this.subscribeTable.put(topic, listener);
                this.defaultMQPushConsumer.subscribe(topic, subExpression);
            } catch (MQClientException var5) {
                throw new GomeClientException("defaultMQPushConsumer subscribe exception", var5);
            }
        }
    }


    public void unsubscribe(String topic) {
        if (null != topic) {
            this.defaultMQPushConsumer.unsubscribe(topic);
        }

    }


    public boolean isStarted() {
        return this.started.get();
    }


    public boolean isClosed() {
        return !this.isStarted();
    }

    class MessageListenerImpl implements MessageListenerConcurrently {
        MessageListenerImpl() {
        }


        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgList,
                                                        ConsumeConcurrentlyContext consumeConcurrentlyContext) {
            MessageExt msg = msgList.get(0);
            String msgId = msg.getMsgId();
            MsgListener listener = ConsumerImpl.this.subscribeTable.get(msg.getTopic());
            if (null == listener) {
                throw new GomeClientException("MsgListener is null");
            } else {
                ConsumeContext context = new ConsumeContext();
                Action action = listener.consume(MyUtils.msgConvert(msg, msgId), context);

                switch (action) {
                    case CommitMessage:
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    case ReconsumeLater:
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    default:
                        break;
                }

                return null;
            }
        }
    }
}
