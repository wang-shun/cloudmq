package com.alibaba.rocketmq.common;

import java.lang.annotation.*;

/**
 * 字段展示注解
 *
 * @author gaoyanlei
 * @since 2016/7/12
 */


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface FieldMeta {
    /**
     * 展示字段名称
     *
     * @author gaoyanlei
     * @since 2016/7/12
     */
    String name() default "";
}
