package com.cloudzone.cloudmq.common;

/**
 * @author yintongjiang
 * @params 统计单位
 * @since 2017/3/30
 */
public enum TimeUnitType {
    SECONDS(0, "秒"),
    MINUTES(1, "分");
    private int code;
    private String des;


    TimeUnitType(int code, String des) {
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
