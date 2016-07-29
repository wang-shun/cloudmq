package com.alibaba.rocketmq.domain.system;

/**
 * @author gaoyanlei
 * @since 2016/7/25
 */
public class CpuInfo {
    private String vendor;
    private String mhz;
    private String model;
    private String user;
    private String system;
    private String idle;
    private String combined;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getVendor() {
        return vendor;
    }


    public void setVendor(String vendor) {
        this.vendor = vendor;
    }


    public String getMhz() {
        return mhz;
    }


    public void setMhz(String mhz) {
        this.mhz = mhz;
    }


    public String getModel() {
        return model;
    }


    public void setModel(String model) {
        this.model = model;
    }


    public String getSystem() {
        return system;
    }


    public void setSystem(String system) {
        this.system = system;
    }


    public String getIdle() {
        return idle;
    }


    public void setIdle(String idle) {
        this.idle = idle;
    }


    public String getCombined() {
        return combined;
    }


    public void setCombined(String combined) {
        this.combined = combined;
    }
}
