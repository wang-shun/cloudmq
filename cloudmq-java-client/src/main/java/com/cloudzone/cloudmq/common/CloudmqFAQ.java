package com.cloudzone.cloudmq.common;

/**
 * @author GaoYanLei
 * @date 2016/7/5
 */
public class CloudmqFAQ {
    public static final String CONNECT_NAMESRV_FAILED = "Name Server 地址不存在,找不到MQ的Name Server！";
    public static final String CONNECT_BROKER_FAILED = "无法连接Broker，请检查网络设置！";
    public static final String TOPIC_NOT_EXIST = "获取不到Topic: %s的路由信息，如新建请联系管理员！";
    public static final String SEND_MSG_FAILED = "发送消息超时%dms，Topic： %s。请检查Namesrv,Broker连接是否正确！";
    public static final String BROKER_RESPONSE_EXCEPTION = "Broker返回异常！";
    public static final String MSG_CHECK_FAILED = "消息不合法！请检查是否为空。";
    public static final String PRODUCER_NOT_START = "producer没有启动或没有启动完成, producer状态:%s";
    public static final String PRODUCER_SHUT_DOWN = "请检查Service是否正常启动。producer状态:%s";
    public static final String SERVICE_EXCEPTION = "Service异常,请重启Service。producer状态:%s";


    public CloudmqFAQ() {

    }


    public static String errorMessage(String msg) {
        return String.format(msg);
    }
}
