package com.cloudzone.cloudmq.demo.springwithbean;

import com.cloudzone.cloudmq.api.open.base.Action;
import com.cloudzone.cloudmq.api.open.base.ConsumeContext;
import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.base.MsgListener;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public class ConsumerMessageListener implements MsgListener {
    // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，
    // 以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
    public Action consume(Msg msg, ConsumeContext context) {
        System.out.println("Receive: " + new String(msg.getBody()));
        try {
            // do something..
            return Action.CommitMessage;
        }
        catch (Exception e) {
            // 消费失败，返回ReconsumeLater，消息被放置到重试队列，延时后下次重新消费
            return Action.ReconsumeLater;
        }
    }
}
