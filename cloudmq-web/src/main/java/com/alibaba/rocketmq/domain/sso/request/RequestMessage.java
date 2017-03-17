package com.alibaba.rocketmq.domain.sso.request;

import java.io.Serializable;

import com.alibaba.rocketmq.domain.sso.gmq.TokenInfo;


/**
 * @author: tianyuliang
 * @since: 2016/12/5
 */
public class RequestMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private RequestBody body;

    private RequestHeader header;


    public RequestMessage() {
    }


    public RequestMessage(RequestBody body, RequestHeader header) {
        this.body = body;
        this.header = header;
    }


    public RequestMessage buildVerifyToken(TokenInfo ssoToken) {
        RequestMessage message = new RequestMessage();
        message.setBody(new RequestBody(ssoToken.getToken()));
        message.setHeader(new RequestHeader(ssoToken.getAppKey()));
        return message;
    }


    public RequestBody getBody() {
        return body;
    }


    public void setBody(RequestBody body) {
        this.body = body;
    }


    public RequestHeader getHeader() {
        return header;
    }


    public void setHeader(RequestHeader header) {
        this.header = header;
    }
}
