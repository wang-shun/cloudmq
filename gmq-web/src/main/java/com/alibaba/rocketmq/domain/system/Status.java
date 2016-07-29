package com.alibaba.rocketmq.domain.system;

/**
 * @author tianyuliang
 * @since 2016/7/27
 */
public class Status {

    private String code;
    private String message;

    public String getCode() {
        return code;
    }


    public void setCode(String code) {
        this.code = code;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }
}
