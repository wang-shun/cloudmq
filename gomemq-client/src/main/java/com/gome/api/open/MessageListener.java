package com.gome.api.open;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface MessageListener {
    Action consume(Message msg, ConsumeContext consumeContext);
}
