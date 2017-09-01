/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.rocketmq.namesrv.routeinfo;

import io.netty.channel.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.namesrv.NamesrvController;
import com.alibaba.rocketmq.remoting.ChannelEventListener;


/**
 * Broker活动检测服务,其中ChannelEventListener是RocketMQ封装Netty向外暴露的一个接口层
 *
 * NameSrv监测Broker的死亡：
 * 当Broker和NameSrv之间的长连接断掉之后，下面的ChannelEventListener里面的函数就会被回调，从而触发NameServer的路由信息更新
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-15
 */
public class BrokerHousekeepingService implements ChannelEventListener {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.NamesrvLoggerName);


    private final NamesrvController namesrvController;


    public BrokerHousekeepingService(NamesrvController namesrvController) {
        this.namesrvController = namesrvController;
    }


    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {
    }


    /**
     * Channel被关闭,通知Topic路由管理器，清除无效Broker
     * 
     * @param remoteAddr
     * @param channel
     */
    @Override
    public void onChannelClose(String remoteAddr, Channel channel) {
        this.namesrvController.getRouteInfoManager().onChannelDestroy(remoteAddr, channel);
    }


    /**
     * Channel出现异常,通知Topic路由管理器，清除无效Broker
     * 
     * @param remoteAddr
     * @param channel
     */
    @Override
    public void onChannelException(String remoteAddr, Channel channel) {
        this.namesrvController.getRouteInfoManager().onChannelDestroy(remoteAddr, channel);
    }


    /**
     * Channe的Idle时间超时,通知Topic路由管理器，清除无效Broker
     * 
     * @param remoteAddr
     * @param channel
     */
    @Override
    public void onChannelIdle(String remoteAddr, Channel channel) {
        this.namesrvController.getRouteInfoManager().onChannelDestroy(remoteAddr, channel);
    }

}
