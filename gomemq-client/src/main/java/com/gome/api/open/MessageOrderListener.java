package com.gome.api.open;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface MessageOrderListener {
    OrderAction consume(Message msg, ConsumeOrderContext consumeOrderContext);
}
