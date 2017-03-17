package com.alibaba.rocketmq.domain.sso.request;

import java.io.Serializable;

/**
 * @author: tianyuliang
 * @since: 2016/12/5
 */
public class RequestBody implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    public RequestBody(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
