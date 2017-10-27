package com.cloudzone.cloudmq.api.open.order;

import com.cloudzone.cloudmq.api.open.base.Msg;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface MsgOrderListener {
    OrderAction consume(Msg msg, ConsumeOrderContext consumeOrderContext);
}
