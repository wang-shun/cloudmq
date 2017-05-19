package com.cloudzone.cloudmq.api.impl.base;

import com.alibaba.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.namesrv.TopAddressing;
import com.cloudzone.cloudlimiter.base.FlowUnit;
import com.cloudzone.cloudlimiter.factory.CloudFactory;
import com.cloudzone.cloudlimiter.limiter.FlowLimiter;
import com.cloudzone.cloudlimiter.limiter.LimiterDelayConstants;
import com.cloudzone.cloudlimiter.limiter.RealTimeLimiter;
import com.cloudzone.cloudlimiter.meter.CloudMeter;
import com.cloudzone.cloudmq.api.impl.heartbeat.MQHeartbeatListenerImpl;
import com.cloudzone.cloudmq.api.impl.meter.MeterFactory;
import com.cloudzone.cloudmq.api.impl.meter.MeterTopicExt;
import com.cloudzone.cloudmq.api.impl.producer.ProducerFactory;
import com.cloudzone.cloudmq.api.impl.synctime.SyncTimeFactory;
import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.exception.AuthFailedException;
import com.cloudzone.cloudmq.api.open.exception.GomeClientException;
import com.cloudzone.cloudmq.common.*;
import com.cloudzone.cloudmq.log.GClientLogger;
import org.slf4j.Logger;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public abstract class MQClientAbstract {
    protected static final String WSADDR_INTERNAL =
            System.getProperty("com.aliyun.openservices.ons.addr.internal",
                    "http://onsaddr-internal.aliyun.com:8080/rocketmq/nsaddr4client-internal");
    protected static final String WSADDR_INTERNET =
            System.getProperty("com.aliyun.openservices.ons.addr.internet",
                    "http://onsaddr-internet.aliyun.com/rocketmq/nsaddr4client-internet");
    protected static final long WSADDR_INTERNAL_TIMEOUTMILLS = Long
            .parseLong(System.getProperty("com.aliyun.openservices.ons.addr.internal.timeoutmills", "3000"));
    protected static final long WSADDR_INTERNET_TIMEOUTMILLS = Long
            .parseLong(System.getProperty("com.aliyun.openservices.ons.addr.internet.timeoutmills", "5000"));
    private static final Logger log = GClientLogger.getLog();
    protected final Properties properties;
    //protected final SessionCredentials sessionCredentials = new SessionCredentials();
    protected String nameServerAddr = MyUtils.getNamesrvAddr();

    private final AtomicBoolean startScheduleFlag = new AtomicBoolean(false);
    private final AtomicBoolean startSyncTimeScheduleFlag = new AtomicBoolean(false);
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryImpl("CLOUDMQ_JAVA_CLIENT_"));
    private ScheduledExecutorService scheduledExecutorSyncTimeService = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryImpl("CLOUDMQ_JAVA_CLIENT_SYNCTIME_"));
    private final CloudMeter cloudMeter = MeterFactory.getCloudMeterSingleton();
    private final ConcurrentHashMap<String, RealTimeLimiter> topicLimiterMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FlowLimiter> topicFlowLimiterMap = new ConcurrentHashMap<>();
    private TopicAndAuthKey topicAndAuthKey;
    private MQHeartbeatListener mqHeartbeatListener;
    private MQSyncTimeListener mqSyncTimeListener;
    private final static int ORG_STEP = 60;
    private final static int SYNC_TIME_STEP = 60;
    //默认或出现异常限制一万的TPS
    private final static int DEFAULT_PERMITS_PER_SECOND = 10000;
    //默认或出现异常限制的128*1024*1000 Byte的流量
    private final static int DEFAULT_FLOW_PERMITS_PER_SECOND = 128 * 1024 * 10000;

    private void registerListener(MQHeartbeatListener mqHeartbeatListener) {
        this.mqHeartbeatListener = mqHeartbeatListener;
    }

    private void registerListener(MQSyncTimeListener mqSyncTimeListener) {
        this.mqSyncTimeListener = mqSyncTimeListener;
    }

    public MQClientAbstract(Properties properties) {
        topicAndAuthKey = (TopicAndAuthKey) properties.get(PropertiesConst.Keys.InnerTopicAndAuthKey);
        this.properties = properties;
        String property = this.properties.getProperty("NAMESRV_ADDR");
        if (property != null) {
            this.nameServerAddr = property;
        } else {
            if (null == this.nameServerAddr) {
                String addr = this.fetchNameServerAddr();
                if (null != addr) {
                    this.nameServerAddr = addr;
                }
            }

            if (null == this.nameServerAddr) {
                // 修改异常信息 2016/7/5 Add by GaoYanLei
                // throw new GomeClientException(FAQ.errorMessage("Can not find
                // name server, May be your network problem.",
                // "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&namesrv_not_exist"));
                throw new GomeClientException(CloudmqFAQ.errorMessage(CloudmqFAQ.CONNECT_NAMESRV_FAILED));
            }
        }
    }


    protected String fetchNameServerAddr() {
        String property = this.properties.getProperty("ONSAddr");
        TopAddressing top;
        if (property != null) {
            top = new TopAddressing(property);
            return top.fetchNSAddr();
        } else {
            top = new TopAddressing(WSADDR_INTERNAL);
            String nsAddrs = top.fetchNSAddr(false, WSADDR_INTERNAL_TIMEOUTMILLS);
            if (nsAddrs != null) {
                log.info("connected to internal server, {} success, {}", WSADDR_INTERNAL, nsAddrs);
                return nsAddrs;
            } else {
                top = new TopAddressing(WSADDR_INTERNET);
                nsAddrs = top.fetchNSAddr(false, WSADDR_INTERNET_TIMEOUTMILLS);
                if (nsAddrs != null) {
                    log.info("connected to internet server, {} success, {}", WSADDR_INTERNET, nsAddrs);
                    return nsAddrs;
                } else {
                    return null;
                }
            }
        }
    }


    protected String buildIntanceName() {
        return Integer.toString(UtilAll.getPid()) + "#" + this.nameServerAddr.hashCode() + "#" + System.nanoTime();
    }


    public String getNameServerAddr() {
        return this.nameServerAddr;
    }


    protected void checkONSProducerServiceState(DefaultMQProducerImpl producer) {
        // 修改异常信息 2016/7/5 Add by GaoYanLei

        switch (producer.getServiceState()) {
            case CREATE_JUST:
                throw new GomeClientException(CloudmqFAQ.errorMessage(
                        String.format(CloudmqFAQ.PRODUCER_NOT_START, new Object[]{producer.getServiceState()})));
            case SHUTDOWN_ALREADY:
                throw new GomeClientException(CloudmqFAQ.errorMessage(
                        String.format(CloudmqFAQ.PRODUCER_SHUT_DOWN, new Object[]{producer.getServiceState()})));
            case START_FAILED:
                throw new GomeClientException(CloudmqFAQ.errorMessage(
                        String.format(CloudmqFAQ.SERVICE_EXCEPTION, new Object[]{producer.getServiceState()})));
            default:
        }
    }

    protected void shutdownSchedule() {
        // 关闭统计定时器 2017/4/14 Add by yintongqiang
        cloudMeter.shutdown();
        // 关闭内部使用的producer 2017/4/14 Add by yintongqiang
        ProducerFactory.getProducerSingleton().shutdown();
        if (this.startScheduleFlag.compareAndSet(true, false)) {
            this.scheduledExecutorService.shutdown();
        }
        if (this.startSyncTimeScheduleFlag.compareAndSet(true, false)) {
            this.scheduledExecutorSyncTimeService.shutdown();
        }
    }

    protected void startSchedule() {
        // 开启内部使用的producer 2017/4/14 Add by yintongqiang
        ProducerFactory.getProducerSingleton().start();
        for (String topic : topicAndAuthKey.getTopicArray()) {
            topicLimiterMap.put(topic, CloudFactory.createRealTimeLimiter(DEFAULT_PERMITS_PER_SECOND));
            topicFlowLimiterMap.put(topic, CloudFactory.createFlowLimiterPerSecond(DEFAULT_FLOW_PERMITS_PER_SECOND, FlowUnit.BYTE));
        }
        if (this.startScheduleFlag.compareAndSet(false, true)) {
            startHeartbeat();
        }
        if (this.startSyncTimeScheduleFlag.compareAndSet(false, true)) {
            startSyncTime();
        }
    }

    // 定时同步服务端和客户端的时间
    private void startSyncTime() {
        if (!SyncTimeFactory.isRegister()) {
            this.registerListener(SyncTimeFactory.getSyncTimeListenerSingleton());
            this.scheduledExecutorSyncTimeService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    mqSyncTimeListener.syncTime(System.currentTimeMillis());
                }
            }, 0, SYNC_TIME_STEP, TimeUnit.SECONDS);
        }
    }

    // 心跳定时任务，定时去cloudzone获取心跳信息进行限流操作
    private void startHeartbeat() {
        this.registerListener(new MQHeartbeatListenerImpl());
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    for (String topic : topicAndAuthKey.getTopicArray()) {
                        HeartbeatData heartbeatData = mqHeartbeatListener.doHeartbeat(topic, topicAndAuthKey.getTopicAuthKeyMap().get(topic));
                        // 为了不影响客户端使用，客户端连不上服务端时，设置默认值
                        if (null == heartbeatData) {
                            heartbeatData = new HeartbeatData(DEFAULT_PERMITS_PER_SECOND, DEFAULT_FLOW_PERMITS_PER_SECOND, 1, 1);
                        }
                        // 判断是否用尽，剩余时间是否用尽
                        boolean isUseUp = heartbeatData.getTps() == 0 || heartbeatData.getFlowTps() == 0 ||
                                heartbeatData.getSurplusFlow() == 0 || heartbeatData.getSurplusTime() == 0;
                        // 用尽
                        if (isUseUp) {
                            // tps限成一秒钟一条
                            if (topicLimiterMap.containsKey(topic)) {
                                topicLimiterMap.get(topic).setRate(LimiterDelayConstants.ONCE_PER_SECOND);
                            } else {
                                topicLimiterMap.put(topic, CloudFactory.createRealTimeLimiter(LimiterDelayConstants.ONCE_PER_SECOND));
                            }
                            // 流量限成一秒钟一字节
                            if (topicFlowLimiterMap.containsKey(topic)) {
                                topicFlowLimiterMap.get(topic).setRate(1, FlowUnit.BYTE);
                            } else {
                                topicFlowLimiterMap.put(topic, CloudFactory.createFlowLimiterPerSecond(1, FlowUnit.BYTE));
                            }
                        }
                        // tps没有用尽，并且tps大于0，(限流的rate不能设置为小于0的值，不然要报错)
                        if (heartbeatData.getTps() > 0 && !isUseUp) {
                            if (!topicLimiterMap.containsKey(topic)) {
                                topicLimiterMap.put(topic, CloudFactory.createRealTimeLimiter(heartbeatData.getTps()));
                                // 当前tps和rate不等，才进行重新设值，不然会导致统计有波动
                            } else if (heartbeatData.getTps() != topicLimiterMap.get(topic).getRate()) {
                                topicLimiterMap.get(topic).setRate(heartbeatData.getTps());
                            }

                        }
                        // 流量tps没有用尽，并且流量tps大于0，(限流的rate不能设置为小于0的值，不然要报错)
                        if (heartbeatData.getFlowTps() > 0 && !isUseUp) {
                            if (!topicFlowLimiterMap.containsKey(topic)) {
                                topicFlowLimiterMap.put(topic, CloudFactory.createFlowLimiterPerSecond(heartbeatData.getFlowTps(), FlowUnit.BYTE));
                                // 当前流量tps和rate不等，才进行重新设值，不然会导致统计有波动
                            } else if (heartbeatData.getFlowTps() != topicFlowLimiterMap.get(topic).getRate()) {
                                topicFlowLimiterMap.get(topic).setRate(heartbeatData.getFlowTps(), FlowUnit.BYTE);
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, ORG_STEP, TimeUnit.SECONDS);
    }

    // 发送或消费消息时候对topic进行校验和统计限流 2017/3/29 Add by yintongqiang
    protected void checkTopic(Properties properties, String topic, Msg msg) {
        if (!topicAndAuthKey.getTopicAuthKeyMap().containsKey(topic)) {
            throw new AuthFailedException("申请的topic和" + topicAndAuthKey.getProcessMsgType().getDes() + "的topic不匹配,申请的topic为[" + topicAndAuthKey.topicArrayToString() + "]," + topicAndAuthKey.getProcessMsgType().getDes() + "的topic为[" + topic + "]");
        }
        String authKey = topicAndAuthKey.getTopicAuthKeyMap().get(topic);
        // 限制tps
        if (null != msg && topicLimiterMap.containsKey(topic)) {
            topicLimiterMap.get(topic).acquire();
        }
        // 限制流量
        if (null != msg && topicFlowLimiterMap.containsKey(topic)) {
            topicFlowLimiterMap.get(topic).acquire(msg.getBody().length, FlowUnit.BYTE);
        }

        if (null != msg) {
            // 统计tps
            cloudMeter.request(new MeterTopicExt(topic, authKey, StatDataType.TPS, topicAndAuthKey.getProcessMsgType()));
            // 统计流量
            cloudMeter.request(new MeterTopicExt(topic, authKey, StatDataType.FLOW, topicAndAuthKey.getProcessMsgType()), msg.getBody().length);
        }
    }
}
