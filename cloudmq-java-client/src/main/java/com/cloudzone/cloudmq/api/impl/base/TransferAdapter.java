package com.cloudzone.cloudmq.api.impl.base;

/**
 * @author yintongjiang
 * @params转换适配器
 * @since 2017/4/13
 */
public interface TransferAdapter<T> {
    /**
     * 转换模板方法
     *
     * @param t
     * @return
     */
    boolean transfer(T t);
}
