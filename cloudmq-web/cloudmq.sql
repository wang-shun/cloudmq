/*
Navicat MariaDB Data Transfer

Source Server         : 10.128.31.125
Source Server Version : 50550
Source Host           : 10.128.31.125:3306
Source Database       : cloudmq

Target Server Type    : MariaDB
Target Server Version : 50550
File Encoding         : 65001

Date: 2017-06-05 16:58:04
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for broker
-- ----------------------------
DROP TABLE IF EXISTS `broker`;
CREATE TABLE `broker` (
  `ID` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `BrokerName` varchar(32) DEFAULT NULL COMMENT 'broker name',
  `brokerId` tinyint(4) DEFAULT NULL COMMENT 'broker id',
  `BrokerIp` varchar(50) NOT NULL DEFAULT '' COMMENT 'broker ip ',
  `brokerPort` varchar(8) DEFAULT NULL COMMENT 'broker port',
  `InTps` varchar(20) NOT NULL DEFAULT '0' COMMENT '发送消息TPS',
  `OutTps` varchar(20) NOT NULL DEFAULT '0' COMMENT '消费消息TPS',
  `InTotalToday` varchar(20) DEFAULT NULL COMMENT '今天发送消息总数',
  `OutTotalToday` varchar(20) DEFAULT NULL COMMENT '今天消费消息总数',
  `RuntimeDate` bigint(20) DEFAULT NULL COMMENT '消息入库的时间戳(web查询排序字段，精确到毫秒)',
  `CreateDate` varchar(40) DEFAULT NULL COMMENT '本条数据创建时间(yyyy-MM-dd HH:mm:ss.SSS)',
  `ClusterName` varchar(40) DEFAULT NULL COMMENT 'cluster Name',
  `OutTotalYest` varchar(20) DEFAULT NULL COMMENT '昨天消费消息总数',
  `InTotalYest` varchar(20) DEFAULT NULL COMMENT '昨天发送消息总数',
  `Version` varchar(40) NOT NULL COMMENT '版本号',
  PRIMARY KEY (`ID`),
  KEY `idx_broker_runtimeDate` (`RuntimeDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Broker明细表';

-- ----------------------------
-- Table structure for msg_info
-- ----------------------------
DROP TABLE IF EXISTS `msg_info`;
CREATE TABLE `msg_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `topic` varchar(100) DEFAULT '',
  `key` varchar(100) DEFAULT '',
  `body_hashcode` int(11) DEFAULT NULL,
  `body` varchar(150) DEFAULT '',
  `repeat_num` int(11) DEFAULT '0',
  `created_time` datetime DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `deleted_time` datetime DEFAULT NULL,
  `deleted` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `body_hashcode_index` (`body_hashcode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='msg信息表';

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `user_name` varchar(50) DEFAULT NULL COMMENT '登录用户名',
  `password` varchar(50) DEFAULT NULL COMMENT '登录密码',
  `mobile` varchar(20) DEFAULT NULL COMMENT '电话 ',
  `email` varchar(255) DEFAULT NULL COMMENT 'email',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '修改时间',
  `deleted_time` datetime DEFAULT NULL COMMENT '删除时间',
  `state` tinyint(4) DEFAULT NULL COMMENT '账户状态(0:正常,1:删除,2:禁用....)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户表DEMO';

-- ----------------------------
-- Table structure for user_demo
-- ----------------------------
DROP TABLE IF EXISTS `user_demo`;
CREATE TABLE `user_demo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(200) DEFAULT NULL,
  `password` varchar(200) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `deleted_time` datetime DEFAULT NULL,
  `deleted` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户表';
