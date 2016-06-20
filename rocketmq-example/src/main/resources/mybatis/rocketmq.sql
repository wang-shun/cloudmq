/*
Navicat MySQL Data Transfer

Source Server         : 10.28.175.91
Source Server Version : 50161
Source Host           : 10.28.175.91:3306
Source Database       : gallery

Target Server Type    : MYSQL
Target Server Version : 50161
File Encoding         : 65001

Date: 2014-03-12 12:55:46 author tantexian
*/
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `rocketmq` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `rocketmq` ;

-- ------------------------------------------------
-- Table structure for `user_demo`， add by ttx 2015-6-1
-- ------------------------------------------------
CREATE  TABLE IF NOT EXISTS `rocketmq`.`user_demo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `deleted_time` datetime DEFAULT NULL,
  `deleted` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
COMMENT = '用户表DEMO';

-- ------------------------------------------------
-- Table structure for `msg_info`， add by ttx 2016-6-15
-- ------------------------------------------------
CREATE  TABLE IF NOT EXISTS `rocketmq`.`msg_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `topic` varchar(255) DEFAULT NULL,
  `key` varchar(255) DEFAULT NULL,
  `body_hashcode` int(11) DEFAULT NULL,
  `body` varchar(255) DEFAULT NULL,
  `repeat_num` int(11) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `deleted_time` datetime DEFAULT NULL,
  `deleted` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
COMMENT = 'msg信息表';
/*由于body_hashcode高频率查询，因此创建索引提高查询效率*/
CREATE INDEX body_hashcode_index ON msg_info(body_hashcode);

