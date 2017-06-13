package com.cloudzone.cloudmq.base;

import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.MessageQueue;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author tantexian, <my.oschina.net/tantexian>
 * @since 2017/6/12$
 */
@XmlRootElement
public class CloudSendResult {
    private SendStatus sendStatus;
    private String msgId;
    private MessageQueue messageQueue;
    private long queueOffset;
    private String transactionId;
    private String exception;

    public static CloudSendResult convertoSelf(SendResult sendResult) {
        CloudSendResult cloudSendResult = new CloudSendResult();
        cloudSendResult.setSendStatus(sendResult.getSendStatus());
        cloudSendResult.setMessageQueue(sendResult.getMessageQueue());
        cloudSendResult.setMsgId(sendResult.getMsgId());
        cloudSendResult.setQueueOffset(sendResult.getQueueOffset());
        cloudSendResult.setTransactionId(sendResult.getTransactionId());
        return cloudSendResult;
    }

    public SendStatus getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(SendStatus sendStatus) {
        this.sendStatus = sendStatus;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    public long getQueueOffset() {
        return queueOffset;
    }

    public void setQueueOffset(long queueOffset) {
        this.queueOffset = queueOffset;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
}
