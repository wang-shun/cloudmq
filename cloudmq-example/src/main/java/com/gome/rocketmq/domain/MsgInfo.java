/*
 * Copyright (c) 2015 wish.com All rights reserved.
 * 本软件源代码版权归----所有,未经许可不得任意复制与传播.
 */
package com.gome.rocketmq.domain;

import com.gome.rocketmq.domain.base.BaseDomain;

import java.util.Date;

/**
 * @author tantexian
 * @since 2016/6/15
 * @params
 */
public class MsgInfo extends BaseDomain {
	private static final long serialVersionUID = 1L;
	private String topic;
	private String key;
	private Integer bodyHashcode;
	private String body;
	private Integer repeatNum;
	private Date createdTime;
	private Date updatedTime;
	private Date deletedTime;
	private Integer deleted;

	public Integer getRepeatNum() {
		return repeatNum;
	}

	public void setRepeatNum(Integer repeatNum) {
		this.repeatNum = repeatNum;
	}

	public MsgInfo(){
		//默认无参构造方法
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Integer getBodyHashcode() {
		return bodyHashcode;
	}

	public void setBodyHashcode(Integer bodyHashcode) {
		this.bodyHashcode = bodyHashcode;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getTopic() {

		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
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
				buf.append("topic=").append(getTopic()).append(", ");
				buf.append("msg_key=").append(getKey()).append(", ");
				buf.append("createdTime=").append(getCreatedTime()).append(", ");
				buf.append("updatedTime=").append(getUpdatedTime()).append(", ");
				buf.append("deletedTime=").append(getDeletedTime()).append(", ");
				buf.append("deleted=").append(getDeleted()).append(", ");
				buf.append("]");
		return buf.toString();
	}
}