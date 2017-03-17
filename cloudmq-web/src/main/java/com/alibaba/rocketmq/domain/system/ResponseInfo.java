package com.alibaba.rocketmq.domain.system;

/**
 * @author: tianyuliang
 * @since: 2016/7/27
 */
public class ResponseInfo {

    private Status status;

    private ResponseData data;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ResponseData getData() {
        return data;
    }

    public void setData(ResponseData data) {
        this.data = data;
    }


}
