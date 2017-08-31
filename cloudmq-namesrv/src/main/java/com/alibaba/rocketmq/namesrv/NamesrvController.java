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
package com.alibaba.rocketmq.namesrv;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.namesrv.NamesrvConfig;
import com.alibaba.rocketmq.namesrv.kvconfig.KVConfigManager;
import com.alibaba.rocketmq.namesrv.processor.DefaultRequestProcessor;
import com.alibaba.rocketmq.namesrv.routeinfo.BrokerHousekeepingService;
import com.alibaba.rocketmq.namesrv.routeinfo.RouteInfoManager;
import com.alibaba.rocketmq.remoting.RemotingServer;
import com.alibaba.rocketmq.remoting.netty.NettyRemotingServer;
import com.alibaba.rocketmq.remoting.netty.NettyServerConfig;


/**
 * Name Server服务控制
 * (1)rocketmq-namesrv扮演着nameNode角色，
 * (2)记录运行时消息相关的meta信息以及broker和filtersrv运行时信息
 * (3)支持部署集群
 * (4)NamesrvStartup.main() 启动序列图 http://lifestack.cn/wp-content/uploads/2015/04/namesrv_start.jpg
 * (5)参考资料：http://lifestack.cn/archives/360.html
 *
 * 当broker，producer，consumer都运行后，namesrv一共有8类线程：
 * 1.ServerHouseKeepingService：守护线程，本质是ChannelEventListener，监听broker的channel变化来更新本地的RouteInfo。
 * 2.NSScheduledThread1：定时任务线程，定时跑2个任务，
 *      第一个是，每隔10分钟扫描出不活动的broker，然后从routeInfo中删除，
 *      第二个是，每个10分钟定时打印configTable的信息。
 * 3.NettyBossSelector_1:Netty的boss线程（Accept线程），这里只有一根线程。
 * 4.NettyEventExecuter:一个单独的线程，监听NettyChannel状态变化来通知ChannelEventListener做响应的动作。
 * 5.DestroyJavaVM:java虚拟机析构钩子，一般是当虚拟机关闭时用来清理或者释放资源。
 * 6.NettyServerSelector_x_x:Netty的Work线程（IO线程），这里可能有多根线程。
 * 7.NettyServerWorkerThread_x:执行ChannelHandler方法的线程，ChannelHandler运行在该线程上，这里可能有多根线程。
 * 8.RemotingExecutorThread_x:服务端逻辑线程，这里可能有多根线程。
 *
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-5
 */
public class NamesrvController {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.NamesrvLoggerName);
    // Name Server配置
    private final NamesrvConfig namesrvConfig;
    // 通信层配置
    private final NettyServerConfig nettyServerConfig;
    // 服务端通信层对象
    private RemotingServer remotingServer;
    // 接收Broker连接事件
    private BrokerHousekeepingService brokerHousekeepingService;
    // 服务端网络请求处理线程池
    private ExecutorService remotingExecutor;

    // 定时线程，定时跑2个任务
    private final ScheduledExecutorService scheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor(new ThreadFactoryImpl("NSScheduledThread"));

    /**
     * 核心数据结构
     */
    private final KVConfigManager kvConfigManager;
    private final RouteInfoManager routeInfoManager;


    public NamesrvController(NamesrvConfig namesrvConfig, NettyServerConfig nettyServerConfig) {
        this.namesrvConfig = namesrvConfig;
        this.nettyServerConfig = nettyServerConfig;
        this.kvConfigManager = new KVConfigManager(this);
        this.routeInfoManager = new RouteInfoManager();
        this.brokerHousekeepingService = new BrokerHousekeepingService(this);
    }


    public boolean initialize() {
        // 加载KV配置
        this.kvConfigManager.load();

        // 初始化通信层
        this.remotingServer = new NettyRemotingServer(this.nettyServerConfig, this.brokerHousekeepingService);

        // 初始化线程池(RemotingExecutorThread 服务端逻辑线程)
        this.remotingExecutor =
                Executors.newFixedThreadPool(nettyServerConfig.getServerWorkerThreads(),
                        new ThreadFactoryImpl("RemotingExecutorThread_"));

        this.registerProcessor();

        // 增加定时任务，第一个任务：每隔10分钟扫描出不活动的broker，然后从routeInfo中删除
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                NamesrvController.this.routeInfoManager.scanNotActiveBroker();
            }
        }, 5, 10, TimeUnit.SECONDS);

        // 增加定时任务，第二个任务：每个10分钟定时打印configTable的信息
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                NamesrvController.this.kvConfigManager.printAllPeriodically();
            }
        }, 1, 10, TimeUnit.MINUTES);

        // this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
        //
        // @Override
        // public void run() {
        // NamesrvController.this.routeInfoManager.printAllPeriodically();
        // }
        // }, 1, 5, TimeUnit.MINUTES);

        return true;
    }


    private void registerProcessor() {
        this.remotingServer
            .registerDefaultProcessor(new DefaultRequestProcessor(this), this.remotingExecutor);
    }


    public void start() throws Exception {
        this.remotingServer.start();
    }


    public void shutdown() {
        this.remotingServer.shutdown();
        this.remotingExecutor.shutdown();
        this.scheduledExecutorService.shutdown();
    }


    public NamesrvConfig getNamesrvConfig() {
        return namesrvConfig;
    }


    public NettyServerConfig getNettyServerConfig() {
        return nettyServerConfig;
    }


    public KVConfigManager getKvConfigManager() {
        return kvConfigManager;
    }


    public RouteInfoManager getRouteInfoManager() {
        return routeInfoManager;
    }


    public RemotingServer getRemotingServer() {
        return remotingServer;
    }


    public void setRemotingServer(RemotingServer remotingServer) {
        this.remotingServer = remotingServer;
    }
}
