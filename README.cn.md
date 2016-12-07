### GMQ是什么？
GMQ是基于阿里RocketMQ V3.2.6改进，二次封装的一款分布式、队列模型的消息中间件，具有以下特点：

* 支持严格的消息顺序
* 支持Topic与Queue两种模式
* 亿级消息堆积能力
* 比较友好的分布式特性

当前最新版本功能支持：
* 1、将整个项目命名为gmq-all-1.0.1
* 2、将项目中所有子工程命名为gmq-*，其中将rocketmq-client命名为gmq-api
* 3、集成为web运维监控界面，gmq-web
* 4、在gmq-api上面封装了一层客户端gmq-client，gmq-client提供给最终用户使用
* 5、目前gmq-client支持普通send，高性能sendOneWay、顺序Order发送，延时消息发送等功能
* 6、其中4中描述的所有功能在gmq-client中都能支持对应的spring及非spring的版本

----------

### 如何开始？`必读`
* [下载最新版安装包](http://git.oschina.net/tantexian/MyRocketMQ)
* [集群部署相关文档](http://my.oschina.net/tantexian/blog/703784)
* [`MyRocketMQ测试相关数据`](http://my.oschina.net/tantexian/blog?catalog=3613328&temp=1467698707818)


----------

### 开源协议
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) Copyright (C) 2016-2020 Gmq Group Holding Limited

----------

### 开发规范
* 代码使用Eclipse代码样式格式化，提交代码前须格式化[gmq.java.code.style.xml](http://git.oschina.net/gomecode/GMQ/tree/dev/docs/gmq.java.code.style.xml)
* Java源文件使用Unix换行、UTF-8文件编码
* 请在git clone命令之前执行`git config --global core.autocrlf false`，确保本地代码使用Unix换行格式
* 请在非主干分支上开发
* 每次提交及，对代码进行修改必须有相对应的解释说明

----------

### 开发IDE
* 开发工具不做统一规定（Idea、Eclipse都可以），建议使用Idea
* 建议使用最新版格式Idea，附下载地址：http://pan.baidu.com/s/1slMkXY1
* 附Idea属性格式注释文件下载地址：http://pan.baidu.com/s/1dEZqmbB

### 联系我们

----------


