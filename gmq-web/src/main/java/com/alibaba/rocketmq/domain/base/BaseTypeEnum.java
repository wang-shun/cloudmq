package com.alibaba.rocketmq.domain.base;

/**
 * SQL语句排序类型 DESC、ASC
 *
 * @author: tianyuliang
 * @since: 2016/7/29
 */
public enum BaseTypeEnum {

    DESC("DESC"),

    ASC("ASC");

    private String baseType;

    private BaseTypeEnum(String baseType) {
        this.baseType = baseType;
    }

    public String getBaseType() {
        return baseType;
    }

}
