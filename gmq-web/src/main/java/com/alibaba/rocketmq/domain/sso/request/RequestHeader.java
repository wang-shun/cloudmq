package com.alibaba.rocketmq.domain.sso.request;

import java.io.Serializable;

/**
 * @author: tianyuliang
 * @since: 2016/12/5
 */
public class RequestHeader implements Serializable {

    private static final long serialVersionUID = 1L;

    private String appKey;

    public RequestHeader(String appKey) {
        this.appKey = appKey;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }
}
