/*
 * Copyright (c) 2015 wish.com All rights reserved.
 * 本软件源代码版权归----所有,未经许可不得任意复制与传播.
 */
package com.alibaba.rocketmq.domain.base;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础查询类
 *
 * @author ttx
 * @since 2015-06-16
 */
class BaseQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient Integer startIndex;// 开始索引

    private transient Integer endIndex;// 结束索引

    private transient String orderField;// 排序字段

    private transient BaseTypeEnum orderFieldTypeEnum;// 排序字段类型

    private transient Integer limitMin;  // Limit子句，从limitMin条开始查询

    private transient Integer limitMax;  // Limit子句，最多查询limitMax条

    private transient Map<String, Object> queryData;// 查询扩展

    private transient String keyword;// 关键字查询

    @JSONField(serialize = false)
    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    @JSONField(serialize = false)
    public Integer getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
    }

    //每页显示条数
    @JSONField(serialize = false)
    public Integer getPageSize() {
        if (endIndex != null && startIndex != null) {
            return endIndex - startIndex;
        }
        return null;
    }

    @JSONField(serialize = false)
    public String getOrderField() {
        return orderField;
    }

    public void setOrderField(String orderField) {
        this.orderField = orderField;
    }

    @JSONField(serialize = false)
    public String getOrderFieldType() {
        return orderFieldTypeEnum.getBaseType().toUpperCase();
    }

    @JSONField(serialize = false)
    public String getOrderFieldNextType() {
        return orderFieldTypeEnum.equals(BaseTypeEnum.ASC)? BaseTypeEnum.DESC.getBaseType() : BaseTypeEnum.ASC.getBaseType();
    }

    public void setOrderFieldType(BaseTypeEnum orderFieldTypeEnum) {
        this.orderFieldTypeEnum = orderFieldTypeEnum;
    }

    @JSONField(serialize = false)
    public Map<String, Object> getQueryData() {
        if (queryData != null && queryData.size() > 0) {
            return queryData;
        }
        return null;
    }

    //添加其它查询数据
    public void addQueryData(String key, Object value) {
        if (queryData == null) {
            queryData = new HashMap<String, Object>();
        }
        queryData.put(key, value);
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getkeyword() {
        return keyword;
    }

    public Integer getLimitMin() {
        return limitMin;
    }

    public void setLimitMin(Integer limitMin) {
        this.limitMin = limitMin;
    }

    public Integer getLimitMax() {
        return limitMax;
    }

    public void setLimitMax(Integer limitMax) {
        this.limitMax = limitMax;
    }
}
