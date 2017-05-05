package com.cloudzone.cloudmq.common;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class StatData {
    // namespace或者topic
    @JSONField(name = "name")
    private String name;
    // authKey
    @JSONField(name = "key")
    private String authKey;
    // ip和进程号格式ip#pid
    @JSONField(name = "id")
    private String ipAndPid;
    // 数量
    @JSONField(name = "num")
    private Long dataNum;
    // 上传数据类型(0:为TPS，1：为流量）
    @JSONField(name = "d_type")
    private int dataType;
    // 统计的单位,0,1等
    @JSONField(name = "unit")
    private Integer timeUnit;
    // 统计类型,0流入,1流出
    @JSONField(name = "type")
    private Integer type;
    // 时间戳
    @JSONField(name = "time")
    private Date timeStamp;

    public StatData(String name, String authKey, String ipAndPid, Long dataNum, int dataType, Integer timeUnit, Integer type, Date timeStamp) {
        this.name = name;
        this.authKey = authKey;
        this.ipAndPid = ipAndPid;
        this.dataNum = dataNum;
        this.dataType = dataType;
        this.timeUnit = timeUnit;
        this.type = type;
        this.timeStamp = timeStamp;
    }

    public StatData(String name, String ipAndPid, Long dataNum, int dataType, Integer timeUnit, Integer type, Date timeStamp) {
        this.name = name;
        this.ipAndPid = ipAndPid;
        this.dataNum = dataNum;
        this.dataType = dataType;
        this.timeUnit = timeUnit;
        this.type = type;
        this.timeStamp = timeStamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getIpAndPid() {
        return ipAndPid;
    }

    public void setIpAndPid(String ipAndPid) {
        this.ipAndPid = ipAndPid;
    }

    public Long getDataNum() {
        return dataNum;
    }

    public void setDataNum(Long dataNum) {
        this.dataNum = dataNum;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public Integer getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(Integer timeUnit) {
        this.timeUnit = timeUnit;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "StatData{" +
                "name='" + name + '\'' +
                ", authKey='" + authKey + '\'' +
                ", ipAndPid='" + ipAndPid + '\'' +
                ", dataNum=" + dataNum +
                ", dataType=" + dataType +
                ", timeUnit=" + timeUnit +
                ", type=" + type +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
