package com.cloudzone.cloudmq.api.impl.producer;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.remoting.common.RemotingHelper;
import com.alibaba.rocketmq.remoting.exception.RemotingConnectException;
import com.alibaba.rocketmq.remoting.exception.RemotingTimeoutException;
import com.cloudzone.cloudmq.api.impl.base.MQClientAbstract;
import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.base.Producer;
import com.cloudzone.cloudmq.api.open.base.SendResult;
import com.cloudzone.cloudmq.api.open.exception.GomeClientException;
import com.cloudzone.cloudmq.common.CloudmqFAQ;
import com.cloudzone.cloudmq.common.PropertiesConst;
import com.cloudzone.cloudmq.log.GClientLogger;
import com.cloudzone.cloudmq.util.Validators;
import org.slf4j.Logger;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public class ProducerImpl extends MQClientAbstract implements Producer {
    private static final Logger log = GClientLogger.getLog();
    private final DefaultMQProducer defaultMQProducer;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public ProducerImpl(Properties properties) {
        super(properties);
        this.defaultMQProducer = new DefaultMQProducer();
        String producerGroup = properties.getProperty("ProducerGroupId", "__PRODUCER_DEFAULT_GROUP");
        this.defaultMQProducer.setProducerGroup(producerGroup);
        if (properties.containsKey("SendMsgTimeoutMillis")) {
            this.defaultMQProducer.setSendMsgTimeout(
                    Integer.valueOf(properties.get("SendMsgTimeoutMillis").toString()).intValue());
        } else {
            this.defaultMQProducer.setSendMsgTimeout(5000);
        }

        this.defaultMQProducer.setInstanceName(this.buildIntanceName());
        this.defaultMQProducer.setNamesrvAddr(this.getNameServerAddr());
        this.defaultMQProducer.setMaxMessageSize(4194304);

        try {
            Properties e = new Properties();
            e.put("MaxMsgSize", "128000");
            e.put("AsyncBufferSize", "2048");
            e.put("MaxBatchNum", "1");
            e.put("WakeUpNum", "1");
            e.put(PropertiesConst.Keys.NAMESRV_ADDR, this.getNameServerAddr());
            e.put("InstanceName", this.buildIntanceName());
        } catch (Throwable throwable) {
            log.error("system mqtrace hook init failed ,maybe can\'t send msg trace data");
        }

    }


    public void start() {
        try {
            if (this.started.compareAndSet(false, true)) {
                this.defaultMQProducer.start();
                this.startSchedule();
            }

        } catch (Exception e) {
            log.error("start Exception", RemotingHelper.exceptionSimpleDesc(e));
            throw new GomeClientException(e.getMessage());
        }
    }


    public void shutdown() {
        if (this.started.compareAndSet(true, false)) {
            this.defaultMQProducer.shutdown();
            this.shutdownSchedule();
        }

    }


    public SendResult send(Msg msg) {
        //this.checkONSProducerServiceState(this.defaultMQProducer.getDefaultMQProduceImpl());

        try {
            this.checkTopic(this.properties, msg.getTopic(),msg);
            com.alibaba.rocketmq.client.producer.SendResult sendResultRMQ = this.defaultMQProducer.send(msg);
            SendResult sendResult = new SendResult();
            sendResult.setMsgId(sendResultRMQ.getMsgId());
            sendResult.setSendStatus(sendResultRMQ.getSendStatus());
            sendResult.setMessageQueue(sendResultRMQ.getMessageQueue());
            sendResult.setQueueOffset(sendResultRMQ.getQueueOffset());
            return sendResult;
        } catch (Exception e) {
//            log.error(String.format("Send msg Exception, %s", new Object[] {msg}), e);
            this.checkProducerException(e, msg);
            return null;
        }
    }


    private void checkProducerException(Exception e, Msg msg) {
        // 修改异常信息 2016/7/5 Add by GaoYanLei
        if (e instanceof MQClientException) {
            if (e.getCause() != null) {
                if (e.getCause() instanceof RemotingConnectException) {
                    // throw new GomeClientException(FAQ.errorMessage(
                    // String.format("Connect broker failed, Topic: %s",
                    // new Object[] { msg.getTopic() }),
                    // "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&connect_broker_failed"));
                    throw new GomeClientException(
                            CloudmqFAQ.errorMessage(String.format(CloudmqFAQ.CONNECT_BROKER_FAILED, msg.getTopic())));
                }

                if (e.getCause() instanceof RemotingTimeoutException) {
                    // throw new GomeClientException(FAQ.errorMessage(
                    // String.format("Send msg to broker timeout, %dms, Topic:
                    // %s",
                    // new Object[] {
                    // Integer.valueOf(this.defaultMQProducer.getSendMsgTimeout()),
                    // msg.getTopic() }),
                    // "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&send_msg_failed"));

                    throw new GomeClientException(CloudmqFAQ.errorMessage(String.format(CloudmqFAQ.SEND_MSG_FAILED,
                            new Object[]{Integer.valueOf(this.defaultMQProducer.getSendMsgTimeout()),
                                    msg.getTopic()})));
                }

                if (e.getCause() instanceof MQBrokerException) {
                    // MQBrokerException excep = (MQBrokerException)
                    // e.getCause();
                    // throw new GomeClientException(FAQ.errorMessage(
                    // String.format("Receive a broker exception, Topic: %s,
                    // %s",
                    // new Object[] { msg.getTopic(), excep.getErrorMessage()
                    // }),
                    // "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&broker_response_exception"));

                    throw new GomeClientException(
                            CloudmqFAQ.errorMessage(String.format(CloudmqFAQ.BROKER_RESPONSE_EXCEPTION)));
                }
            } else {
                MQClientException excep1 = (MQClientException) e;
                if (-1 == excep1.getResponseCode()) {
                    // throw new GomeClientException(FAQ.errorMessage(
                    // String.format("Topic does not exist, Topic: %s", new
                    // Object[] { msg.getTopic() }),
                    // "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&topic_not_exist"));

                    throw new GomeClientException(
                            CloudmqFAQ.errorMessage(String.format(CloudmqFAQ.TOPIC_NOT_EXIST, msg.getTopic())));
                }
                if (13 == excep1.getResponseCode()) {
                    // throw new GomeClientException(FAQ.errorMessage(
                    // String.format("ONS Client check msg exception, Topic:
                    // %s",
                    // new Object[] { msg.getTopic() }),
                    // "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&msg_check_failed"));

                    throw new GomeClientException(
                            CloudmqFAQ.errorMessage(String.format(CloudmqFAQ.MSG_CHECK_FAILED)));
                }
            }
        }

        throw new GomeClientException("defaultMQProducer send exception", e);
    }


    public void sendOneway(Msg msg) {
        this.checkONSProducerServiceState(this.defaultMQProducer.getDefaultMQProducerImpl());

        try {
            this.checkTopic(this.properties,msg.getTopic(),msg);
            this.defaultMQProducer.sendOneway(msg);
        } catch (Exception e) {
            log.error(String.format("Send msg oneway Exception, %s", new Object[]{msg}), e);
            this.checkProducerException(e, msg);
        }

    }


    public boolean isStarted() {
        return this.started.get();
    }


    public boolean isClosed() {
        return !this.isStarted();
    }

}
