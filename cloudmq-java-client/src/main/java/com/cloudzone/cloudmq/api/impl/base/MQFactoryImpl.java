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

import java.util.Map;
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
            AuthKey authKey = Validators.checkTopicAndAuthKey(properties, groupKey);
            if (null != authKey) {
                for (Map.Entry<String, String> entry : authKey.getTopicAndAuthKey().getTopicAuthKeyMap().entrySet()) {
                    String aKey = entry.getValue();
                    String keyPrefix = UtilAll.frontStringAtLeast(aKey, 1);
                    if (authkeyStatus.getIndex() != Integer.valueOf(keyPrefix) &&
                            // TODO: 2017/3/29 事务消息，延迟消息，sendoneway，普通消息的消费都是一个类型需特殊处理
                            ((Integer.valueOf(keyPrefix) != AuthkeyStatus.TRANSACTION_MSG.getIndex() &&
                                    Integer.valueOf(keyPrefix) != AuthkeyStatus.DELAY_MSG.getIndex() &&
                                    Integer.valueOf(keyPrefix) != AuthkeyStatus.SENDONEWAY.getIndex()) ||
                                    authkeyStatus.getIndex() != AuthkeyStatus.NORMAL_MSG.getIndex() ||
                                    !groupKey.equals(PropertiesConst.Keys.ConsumerGroupId)) &&
                            // TODO: 2017/3/29 延迟消息，sendoneway普通消息的发送时一个类型需特殊处理
                            ((Integer.valueOf(keyPrefix) != AuthkeyStatus.DELAY_MSG.getIndex() &&
                                    Integer.valueOf(keyPrefix) != AuthkeyStatus.SENDONEWAY.getIndex()) ||
                                    authkeyStatus.getIndex() != AuthkeyStatus.NORMAL_MSG.getIndex() ||
                                    !groupKey.equals(PropertiesConst.Keys.ProducerGroupId)
                            )) {
                        AuthkeyStatus authStatus = AuthkeyStatus.getAuthkeyStatus(Integer.valueOf(keyPrefix));
                        throw new AuthFailedException("调用方法错误，[" + authStatus.getName() + "]的topic，请调用[" + authStatus.getName() + "]的方法");
                    }
                }
                Properties prop = new Properties();
                if (PropertiesConst.Keys.ProducerGroupId.equals(groupKey)) {
                    prop.put(PropertiesConst.Keys.ProducerGroupId, properties.get(PropertiesConst.Keys.ProducerGroupId));
                }
                if (PropertiesConst.Keys.ConsumerGroupId.equals(groupKey)) {
                    prop.put(PropertiesConst.Keys.ConsumerGroupId, properties.get(PropertiesConst.Keys.ConsumerGroupId));
                }
                if (properties.containsKey(PropertiesConst.Keys.MessageModel)) {
                    prop.put(PropertiesConst.Keys.MessageModel, properties.get(PropertiesConst.Keys.MessageModel));
                }
                prop.put(PropertiesConst.Keys.TopicAndAuthKey, authKey.getTopicAndAuthKey());
                prop.put(PropertiesConst.Keys.NAMESRV_ADDR, authKey.getIpAndPort());
                return prop;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
