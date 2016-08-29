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

    // 定时线程
    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("NSScheduledThread"));

    /**
     * 核心数据结构
     */
    private final KVConfigManager kvConfigManager;
    private final RouteInfoManager routeInfoManager;


    public NamesrvController(NamesrvConfig namesrvConfig, NettyServerConfig nettyServerConfig) {
        this.namesrvConfig = namesrvConfig; // namesrv相关配置
        this.nettyServerConfig = nettyServerConfig; // netty相关配置
        this.kvConfigManager = new KVConfigManager(this); // KV配置管理
        this.routeInfoManager = new RouteInfoManager(); // 路由信息、topic信息管理
        this.brokerHousekeepingService = new BrokerHousekeepingService(this); // broker管理服务
    }


    public boolean initialize() {
        // 加载KV配置
        this.kvConfigManager.load();

        // 初始化通信层
        this.remotingServer = new NettyRemotingServer(this.nettyServerConfig, this.brokerHousekeepingService);

        // 初始化线程池（根据getServerWorkerThreads值，启动相应数量线程）
        this.remotingExecutor = Executors.newFixedThreadPool(nettyServerConfig.getServerWorkerThreads(),
            new ThreadFactoryImpl("RemotingExecutorThread_"));

        // 此注册函数主要作用就是，定义RequestCode，用来作为netty的通信协议字段
        // 即：如果broker通过netty发送通信请求，其中请求信息中带有code == RequestCode.REGISTER_BROKER，
        //那么在namesrv的netty端接收到该通信连接时候，
        // 则对应调用namesrv的DefaultRequestProcessor类下面的registerBroker方法，从而完成broker向namesrv注册
        // 具体请参考com.alibaba.rocketmq.namesrv.processor.DefaultRequestProcessor类
        // 更多关于netty在gmq中的通信机制及原理，请关注后续博文(博客地址为：http://my.oschina.net/tantexian)
        this.registerProcessor();

        // 增加定时任务（延时5秒，每间隔10s钟，定时扫描一次）
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                // 定时扫描notActive的broker（若发现broker过期，则清除该broker与namesrv之间的socketChanel通道）
                NamesrvController.this.routeInfoManager.scanNotActiveBroker();
            }
        }, 5, 10, TimeUnit.SECONDS);

        // 增加定时任务（延时1秒，每间隔10s钟，定时扫描一次）
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                // 定时将configTable相关信息记录到日志文件中
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
        this.remotingServer.registerDefaultProcessor(new DefaultRequestProcessor(this),
            this.remotingExecutor);
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
