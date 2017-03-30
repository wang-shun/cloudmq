package com.cloudzone.cloudmq.api.impl.producer;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.LocalTransactionState;
import com.alibaba.rocketmq.client.producer.TransactionCheckListener;
import com.alibaba.rocketmq.client.producer.TransactionMQProducer;
import com.alibaba.rocketmq.common.message.MessageAccessor;
import com.alibaba.rocketmq.remoting.RPCHook;
import com.alibaba.rocketmq.remoting.protocol.RemotingCommand;
import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.exception.AuthFailedException;
import com.cloudzone.cloudmq.api.open.transaction.TransactionProducer;
import com.cloudzone.cloudmq.api.open.transaction.TransactionStatus;
import com.cloudzone.cloudmq.api.impl.base.MQClientAbstract;
import com.cloudzone.cloudmq.api.open.base.TransactionSendResult;
import com.cloudzone.cloudmq.api.open.transaction.LocalTransactionExecuter;
import com.cloudzone.cloudmq.common.MQTraceConstants;
import com.cloudzone.cloudmq.common.MyUtils;
import com.cloudzone.cloudmq.common.PropertiesConst;
import com.cloudzone.cloudmq.common.TopicAndAuthKey;
import com.cloudzone.cloudmq.util.Validators;
import org.slf4j.Logger;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static javafx.scene.input.KeyCode.V;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class TransactionProducerImpl extends MQClientAbstract implements TransactionProducer {
    private static final Logger log = ClientLogger.getLog();
    private final AtomicBoolean started = new AtomicBoolean(false);
    TransactionMQProducer transactionMQProducer = null;
    private Properties properties;

    public TransactionProducerImpl(Properties properties, TransactionCheckListener transactionCheckListener) {
        super(properties);
        this.properties = properties;
        this.transactionMQProducer = new TransactionMQProducer((String) properties.get("ProducerGroupId"), new RPCHook() {
            @Override
            public void doBeforeRequest(String remoteAddr, RemotingCommand request) {

            }

            @Override
            public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {

            }
        });
        //boolean isVipChannelEnabled = Boolean.parseBoolean(properties.getProperty("isVipChannelEnabled", "false"));
        this.transactionMQProducer.setTransactionCheckListener(transactionCheckListener);

        try {
            Properties e = new Properties();
            e.put("MaxMsgSize", "128000");
            e.put("AsyncBufferSize", "2048");
            e.put("MaxBatchNum", "1");
            e.put("WakeUpNum", "1");
            e.put(MQTraceConstants.NAMESRV_ADDR, this.getNameServerAddr());
            e.put("InstanceName", this.buildIntanceName());
        } catch (Throwable exception) {
            log.error("system mqtrace hook init failed ,maybe can\'t send msg trace data");
        }

    }

    public void start() {
        if (this.started.compareAndSet(false, true)) {
            if (this.transactionMQProducer.getTransactionCheckListener() == null) {
                throw new IllegalArgumentException("TransactionCheckListener is null");
            }

            this.transactionMQProducer.setNamesrvAddr(this.nameServerAddr);

            try {
                this.transactionMQProducer.start();
            } catch (MQClientException exception) {
                throw new RuntimeException(exception);
            }
        }

    }

    public void shutdown() {
        if (this.started.compareAndSet(true, false)) {
            this.transactionMQProducer.shutdown();
        }

    }

    public TransactionSendResult send(final Msg message, final LocalTransactionExecuter executer, Object arg) throws RuntimeException {
        Validators.checkTopic(this.properties, message.getTopic());
        this.checkONSProducerServiceState(this.transactionMQProducer.getDefaultMQProducerImpl());
        com.alibaba.rocketmq.common.message.Message msgRMQ = MyUtils.msgConvert(message);
        MessageAccessor.putProperty(msgRMQ, "ProducerGroupId", (String) this.properties.get("ProducerGroupId"));
        TransactionSendResult sendResultRMQ = new TransactionSendResult();
        com.alibaba.rocketmq.client.producer.TransactionSendResult transactionSendResult = null;
        try {
            transactionSendResult = this.transactionMQProducer.sendMessageInTransaction(msgRMQ, new com.alibaba.rocketmq.client.producer.LocalTransactionExecuter() {
                public LocalTransactionState executeLocalTransactionBranch(com.alibaba.rocketmq.common.message.Message msg, Object arg) {
                    String msgId = msg.getProperty("__transactionId__");
                    message.setTransactionMsgId(msgId);
                    TransactionStatus transactionStatus = executer.execute(message, arg);
                    return TransactionStatus.CommitTransaction == transactionStatus ? LocalTransactionState.COMMIT_MESSAGE : (TransactionStatus.RollbackTransaction == transactionStatus ? LocalTransactionState.ROLLBACK_MESSAGE : LocalTransactionState.UNKNOW);
                }
            }, arg);

            sendResultRMQ.setSendStatus(transactionSendResult.getSendStatus());
            sendResultRMQ.setMsgId(transactionSendResult.getMsgId());
            sendResultRMQ.setMessageQueue(transactionSendResult.getMessageQueue());
            sendResultRMQ.setQueueOffset(transactionSendResult.getQueueOffset());
            sendResultRMQ.setLocalTransactionState(transactionSendResult.getLocalTransactionState());
            sendResultRMQ.setTransactionId(transactionSendResult.getTransactionId());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        if (sendResultRMQ.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE) {
            log.error("localTransactionState: " + sendResultRMQ.getLocalTransactionState());
            throw new RuntimeException("local transaction branch failed ,so transaction rollback");
        } else {
            return sendResultRMQ;
        }
    }

    public boolean isStarted() {
        return this.started.get();
    }

    public boolean isClosed() {
        return !this.isStarted();
    }
}
