package com.cloudzone.cloudmq.api.open.order;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public enum OrderAction {
    Success, // 消费成功消息
    Suspend; //消费失败，返回Suspend，消息被放置到重试队列，延时后下次重新消费

    private OrderAction() {
    }
}
