package com.alibaba.rocketmq.domain.sso.request;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.rocketmq.domain.sso.gmq.TokenInfo;
import com.google.common.collect.Maps;


/**
 * @author: tianyuliang
 * @since: 2016/12/2
 */
public class RequestData implements Serializable {

    private static final long serialVersionUID = -5385196012551768394L;

    private String token;

    /**
     * 额外字段，暂时保留
     */
    private Object data;


    public RequestData() {
        super();
    }


    public RequestData(String token) {
        this.token = token;
    }


    public RequestData(TokenInfo ssoToken) {
        this.token = ssoToken.getToken();
    }


    public RequestData(String token, Object data) {
        this.token = token;
        this.data = data;
    }


    public Map<String, Object> buildVerifyToken(TokenInfo ssoToken) {
        Map<String, Object> token = Maps.newHashMap();
        token.put("token", ssoToken.getToken());

        JSONObject message = new JSONObject();
        message.put("body", token);

        return message;
    }


    public String getToken() {
        return token;
    }


    public void setToken(String token) {
        this.token = token;
    }


    public Object getData() {
        return data;
    }


    public void setData(Object data) {
        this.data = data;
    }


    @Override
    public String toString() {
        return MessageFormat.format("RequestData{token={1}, data={2}}", token, data);
    }
}
