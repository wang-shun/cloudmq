package com.cloudzone.cloudmq.api.open.base;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public enum Action {
    CommitMessage, // 消费成功消息
    ReconsumeLater;  //消费失败，返回ReconsumeLater，消息被放置到重试队列，延时后下次重新消费

    private Action() {}
}
