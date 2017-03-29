package com.cloudzone.cloudmq.api.impl.base;

import java.util.Properties;

import com.cloudzone.cloudmq.api.open.exception.GomeClientException;
import com.cloudzone.cloudmq.common.CloudmqFAQ;
import com.cloudzone.cloudmq.common.MyUtils;
import org.slf4j.Logger;

import com.alibaba.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.namesrv.TopAddressing;
import com.cloudzone.cloudmq.log.GClientLogger;


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


    public MQClientAbstract(Properties properties) {
        this.properties = properties;
        String property = this.properties.getProperty("NAMESRV_ADDR");
        if(property != null) {
            this.nameServerAddr = property;
        } else {
            if(null == this.nameServerAddr) {
                String addr = this.fetchNameServerAddr();
                if(null != addr) {
                    this.nameServerAddr = addr;
                }
            }

            if(null == this.nameServerAddr) {
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
        }
        else {
            top = new TopAddressing(WSADDR_INTERNAL);
            String nsAddrs = top.fetchNSAddr(false, WSADDR_INTERNAL_TIMEOUTMILLS);
            if (nsAddrs != null) {
                log.info("connected to internal server, {} success, {}", WSADDR_INTERNAL, nsAddrs);
                return nsAddrs;
            }
            else {
                top = new TopAddressing(WSADDR_INTERNET);
                nsAddrs = top.fetchNSAddr(false, WSADDR_INTERNET_TIMEOUTMILLS);
                if (nsAddrs != null) {
                    log.info("connected to internet server, {} success, {}", WSADDR_INTERNET, nsAddrs);
                    return nsAddrs;
                }
                else {
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

}
