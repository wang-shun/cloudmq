package com.cloudzone.cloudmq.common;

/**
 * @author yintongjiang
 * @params
 * @since 2017/3/30
 */
public enum StatDataType {
    TPS(0, "tps"),
    FLOW(1, "flow");
    private int code;
    private String des;


    StatDataType(int code, String des) {
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
