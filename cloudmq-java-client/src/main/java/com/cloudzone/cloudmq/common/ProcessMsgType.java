package com.cloudzone.cloudmq.common;

/**
 * @author yintongjiang
 * @params
 * @since 2017/3/30
 */
public enum ProcessMsgType {
    PRODUCER_MSG(0, "发送"),
    CONSUMER_MSG(1, "消费");
    // 成员变量
    private int code;
    private String des;


    private ProcessMsgType(int code, String des) {
        this.code = code;
        this.des = des;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }
}
