package com.alibaba.rocketmq.util.restful.domian;

/**
 * @author gaoyanlei
 * @since 2016/7/25
 */

public class Status {
    private int code;
    private String message;


    public int getCode() {
        return code;
    }


    public void setCode(int code) {
        this.code = code;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }
}
