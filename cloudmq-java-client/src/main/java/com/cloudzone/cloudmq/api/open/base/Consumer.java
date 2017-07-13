package com.cloudzone.cloudmq.api.open.base;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface Consumer extends Admin {
    void start();

    void shutdown();

    /**
     * 订阅消息
     *
     * @author tantexian
     * @since 2016/6/28
     * @params topic 订阅主题
     * @params tag 订阅tag，"*":表示订阅主题下所有tag、"TagA || TagB":表示订阅主题下TagA及TagB
     * @params msgListener 消息接收监听器
     */
    void subscribe(String topic, String tag, MsgListener msgListener);

    /**
     * 取消订阅消息
     *
     * @author tantexian
     * @since 2016/6/28
     * @params
     */
    void unsubscribe(String topic);
}
