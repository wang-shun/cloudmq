package com.gome.demo.springwithbean;

import com.gome.api.open.base.Action;
import com.gome.api.open.base.ConsumeContext;
import com.gome.api.open.base.Message;
import com.gome.api.open.base.MessageListener;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public class ConsumerMessageListener implements MessageListener {
    // 消费者订阅消费，建议业务程序自行记录生产及消费log日志，以方便您在无法正常收到消息情况下，可通过MQ控制台或者MQ日志查询消息并补发。
    public Action consume(Message message, ConsumeContext context) {
        System.out.println("Receive: " + new String(message.getBody()));
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
