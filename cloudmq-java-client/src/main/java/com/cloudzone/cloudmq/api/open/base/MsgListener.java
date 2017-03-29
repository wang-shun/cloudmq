package com.cloudzone.cloudmq.api.open.base;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface MsgListener {
    Action consume(Msg msg, ConsumeContext consumeContext);
}
