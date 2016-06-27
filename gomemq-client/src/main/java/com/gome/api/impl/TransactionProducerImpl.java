package com.gome.api.impl;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.*;
import com.alibaba.rocketmq.common.message.MessageAccessor;
import com.alibaba.rocketmq.remoting.RPCHook;
import com.alibaba.rocketmq.remoting.protocol.RemotingCommand;
import com.gome.api.open.LocalTransactionExecuter;
import com.gome.api.open.Message;
import com.gome.api.open.TransactionProducer;
import com.gome.api.open.TransactionStatus;
import com.gome.common.MQTraceConstants;
import com.gome.util.MyUtils;
import org.slf4j.Logger;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

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
        this.transactionMQProducer = new TransactionMQProducer((String) properties.get("ProducerId"), new RPCHook() {
            @Override
            public void doBeforeRequest(String remoteAddr, RemotingCommand request) {

            }

            @Override
            public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {

            }
        });
        boolean isVipChannelEnabled = Boolean.parseBoolean(properties.getProperty("isVipChannelEnabled", "false"));
        this.transactionMQProducer.setTransactionCheckListener(transactionCheckListener);

        try {
            Properties e = new Properties();
            e.put("MaxMsgSize", "128000");
            e.put("AsyncBufferSize", "2048");
            e.put("MaxBatchNum", "1");
            e.put("WakeUpNum", "1");
            e.put(MQTraceConstants.NAMESRV_ADDR, this.getNameServerAddr());
            e.put("InstanceName", this.buildIntanceName());
        } catch (Throwable var6) {
            log.error("system mqtrace hook init failed ,maybe can\'t send msg trace data");
        }

    }

    public void start() {
        if(this.started.compareAndSet(false, true)) {
            if(this.transactionMQProducer.getTransactionCheckListener() == null) {
                throw new IllegalArgumentException("TransactionCheckListener is null");
            }

            this.transactionMQProducer.setNamesrvAddr(this.nameServerAddr);

            try {
                this.transactionMQProducer.start();
            } catch (MQClientException var2) {
                throw new RuntimeException(var2);
            }
        }

    }

    public void shutdown() {
        if(this.started.compareAndSet(true, false)) {
            this.transactionMQProducer.shutdown();
        }

    }

    public SendResult send(final Message message, final LocalTransactionExecuter executer, Object arg) {
        this.checkONSProducerServiceState(this.transactionMQProducer.getDefaultMQProducerImpl());
        com.alibaba.rocketmq.common.message.Message msgRMQ = MyUtils.msgConvert(message);
        MessageAccessor.putProperty(msgRMQ, "ProducerId", (String)this.properties.get("ProducerId"));
        TransactionSendResult sendResultRMQ = null;

        try {
            sendResultRMQ = this.transactionMQProducer.sendMessageInTransaction(msgRMQ, new com.alibaba.rocketmq.client.producer.LocalTransactionExecuter() {
                public LocalTransactionState executeLocalTransactionBranch(com.alibaba.rocketmq.common.message.Message msg, Object arg) {
                    String msgId = msg.getProperty("__transactionId__");
                    message.setMsgID(msgId);
                    TransactionStatus transactionStatus = executer.execute(message, arg);
                    return TransactionStatus.CommitTransaction == transactionStatus?LocalTransactionState.COMMIT_MESSAGE:(TransactionStatus.RollbackTransaction == transactionStatus?LocalTransactionState.ROLLBACK_MESSAGE:LocalTransactionState.UNKNOW);
                }
            }, arg);
        } catch (Exception var7) {
            throw new RuntimeException(var7);
        }

        if(sendResultRMQ.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE) {
            throw new RuntimeException("local transaction branch failed ,so transaction rollback");
        } else {
            SendResult sendResult = new SendResult();
            return sendResult;
        }
    }

    public boolean isStarted() {
        return this.started.get();
    }

    public boolean isClosed() {
        return !this.isStarted();
    }
}
