/*
 * Copyright (c) 2015 wish.com All rights reserved.
 * 本软件源代码版权归----所有,未经许可不得任意复制与传播.
 */
package com.alibaba.rocketmq.domain.base;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * 领域模型基类(常规公共字段)<br/>
 * 一律使用引用类型
 * @author ttx
 * @since 2015-06-16
 */
public class BaseDomain extends BaseQuery {
	private static final long serialVersionUID = 1L;
	
	private String id;// 编号id可以保存uuid也可以保存id，get如果id为空，则取uuid
	
	private String name;
	
	private String uuid;// 唯一编号
	
	private String code;// 编码
	
	private String remark;// 备注
	
	private Date createDate;// 创建日期
	
	private String createUser;// 创建者
	
	private Date updateDate;// 最后修改日期
	
	private String updateUser;// 最后修改者
	
	private Integer isDel;// 是否删除
	
	
	@JSONField(serialize=false)
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	@JSONField(serialize=false)
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	@JSONField(serialize=false)
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@JSONField(serialize=false)
	public Date getCreateDate() {
		return createDate;
	}
	@JSONField(name="created")
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	@JSONField(serialize=false)
	public String getCreateUser() {
		return createUser;
	}
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	
	@JSONField(serialize=false)
	public Integer getIsDel() {
		return isDel;
	}
	
	public void setIsDel(Integer isDel) {
		this.isDel = isDel;
	}
	
	public Date getUpdateDate() {
		return updateDate;
	}
	
	@JSONField(name="updated")
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * TODO 由于底层返回json串混用id和uuid，因此如果getid为空，则获取uuid值
	 * @return
	 * @author ttx
	 * @since 2016年1月26日 下午1:59:17
	 */
	public String getId() {
		if(StringUtils.isEmpty(this.id)){
			return this.getUuid();
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
