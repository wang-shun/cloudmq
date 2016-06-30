package com.gome.api.open.order;

import com.gome.api.open.base.Msg;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface MsgOrderListener {
    OrderAction consume(Msg msg, ConsumeOrderContext consumeOrderContext);
}
