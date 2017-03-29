package com.cloudzone.cloudmq.common;

/**
 * 验证接口返回对象
 * 
 * @author gaoyanlei
 * @since 2017/2/23
 */
public class ResultContent {
    public int code;
    public String body;
    public String msg;


    public int getCode() {
        return code;
    }


    public void setCode(int code) {
        this.code = code;
    }


    public String getBody() {
        return body;
    }


    public void setBody(String body) {
        this.body = body;
    }


    public String getMsg() {
        return msg;
    }


    public void setMsg(String msg) {
        this.msg = msg;
    }
}
