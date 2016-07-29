package com.alibaba.rocketmq.domain.system;

/**
 * @author: tianyuliang
 * @since: 2016/7/27
 */
public class JavaInfo {

    private String version;
    private String vendor;
    private String home;
    private String specificationVersion;
    private String specificationVendor;
    private String specificationName;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getSpecificationVersion() {
        return specificationVersion;
    }

    public void setSpecificationVersion(String specificationVersion) {
        this.specificationVersion = specificationVersion;
    }

    public String getSpecificationVendor() {
        return specificationVendor;
    }

    public void setSpecificationVendor(String specificationVendor) {
        this.specificationVendor = specificationVendor;
    }

    public String getSpecificationName() {
        return specificationName;
    }

    public void setSpecificationName(String specificationName) {
        this.specificationName = specificationName;
    }
}
