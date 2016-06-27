package com.gome.api.impl;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.api.open.*;
import com.gome.common.MQTraceConstants;
import com.gome.util.MyUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class OrderConsumerImpl extends MQClientAbstract implements OrderConsumer {
    private static final Logger log = ClientLogger.getLog();
    private final DefaultMQPushConsumer defaultMQPushConsumer;
    private final ConcurrentHashMap<String, MessageOrderListener> subscribeTable = new ConcurrentHashMap();
    private final AtomicBoolean started = new AtomicBoolean(false);

    public OrderConsumerImpl(Properties properties) {
        super(properties);
        this.defaultMQPushConsumer = new DefaultMQPushConsumer();
        String consumerGroup = properties.getProperty("ConsumerId");
        if(null == consumerGroup) {
            throw new GomeClientException("\'ConsumerId\' property is null");
        } else {
            /*String suspendTimeMillis = properties.getProperty("suspendTimeMillis");
            if(!UtilAll.isBlank(suspendTimeMillis)) {
                try {
                    this.defaultMQPushConsumer.setSuspendCurrentQueueTimeMillis(Long.parseLong(suspendTimeMillis));
                } catch (NumberFormatException var12) {
                    ;
                }
            }

            String maxReconsumeTimes = properties.getProperty("maxReconsumeTimes");
            if(!UtilAll.isBlank(maxReconsumeTimes)) {
                try {
                    this.defaultMQPushConsumer.setMaxReconsumeTimes(Integer.parseInt(maxReconsumeTimes));
                } catch (NumberFormatException var11) {
                    ;
                }
            }

            String consumeTimeout = properties.getProperty("consumeTimeout");
            if(!UtilAll.isBlank(consumeTimeout)) {
                try {
                    this.defaultMQPushConsumer.setConsumeTimeout((long)Integer.parseInt(consumeTimeout));
                } catch (NumberFormatException var10) {
                    ;
                }
            }*/

            this.defaultMQPushConsumer.setConsumerGroup(consumerGroup);
            this.defaultMQPushConsumer.setInstanceName(this.buildIntanceName());
            this.defaultMQPushConsumer.setNamesrvAddr(this.getNameServerAddr());
            if(properties.containsKey("ConsumeThreadNums")) {
                this.defaultMQPushConsumer.setConsumeThreadMin(Integer.valueOf(properties.get("ConsumeThreadNums").toString()).intValue());
                this.defaultMQPushConsumer.setConsumeThreadMax(Integer.valueOf(properties.get("ConsumeThreadNums").toString()).intValue());
            }

            try {
                Properties e = new Properties();
                e.put("MaxMsgSize", "128000");
                e.put("AsyncBufferSize", "2048");
                e.put("MaxBatchNum", "1");
                e.put("WakeUpNum", "1");
                e.put(MQTraceConstants.NAMESRV_ADDR, this.getNameServerAddr());
                e.put("InstanceName", this.buildIntanceName());
            } catch (Throwable throwable) {
                log.error("system mqtrace hook init failed ,maybe can\'t send msg trace data");
            }

        }
    }

    public void start() {
        this.defaultMQPushConsumer.registerMessageListener(new OrderConsumerImpl.MessageListenerOrderlyImpl());

        try {
            if(this.started.compareAndSet(false, true)) {
                this.defaultMQPushConsumer.start();
            }

        } catch (Exception var2) {
            throw new GomeClientException(var2);
        }
    }

    public void shutdown() {
        if(this.started.compareAndSet(true, false)) {
            this.defaultMQPushConsumer.shutdown();
        }

    }

    public void subscribe(String topic, String subExpression, MessageOrderListener listener) {
        if(null == topic) {
            throw new GomeClientException("topic is null");
        } else if(null == listener) {
            throw new GomeClientException("listener is null");
        } else {
            try {
                this.subscribeTable.put(topic, listener);
                this.defaultMQPushConsumer.subscribe(topic, subExpression);
            } catch (MQClientException var5) {
                throw new GomeClientException("defaultMQPushConsumer subscribe exception", var5);
            }
        }
    }

    public boolean isStarted() {
        return this.started.get();
    }

    public boolean isClosed() {
        return !this.isStarted();
    }

    class MessageListenerOrderlyImpl implements MessageListenerOrderly {
        MessageListenerOrderlyImpl() {
        }

        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> arg0, ConsumeOrderlyContext arg1) {
            MessageExt msg = arg0.get(0);

            MessageOrderListener listener = OrderConsumerImpl.this.subscribeTable.get(msg.getTopic());
            if(null == listener) {
                throw new GomeClientException("MessageOrderListener is null");
            } else {

                ConsumeOrderContext context = new ConsumeOrderContext();
                OrderAction action = listener.consume(MyUtils.msgConvert(msg), context);
                if(action != null) {
                    switch(action.ordinal()) {
                        case 1:
                            return ConsumeOrderlyStatus.SUCCESS;
                        case 2:
                            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                    }
                }

                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
            }
        }
    }
}
