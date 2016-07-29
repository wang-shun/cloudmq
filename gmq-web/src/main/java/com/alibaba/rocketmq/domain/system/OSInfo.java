package com.alibaba.rocketmq.domain.system;


/**
 * @author: tianyuliang
 * @since: 2016/7/27
 */
public class OSInfo {
    private String osVendorName;
    private String osArch;
    private String osDataModel;
    private String osDescription;
    private String osVendorVersion;
    private String osVersion;


    public String getOsVendorName() {
        return osVendorName;
    }


    public void setOsVendorName(String osVendorName) {
        this.osVendorName = osVendorName;
    }


    public String getOsDataModel() {
        return osDataModel;
    }


    public void setOsDataModel(String osDataModel) {
        this.osDataModel = osDataModel;
    }


    public String getOsDescription() {
        return osDescription;
    }


    public void setOsDescription(String osDescription) {
        this.osDescription = osDescription;
    }


    public String getOsVendorVersion() {
        return osVendorVersion;
    }


    public void setOsVendorVersion(String osVendorVersion) {
        this.osVendorVersion = osVendorVersion;
    }


    public String getOsArch() {
        return osArch;
    }


    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }


    public String getOsVersion() {
        return osVersion;
    }


    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }
}
