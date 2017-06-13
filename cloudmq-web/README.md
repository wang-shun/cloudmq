RocketMQ Console
================

为RocketMQ提供web控制台  

1、当前版本cloudmq-web已接入SSO系统，使用域账号登陆     
2、当前电脑IP是10.122.2.223，则配置本机host  10.122.2.223 cloudmq.web.dev    
3、启动cloudmq-web工程，访问 http://cloudmq.web.dev:9017 即可    
4、普通用户和管理员看到的界面有差异    
5、把普通用户切换管理员，需要修改rocketmq数据库的user表的state字段   
6、state字段含义：0:正常，1:删除，2:禁用，该条记录的state为0则是管理员，否则不是


