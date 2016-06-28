package com.gome.api.impl.base;

import com.alibaba.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.namesrv.TopAddressing;
import com.gome.api.open.exception.GomeClientException;
import com.gome.common.FAQ;
import com.gome.common.MyUtils;
import org.slf4j.Logger;

import java.util.Properties;


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
    private static final Logger log = ClientLogger.getLog();
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
                throw new GomeClientException(FAQ.errorMessage("Can not find name server, May be your network problem.", "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&namesrv_not_exist"));
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
/*
        switch (MQClientAbstract.SyntheticClass_1.$SwitchMap$com$alibaba$rocketmq$common$ServiceState[producer
            .getServiceState().ordinal()]) {
        case 1:
            throw new GomeClientException(FAQ.errorMessage(
                String.format("You do not have start the producer[" + UtilAll.getPid() + "], %s",
                    new Object[] { producer.getServiceState() }),
                "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&service_not_ok"));
        case 2:
            throw new GomeClientException(FAQ.errorMessage(
                String.format("Your producer has been shut down, %s",
                    new Object[] { producer.getServiceState() }),
                "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&service_not_ok"));
        case 3:
            throw new GomeClientException(FAQ.errorMessage(
                String.format("When you start your service throws an exception, %s",
                    new Object[] { producer.getServiceState() }),
                "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&service_not_ok"));
        case 4:
        default:
        }
*/
    }

}
