package com.gome.mq.springwithbean;

import com.gome.api.open.base.Action;
import com.gome.api.open.base.ConsumeContext;
import com.gome.api.open.base.Message;
import com.gome.api.open.base.MessageListener;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public class ConsumerMessageListener implements MessageListener {
    public Action consume(Message message, ConsumeContext context) {
        System.out.println("Receive: " + message);
        try {
            // do something..
            return Action.CommitMessage;
        }
        catch (Exception e) {
            // 消费失败
            return Action.ReconsumeLater;
        }
    }
}
