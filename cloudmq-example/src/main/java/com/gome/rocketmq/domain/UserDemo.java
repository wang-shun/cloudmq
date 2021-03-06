/*
 * Copyright (c) 2015 wish.com All rights reserved.
 * 本软件源代码版权归----所有,未经许可不得任意复制与传播.
 */
package com.gome.rocketmq.domain;

import com.gome.rocketmq.domain.base.BaseDomain;

import java.util.Date;

/**
 * userDemo
 * @author ttx
 * @since 2015-06-16
 */
public class UserDemo extends BaseDomain {
	private static final long serialVersionUID = 1L;
	private String userName;
	private String password;
	private Date createdTime;
	private Date updatedTime;
	private Date deletedTime;
	private Integer deleted;

	public UserDemo(){
		//默认无参构造方法
	}

	/**
	 * 获取 userName
	 * @return
	 */
	public String getUserName(){
		return userName;
	}
	
	/**
	 * 设置 userName
	 * @param userName
	 */
	public void setUserName(String userName){
		this.userName = userName;
	}

	/**
	 * 获取 password
	 * @return
	 */
	public String getPassword(){
		return password;
	}
	
	/**
	 * 设置 password
	 * @param password
	 */
	public void setPassword(String password){
		this.password = password;
	}

	/**
	 * 获取 createdTime
	 * @return
	 */
	public Date getCreatedTime(){
		return createdTime;
	}
	
	/**
	 * 设置 createdTime
	 * @param createdTime
	 */
	public void setCreatedTime(Date createdTime){
		this.createdTime = createdTime;
	}

	/**
	 * 获取 updatedTime
	 * @return
	 */
	public Date getUpdatedTime(){
		return updatedTime;
	}
	
	/**
	 * 设置 updatedTime
	 * @param updatedTime
	 */
	public void setUpdatedTime(Date updatedTime){
		this.updatedTime = updatedTime;
	}

	/**
	 * 获取 deletedTime
	 * @return
	 */
	public Date getDeletedTime(){
		return deletedTime;
	}
	
	/**
	 * 设置 deletedTime
	 * @param deletedTime
	 */
	public void setDeletedTime(Date deletedTime){
		this.deletedTime = deletedTime;
	}

	/**
	 * 获取 deleted
	 * @return
	 */
	public Integer getDeleted(){
		return deleted;
	}
	
	/**
	 * 设置 deleted
	 * @param deleted
	 */
	public void setDeleted(Integer deleted){
		this.deleted = deleted;
	}
	@Override
    public String toString() {
		StringBuffer buf = new StringBuffer("UserDemo=[");
				buf.append("userName=").append(getUserName()).append(", ");
				buf.append("password=").append(getPassword()).append(", ");
				buf.append("createdTime=").append(getCreatedTime()).append(", ");
				buf.append("updatedTime=").append(getUpdatedTime()).append(", ");
				buf.append("deletedTime=").append(getDeletedTime()).append(", ");
				buf.append("deleted=").append(getDeleted()).append(", ");
				buf.append("]");
		return buf.toString();
	}
}