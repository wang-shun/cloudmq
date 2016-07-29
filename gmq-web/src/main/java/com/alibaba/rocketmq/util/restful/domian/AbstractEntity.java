package com.alibaba.rocketmq.util.restful.domian;

import com.alibaba.rocketmq.util.restful.domian.Status;

/**
 * @author gaoyanlei
 * @since 2016/7/25
 */
public class AbstractEntity<E> {
    private Status status;
    private Object data;


    public Status getStatus() {
        return status;
    }


    public void setStatus(Status status) {
        this.status = status;
    }


    public Object getData() {
        return data;
    }


    public void setData(Object data) {
        this.data = data;
    }
}
