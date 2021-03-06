package com.cloudzone.cloudmq.api.impl.consumer;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.cloudzone.cloudmq.api.impl.base.MQClientAbstract;
import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.exception.GomeClientException;
import com.cloudzone.cloudmq.api.open.order.ConsumeOrderContext;
import com.cloudzone.cloudmq.api.open.order.MsgOrderListener;
import com.cloudzone.cloudmq.api.open.order.OrderAction;
import com.cloudzone.cloudmq.api.open.order.OrderConsumer;
import com.cloudzone.cloudmq.common.MyUtils;
import com.cloudzone.cloudmq.common.PropertiesConst;
import com.cloudzone.cloudmq.util.UtilAll;
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
    private final ConcurrentHashMap<String, MsgOrderListener> subscribeTable = new ConcurrentHashMap();
    private final AtomicBoolean started = new AtomicBoolean(false);


    public OrderConsumerImpl(Properties properties) {
        super(properties);
        this.defaultMQPushConsumer = new DefaultMQPushConsumer();
        String consumerGroup = properties.getProperty(PropertiesConst.Keys.ConsumerGroupId);
        if (UtilAll.isBlank(consumerGroup)) {
            throw new GomeClientException(String.format("'%s' property can't be empty", PropertiesConst.Keys.ConsumerGroupId));
        }

        // 下述参数暂时不支持 2016/6/28 Add by tantexixan
            /*
             * String suspendTimeMillis =
             * properties.getProperty("suspendTimeMillis");
             * if(!UtilAll.isBlank(suspendTimeMillis)) { try {
             * this.defaultMQPushConsumer.setSuspendCurrentQueueTimeMillis(Long.
             * parseLong(suspendTimeMillis)); } catch (NumberFormatException
             * var12) { ; } }
             *
             * String maxReconsumeTimes =
             * properties.getProperty("maxReconsumeTimes");
             * if(!UtilAll.isBlank(maxReconsumeTimes)) { try {
             * this.defaultMQPushConsumer.setMaxReconsumeTimes(Integer.parseInt(
             * maxReconsumeTimes)); } catch (NumberFormatException var11) { ; }
             * }
             *
             * String consumeTimeout = properties.getProperty("consumeTimeout");
             * if(!UtilAll.isBlank(consumeTimeout)) { try {
             * this.defaultMQPushConsumer.setConsumeTimeout((long)Integer.
             * parseInt(consumeTimeout)); } catch (NumberFormatException var10)
             * { ; } }
             */

        this.defaultMQPushConsumer.setConsumerGroup(consumerGroup.trim());
        this.defaultMQPushConsumer.setInstanceName(this.buildIntanceName());
        this.defaultMQPushConsumer.setNamesrvAddr(this.getNameServerAddr());
        if (properties.containsKey("ConsumeThreadNums")) {
            this.defaultMQPushConsumer.setConsumeThreadMin(Integer.valueOf(properties.get("ConsumeThreadNums").toString()).intValue());
            this.defaultMQPushConsumer.setConsumeThreadMax(Integer.valueOf(properties.get("ConsumeThreadNums").toString()).intValue());
        }

        // 当前版本不支持 2016/6/28 Add by tantexixan
            /*
             * try { Properties e = new Properties(); e.put("AccessKey",
             * this.sessionCredentials.getAccessKey()); e.put("SecretKey",
             * this.sessionCredentials.getSecretKey()); e.put("MaxMsgSize",
             * "128000"); e.put("AsyncBufferSize", "2048"); e.put("MaxBatchNum",
             * "1"); e.put("WakeUpNum", "1");
             * e.put(OnsTraceConstants.NAMESRV_ADDR, this.getNameServerAddr());
             * e.put("InstanceName", this.buildIntanceName());
             * AsyncTraceAppender appender = new AsyncTraceAppender(e);
             * this.traceDispatcher = new AsyncTraceDispatcher(e);
             * this.traceDispatcher.start(appender,
             * this.defaultMQPushConsumer.getInstanceName());
             * this.defaultMQPushConsumer.getDefaultMQPushConsumerImpl().
             * registerConsumeMessageHook(new
             * OnsConsumeMessageHookImpl(this.traceDispatcher)); } catch
             * (Throwable var9) { log.error(
             * "system mqtrace hook init failed ,maybe can\'t send msg trace data"
             * ); }
             */
    }


    public void start() {
        this.defaultMQPushConsumer
                .registerMessageListener(new OrderConsumerImpl.MessageListenerOrderlyImpl());

        try {
            if (this.started.compareAndSet(false, true)) {
                this.defaultMQPushConsumer.start();
                this.startSchedule();
            }

        } catch (Exception var2) {
            throw new GomeClientException(var2);
        }
    }


    public void shutdown() {
        if (this.started.compareAndSet(true, false)) {
            this.defaultMQPushConsumer.shutdown();
            this.shutdownSchedule();
        }

    }


    public void subscribe(String topic, String subExpression, MsgOrderListener listener) {
        if (null == topic) {
            throw new GomeClientException("topic is null");
        } else if (null == listener) {
            throw new GomeClientException("listener is null");
        } else {
            try {
                // 订阅消息时候对topic进行校验 2017/3/29 Add by yintongqiang
                this.checkTopic(this.properties, topic, null);
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


        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs,
                                                   ConsumeOrderlyContext consumeOrderlyContext) {
            MessageExt msg = msgs.get(0);
            String msgId = msg.getMsgId();
            MsgOrderListener listener = OrderConsumerImpl.this.subscribeTable.get(msg.getTopic());
            if (null == listener) {
                throw new GomeClientException("MsgOrderListener is null");
            } else {
                ConsumeOrderContext context = new ConsumeOrderContext();
                Msg m = MyUtils.msgConvert(msg, msgId);
                OrderAction action = listener.consume(m, context);
                // 消费消息时候对topic进行校验和统计限流 2017/3/29 Add by yintongqiang
                checkTopic(properties, msg.getTopic(), m);
                if (action != null) {
                    switch (action) {
                        case Success:
                            return ConsumeOrderlyStatus.SUCCESS;
                        case Suspend:
                            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                        default:
                            break;
                    }

                }
            }
            return null;
        }
    }
}
