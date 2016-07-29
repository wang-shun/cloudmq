package com.alibaba.rocketmq.domain.system;

/**
 * @author gaoyanlei
 * @since 2016/7/27
 */

public class EthernetInfo {
    private String name;
    private String address;
    private String broadcast;
    private String hwaddr;
    private String netmask;
    private String description;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }


    public void setAddress(String address) {
        this.address = address;
    }


    public String getBroadcast() {
        return broadcast;
    }


    public void setBroadcast(String broadcast) {
        this.broadcast = broadcast;
    }


    public String getHwaddr() {
        return hwaddr;
    }


    public void setHwaddr(String hwaddr) {
        this.hwaddr = hwaddr;
    }


    public String getNetmask() {
        return netmask;
    }


    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }
}
