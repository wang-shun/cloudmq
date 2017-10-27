package com.cloudzone.cloudmq.api.open.order;

import com.cloudzone.cloudmq.api.open.base.Admin;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface OrderConsumer extends Admin {

    /**
     * 启动订阅消息
     *
     * @author tantexian
     * @since 2016/6/28
     * @params
     */
    void start();


    /**
     * 停止订阅消息
     *
     * @author tantexian
     * @since 2016/6/28
     * @params
     */
    void shutdown();


    /**
     * @author tantexian
     * @since 2016/6/28
     * @params topic 订阅主题
     * @params tag 订阅tag，"*":表示订阅主题下所有tag、"TagA | TagB":表示订阅主题下TagA及TagB
     * @params msgOrderListener 顺序消息接收监听器
     */
    void subscribe(String topic, String tag, MsgOrderListener msgOrderListener);
}
