package com.cloudzone.cloudmq.api.impl.base;

import com.alibaba.rocketmq.client.producer.LocalTransactionState;
import com.alibaba.rocketmq.client.producer.TransactionCheckListener;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.cloudzone.cloudmq.api.impl.consumer.ConsumerImpl;
import com.cloudzone.cloudmq.api.impl.producer.OrderProducerImpl;
import com.cloudzone.cloudmq.api.impl.producer.ProducerImpl;
import com.cloudzone.cloudmq.api.impl.producer.TransactionProducerImpl;
import com.cloudzone.cloudmq.api.open.base.Consumer;
import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.base.Producer;
import com.cloudzone.cloudmq.api.open.factory.MQFactoryAPI;
import com.cloudzone.cloudmq.api.open.order.OrderConsumer;
import com.cloudzone.cloudmq.api.open.order.OrderProducer;
import com.cloudzone.cloudmq.api.open.transaction.LocalTransactionChecker;
import com.cloudzone.cloudmq.api.open.transaction.TransactionProducer;
import com.cloudzone.cloudmq.api.open.transaction.TransactionStatus;
import com.cloudzone.cloudmq.common.AuthKey;
import com.cloudzone.cloudmq.common.AuthkeyStatus;
import com.cloudzone.cloudmq.common.MyUtils;
import com.cloudzone.cloudmq.common.PropertiesConst;
import com.cloudzone.cloudmq.util.Validators;
import com.cloudzone.cloudmq.api.impl.consumer.OrderConsumerImpl;
import com.cloudzone.cloudmq.api.open.exception.AuthFailedException;
import com.cloudzone.cloudmq.util.UtilAll;

import java.util.Properties;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class MQFactoryImpl implements MQFactoryAPI {
    public MQFactoryImpl() {
    }

    public Producer createProducer(Properties properties) {
        Properties prop = checkTopicAndAuthKey(properties, AuthkeyStatus.NORMAL_MSG, PropertiesConst.Keys.ProducerGroupId);
        if (null != prop) {
            return new ProducerImpl(prop);
        }
        return null;


    }

    public Consumer createConsumer(Properties properties) {
        Properties prop = checkTopicAndAuthKey(properties, AuthkeyStatus.NORMAL_MSG, PropertiesConst.Keys.ConsumerGroupId);
        if (null != prop) {
            return new ConsumerImpl(prop);
        }
        return null;
    }

    public OrderProducer createOrderProducer(Properties properties) {
        Properties prop = checkTopicAndAuthKey(properties, AuthkeyStatus.ORDER_MSG, PropertiesConst.Keys.ProducerGroupId);
        if (null != prop) {
            return new OrderProducerImpl(prop);
        }
        return null;
    }

    public OrderConsumer createOrderedConsumer(Properties properties) {
        Properties prop = checkTopicAndAuthKey(properties, AuthkeyStatus.ORDER_MSG, PropertiesConst.Keys.ConsumerGroupId);
        if (null != prop) {
            return new OrderConsumerImpl(prop);
        }
        return null;
    }

    /**
     * 当前版本目前还不支持事务
     *
     * @author tantexian
     * @params
     * @since 2016/7/12
     */
//    @Deprecated
    public TransactionProducer createTransactionProducer(Properties properties, final LocalTransactionChecker checker) {
        Properties prop = checkTopicAndAuthKey(properties, AuthkeyStatus.TRANSACTION_MSG, PropertiesConst.Keys.ProducerGroupId);
        if (null != prop) {
            TransactionProducerImpl transactionProducer = new TransactionProducerImpl(prop, new TransactionCheckListener() {
                public LocalTransactionState checkLocalTransactionState(MessageExt msg) {
                    String msgId = msg.getProperty("__transactionId__");
                    Msg message = MyUtils.msgConvert(msg, msgId);
                    message.setTransactionMsgId(msgId);
                    TransactionStatus check = checker.check(message);
                    return TransactionStatus.CommitTransaction == check ? LocalTransactionState.COMMIT_MESSAGE : (TransactionStatus.RollbackTransaction == check ? LocalTransactionState.ROLLBACK_MESSAGE : LocalTransactionState.UNKNOW);
                }
            });
            return transactionProducer;
        }
        return null;
    }

    private Properties checkTopicAndAuthKey(Properties properties, AuthkeyStatus authkeyStatus, String groupKey) {
        try {
            if (properties.containsKey(PropertiesConst.Keys.NAMESRV_ADDR)) {
                throw new AuthFailedException("Please remove NAMESRV_ADDR in properties");
            }
            // TODO: 2017/3/28 为了是兼容spring的对接方便
            if (!properties.containsKey(PropertiesConst.Keys.TOPIC_NAME) && properties.containsKey("TOPIC_NAME")) {
                properties.setProperty(PropertiesConst.Keys.TOPIC_NAME, properties.getProperty("TOPIC_NAME"));
                properties.remove("TOPIC_NAME");
            }
            if (!properties.containsKey(PropertiesConst.Keys.AUTH_KEY) && properties.containsKey("AUTH_KEY")) {
                properties.setProperty(PropertiesConst.Keys.AUTH_KEY, properties.getProperty("AUTH_KEY"));
                properties.remove("AUTH_KEY");
            }
            AuthKey authKey = Validators.checkTopicAndAuthKey(properties, groupKey);
            if (null != authKey) {
                String keyPrefix = UtilAll.frontStringAtLeast(authKey.getAuthKey(), 1);
                if ((authkeyStatus.getIndex() != Integer.valueOf(keyPrefix) &&
                        (Integer.valueOf(keyPrefix) != AuthkeyStatus.TRANSACTION_MSG.getIndex()
                                && authkeyStatus.getIndex() == AuthkeyStatus.NORMAL_MSG.getIndex()))) {
                    AuthkeyStatus authStatus = AuthkeyStatus.getAuthkeyStatus(Integer.valueOf(keyPrefix));
                    throw new AuthFailedException("调用方法错误，[" + authStatus.getName() + "]的topic，请调用[" + authStatus.getName() + "]的方法");
                } else {
                    Properties prop = new Properties();
                    if (PropertiesConst.Keys.ProducerGroupId.equals(groupKey)) {
                        prop.put(PropertiesConst.Keys.ProducerGroupId, properties.get(PropertiesConst.Keys.ProducerGroupId));
                    }
                    if (PropertiesConst.Keys.ConsumerGroupId.equals(groupKey)) {
                        prop.put(PropertiesConst.Keys.ConsumerGroupId, properties.get(PropertiesConst.Keys.ConsumerGroupId));
                    }
                    prop.put(PropertiesConst.Keys.TOPIC_NAME, properties.get(PropertiesConst.Keys.TOPIC_NAME));
                    prop.put(PropertiesConst.Keys.NAMESRV_ADDR, authKey.getIpAndPort());
                    return prop;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
