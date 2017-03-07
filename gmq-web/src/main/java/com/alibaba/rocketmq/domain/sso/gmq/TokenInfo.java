package com.alibaba.rocketmq.domain.sso.gmq;

import java.io.Serializable;


/**
 * @author: tianyuliang
 * @since: 2016/12/2
 */
public class TokenInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    private String appKey;


    public TokenInfo(){

    }

    public TokenInfo(String token, String appKey){
        this.token = token;
        this.appKey = appKey;
    }

    public String getToken() {
        return token;
    }


    public void setToken(String token) {
        this.token = token;
    }


    public String getAppKey() {
        return appKey;
    }


    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }
}
