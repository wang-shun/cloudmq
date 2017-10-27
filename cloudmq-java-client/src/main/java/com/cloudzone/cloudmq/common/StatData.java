package com.cloudzone.cloudmq.common;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;


/**
 * @author yintongjiang
 * @params 统计数据
 * @since 2017/3/30
 */
public class StatData {

    /**
     * namespace或者topic
     */
    @JSONField(name = "name")
    private String name;

    /**
     * authKey
     */
    @JSONField(name = "key")
    private String authKey;

    /**
     * ip地址和进程号(格式ip#pid)
     */
    @JSONField(name = "id")
    private String ipAndPid;

    /**
     * 数量
     */
    @JSONField(name = "num")
    private Long dataNum;

    /**
     * 上传数据类型 0:TPS, 1:流量
     */
    @JSONField(name = "d_type")
    private int dataType;

    /**
     * 统计的单位 0,1等
     */
    @JSONField(name = "unit")
    private Integer timeUnit;

    /**
     * 统计类型 0:流入, 1:流出
     */
    @JSONField(name = "type")
    private Integer type;

    /**
     * 时间戳
     */
    @JSONField(name = "time")
    private Date timeStamp;


    public StatData(String name, String authKey, String ipAndPid, Long dataNum, int dataType,
            Integer timeUnit, Integer type, Date timeStamp) {
        this.name = name;
        this.authKey = authKey;
        this.ipAndPid = ipAndPid;
        this.dataNum = dataNum;
        this.dataType = dataType;
        this.timeUnit = timeUnit;
        this.type = type;
        this.timeStamp = timeStamp;
    }


    public StatData(String name, String ipAndPid, Long dataNum, int dataType, Integer timeUnit, Integer type,
            Date timeStamp) {
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
        String format = "StatData {name='%s', authKey='%s', ipAndPid='%s'";
        format += ", dataNum=%d, dataType=%d, timeUnit=%d, type=%d, timeStamp=%d}";

        Long ts = timeStamp.getTime();
        return String.format(format, name, authKey, ipAndPid, dataNum, dataType, timeUnit, type, ts);
    }
}
