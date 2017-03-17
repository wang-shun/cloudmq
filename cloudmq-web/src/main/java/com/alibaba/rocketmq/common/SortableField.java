package com.alibaba.rocketmq.common;

import java.lang.reflect.Field;

/**
 * 获取到注解的帮助类
 *
 * @author gaoyanlei
 * @since 2016/7/12
 */
public class SortableField {
    public SortableField() {
    }

    public SortableField(FieldMeta meta, Field field) {
        super();
        this.meta = meta;
        this.field = field;
        this.name = field.getName();
        this.type = field.getType();
    }


    public SortableField(FieldMeta meta, String name, Class<?> type) {
        super();
        this.meta = meta;
        this.name = name;
        this.type = type;
    }


    private FieldMeta meta;
    private Field field;
    private String name;
    private Class<?> type;

    public FieldMeta getMeta() {
        return meta;
    }

    public void setMeta(FieldMeta meta) {
        this.meta = meta;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }
}
