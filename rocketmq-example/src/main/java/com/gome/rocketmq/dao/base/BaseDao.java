/*
 * Copyright (c) 2015 wish.com All rights reserved.
 * 本软件源代码版权归----所有,未经许可不得任意复制与传播.
 */
package com.gome.rocketmq.dao.base;

import java.io.Serializable;
import java.util.List;

/**
 * dao基类<实体,主键>
 * @author ttx
 * @since 2015-06-16
 * @param <T> 实体
 * @param <KEY> 主键
 */
public interface BaseDao<T,KEY extends Serializable> {

	/**
	 * 添加对象
	 * @param t
	 * @return 影响条数
	 */
	@SuppressWarnings("unchecked")
	int insertEntry(T... t);
	
	/**
	 * 添加对象并设置ID到对象上(需开启事务)
	 * @param t
	 * @return 影响条数
	 */
	int insertEntryCreateId(T t);
	
	/**
	 * 删除对象,主键
	 * @param key
	 * @return 影响条数
	 */
	@SuppressWarnings("unchecked")
	int deleteByKey(KEY... key);
	
	/**
	 * 删除对象,条件
	 * @param condtion
	 * @return 影响条数
	 */
	int deleteByKey(T condtion);
	
	/**
	 * 更新对象,条件主键ID
	 * @param t
	 * @return 影响条数
	 */
	int updateByKey(T t);

	/**
	 * 查询对象,条件主键
	 * @param key
	 * @return
	 */
	T selectEntry(KEY key);
	
	/**
	 * 查询对象,条件主键数组
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	List<T> selectEntryList(KEY... key);
	
	/**
	 * 查询对象,只要不为NULL与空则为条件
	 * @param t
	 * @return
	 */
	List<T> selectEntryList(T t);

	/**
	 * 查询对象总数
	 * @param t
	 * @return
	 */
	Integer selectEntryListCount(T t);
}
