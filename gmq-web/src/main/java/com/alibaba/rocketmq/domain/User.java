/*
 * Copyright (c) 2015 wish.com All rights reserved.
 * 本软件源代码版权归----所有,未经许可不得任意复制与传播.
 */
package com.alibaba.rocketmq.domain;


import com.alibaba.rocketmq.common.FieldMeta;
import com.alibaba.rocketmq.domain.base.BaseDomain;

import java.util.Date;

/**
 * @author gaoyanlei
 * @since 2016/7/12
 */
public class User extends BaseDomain {
    private static final long serialVersionUID = 1L;
    @FieldMeta(name = "真实姓名")
    private String realName;
    @FieldMeta(name = "用户名")
    private String userName;
    @FieldMeta(name = "123")
    private String password;
    @FieldMeta(name = "创建时间")
    private Date createdTime;
    private Date updatedTime;
    private Date deletedTime;
    private int state;

    public User() {
        //默认无参构造方法
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Date getDeletedTime() {
        return deletedTime;
    }

    public void setDeletedTime(Date deletedTime) {
        this.deletedTime = deletedTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("User=[");
        buf.append("realName=").append(getRealName()).append(", ");
        buf.append("userName=").append(getUserName()).append(", ");
        buf.append("password=").append(getPassword()).append(", ");
        buf.append("createdTime=").append(getCreatedTime()).append(", ");
        buf.append("updatedTime=").append(getUpdatedTime()).append(", ");
        buf.append("deletedTime=").append(getDeletedTime()).append(", ");
        buf.append("]");
        return buf.toString();
    }
}