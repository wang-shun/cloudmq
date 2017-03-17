/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50528
Source Host           : localhost:3306
Source Database       : rocketmq

Target Server Type    : MYSQL
Target Server Version : 50528
File Encoding         : 65001

Date: 2016-07-13 10:47:48
*/

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `rocketmq` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `rocketmq` ;

-- ----------------------------
-- Table structure for user
-- ----------------------------
CREATE  TABLE IF NOT EXISTS `rocketmq`.`user` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `user_name` varchar(50) DEFAULT NULL COMMENT '登录用户名',
  `password` varchar(50) DEFAULT NULL COMMENT '登录密码',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '修改时间',
  `deleted_time` datetime DEFAULT NULL COMMENT '删除时间',
  `state` tinyint(4) DEFAULT NULL COMMENT '账户状态(0:正常,1:删除,2:禁用....)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='用户表';

CREATE  TABLE IF NOT EXISTS `rocketmq`.`broker` (
  `ID` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `BrokerName` varchar(40) NOT NULL COMMENT 'broker name',
  `brokerId` tinyint(4) NOT NULL COMMENT 'broker id',
  `ClusterName` varchar(40) NOT NULL COMMENT 'cluster Name',
  `BrokerAddr` varchar(60) NOT NULL DEFAULT '' COMMENT 'broker addr',
  `version` varchar(40) NOT NULL COMMENT '版本号',
  `inTPS` varchar(20) NOT NULL DEFAULT '0' COMMENT '发送消息TPS',
  `outTPS` varchar(20) NOT NULL DEFAULT '0' COMMENT '消费消息TPS',
  `inTotalYest` varchar(20) NOT NULL COMMENT '昨天发送消息总数',
  `inTotalToday` varchar(20) NOT NULL COMMENT '今天发送消息总数',
  `outTotalYest` varchar(20) NOT NULL COMMENT '昨天消费消息总数',
  `outTotalTodtay` varchar(20) NOT NULL COMMENT '今天消费消息总数',
  `runtimeDate` bigint(20) NOT NULL COMMENT '服务器入库时间(yyyy-MM-dd HH:mm:ss.SSS)',
  `createDate` datetime NOT NULL COMMENT '本条数据创建时间',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='Broker明细表';

