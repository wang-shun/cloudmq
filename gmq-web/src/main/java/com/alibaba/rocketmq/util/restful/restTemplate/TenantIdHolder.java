package com.alibaba.rocketmq.util.restful.restTemplate;

import org.springframework.util.Assert;


/**
 * @author gaoyanlei
 * @since 2016/7/25
 */
public final class TenantIdHolder {
    private static final ThreadLocal<String> tenantIdHolder = new ThreadLocal<String>();


    private TenantIdHolder() {
        throw new RuntimeException("Util class can't be instantiated");
    }


    public static void clearTenantId() {
        tenantIdHolder.remove();
    }


    public static String getTenantId() {
        return tenantIdHolder.get();
    }


    public static void setTenantId(String tenantId) {
        Assert.notNull(tenantId, "Only non-null tenant id are permitted");
        tenantIdHolder.set(tenantId);
    }

}
