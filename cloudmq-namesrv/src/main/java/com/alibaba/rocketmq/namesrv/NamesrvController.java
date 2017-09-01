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
 * Name Server真正的启动控制器
 *
 * (1)rocketmq-namesrv扮演着nameNode角色，
 * (2)记录运行时消息相关的meta信息以及broker和filtersrv运行时信息
 * (3)支持部署集群
 * (4)NamesrvStartup.main() 启动序列图 http://lifestack.cn/wp-content/uploads/2015/04/namesrv_start.jpg
 * (5)参考资料：http://lifestack.cn/archives/360.html
 *
 * 当broker，producer，consumer都运行后，namesrv一共有8类线程：
 * 1.ServerHouseKeepingService：守护线程，本质是ChannelEventListener，监听broker的channel变化来更新本地的RouteInfo。
 * 2.NSScheduledThread1：定时任务线程，定时跑2个任务，
 *      第一个是任务，每隔10秒扫描出不活动的broker，然后从routeInfo中删除，
 *      第二个是任务，每隔10分钟定时打印configTable的信息。
 * 3.NettyBossSelector_1:Netty的boss线程（Accept线程），这里只有一根线程。
 * 4.NettyEventExecuter:一个单独的线程，监听NettyChannel状态变化来通知ChannelEventListener做响应的动作。
 * 5.DestroyJavaVM:java虚拟机析构钩子，一般是当虚拟机关闭时用来清理或者释放资源。
 * 6.NettyServerSelector_x_x:Netty的Work线程（IO线程），这里可能有多根线程。
 * 7.NettyServerWorkerThread_x:执行ChannelHandler方法的线程，ChannelHandler运行在该线程上，这里可能有多根线程。
 * 8.RemotingExecutorThread_x:服务端逻辑线程，这里可能有多根线程。
 *
 *
 *
 *
 * Name Server是RocketMQ的寻址服务，用于把Broker的路由信息做聚合；客户端依靠NameServer决定去获取对应topic的路由信息，从而决定对哪些Broker做连接
 * （1）Name Server是一个几乎无状态的结点，Name Server之间采取share-nothing的设计，互不通信
 * （2）对于一个Name Server集群列表，客户端连接Name Server的时候，只会选择随机连接一个结点，以做到负载均衡
 * （3）Name Server所有状态都从Broker上报而来，本身不存储任何状态，所有数据均在内存
 * （4）如果中途所有Name Server全都挂了，影响到路由信息的更新，不会影响和Broker的通信
 *
 *
 * Broker向所有的NameServer结点建立长连接，注册Topic信息
 * （1）主要是用于管理所有的broker信息，让producer和consumer都能获取到正确的broker信息，进行业务处理
 * （2）这是一个类似于zookeeper的服务治理中心
 *
 *
 * Namesrv主要功能
 * (1)Broker启动时候会向Namesrv发送注册请求，Namesrv接收broker的请求注册路由信息和保存活跃的broker列表，包括Master和Slave
 * (2)用来保存所有topic和该topic所有队列的列表
 * (3)NameServer用来保存所有broker的Filter列表
 * (4)接收Producer和Consumer的请求，根据某个topic获取到broker的路由信息
 *
 *
 * 为什么放弃zookeeper
 * (1)在RocketMQ中，topic在每个master的数据是对等的，没有哪个master有全部的topic数据，所以选举是没有意义的
 * (2)NameServer与NameServer直接完全没有信息同步，所以与其依赖重量级的zookeeper，不如开发轻量级的NameServer
 *
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-5
 */
public class NamesrvController {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.NamesrvLoggerName);
    /**
     * NameServer配置信息
     */
    private final NamesrvConfig namesrvConfig;
    /**
     * NettyServer通信层配置信息
     */
    private final NettyServerConfig nettyServerConfig;
    /**
     * remotingServer服务启动接口
     * (1)用于服务端通信
     * (2)包含启动、关闭、注册默认请求处理器、注册RPC钩子等方法,几种数据传输方式invokeSync、invokeAsync、invokeOneway
     * (3)这里传入的是NettyRemotingServer
     */
    private RemotingServer remotingServer;
    /**
     * Broker事件监听器
     * (1)属于netty概念
     * (2)监听Chanel(Connect、Close、Exception、Idle) 等四个动作事件
     * (3)提供相应的处理方法
     * (4)Broker启动时候会向Namesrv发送注册请求，Namesrv接收broker的请求注册路由信息和保存活跃的broker列表，包括Master和Slave
     */
    private BrokerHousekeepingService brokerHousekeepingService;
    /**
     * 服务端网络请求处理线程池
     * (1)remotingServer的并发处理器，处理各种类型请求
     * (2)包含服务端逻辑线程，线程名称是 RemotingExecutorThread_xxx
     */
    private ExecutorService remotingExecutor;
    /**
     * 定时线程池，运行两个任务：(1)清理不生效broker (2)打印每个namesrv的配置表
     */
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("NSScheduledThread"));
    /**
     * 核心数据结构：namesrv配置管理器 容器为HashMap，其中包含的ReadWriteLock保证读写安全
     */
    private final KVConfigManager kvConfigManager;
    /**
         * 核心数据结构：所有运行数据管理器，topicQueueTable、brokerAddrTable等，信息量很多，其中包含的ReadWriteLock保证读写安全
     */
    private final RouteInfoManager routeInfoManager;

    /**
     * NamesrvStartup.main()主入口调用,传入namesrvConfig、nettyServerConfig配置项
     * @param namesrvConfig
     * @param nettyServerConfig
     */
    public NamesrvController(NamesrvConfig namesrvConfig, NettyServerConfig nettyServerConfig) {
        this.namesrvConfig = namesrvConfig;
        this.nettyServerConfig = nettyServerConfig;
        this.kvConfigManager = new KVConfigManager(this);
        this.routeInfoManager = new RouteInfoManager();
        this.brokerHousekeepingService = new BrokerHousekeepingService(this);
    }

    /**
     * 初始化，在NamesrvStartup.main0()被调用
     * (1)加载kvConfig.json至KVConfigManager的configTable，即持久化转移到内存
     * (2)将namesrv作为一个netty server启动，即初始化通信层
     * (3)启动服务端请求的handle处理线程池，名称为“RemotingExecutorThread_xxx”的子线程用于服务端处理请求的子线程
     * (4)注册默认DefaultRequestProcessor和remotingExecutor，只要start启动，就开始处理netty请求
     * (5)启动(延迟5秒执行)第一个定时任务：每隔10秒扫描出(2分钟扫描间隔)不活动的broker，然后从routeInfo中删除
     * (6)启动(延迟1分钟执行)第二个定时任务：每隔10分钟定时打印namesrv全局配置信息
     *
     * @return 以上6个步骤执行完毕，返回true
     */
    public boolean initialize() {
        // (1)加载kvConfig.json至KVConfigManager的configTable，即持久化转移到内存
        this.kvConfigManager.load();

        // (2)将namesrv作为一个netty server启动，即初始化通信层
        // NettyRemotingServer对象，BrokerHousekeepingService对象作为该Netty连接中Socket链接的监听器(ChannelEventListener)
        // 监听与Broker建立的Channel的状态(空闲、关闭、异常三个状态)，并调用BrokerHousekeepingService的相应onChannel****方法。
        // 其中Channel的空闲、关闭、异常状态均调用RouteInfoManager.onChannelDestory方法处理。
        // 清理RouteInfoManager类的几个成员变量数据
        this.remotingServer = new NettyRemotingServer(this.nettyServerConfig, this.brokerHousekeepingService);

        // (3)启动服务端请求的handle处理线程池，名称为“RemotingExecutorThread_xxx”的子线程用于服务端处理请求的子线程
        this.remotingExecutor = Executors.newFixedThreadPool(nettyServerConfig.getServerWorkerThreads(), new ThreadFactoryImpl("RemotingExecutorThread_"));

        // (4)注册默认DefaultRequestProcessor和remotingExecutor，只要start启动，就开始处理netty请求
        this.registerProcessor();

        // (5)启动(延迟5秒执行)第一个定时任务：每隔10秒扫描出(2分钟扫描间隔)不活动的broker，然后从routeInfo中删除
        //      每隔十秒钟遍历brokerLiveTable集合，
        //      查看每个broker的最后更新时间是否超过了两分钟，
        //      超过则关闭broker的Channel并清理RouteInfoManager类的topicQueueTable、brokerAddrTable、clusterAddrTable、filterServerTable成员变量
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                NamesrvController.this.routeInfoManager.scanNotActiveBroker();
            }
        }, 5, 10, TimeUnit.SECONDS);

        // (6)启动(延迟1分钟执行)第二个定时任务：每隔10分钟打印NameServer的配置参数,即KVConfigManager.configTable变量的内容
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

    /**
     * 注册默认DefaultRequestProcessor和remotingExecutor，只要start启动，即可处理netty请求
     * (1)启动NameServer的Netty服务端(NettyRemotingServer),监听Channel的请求信息
     * (2)当收到客户端的请求信息之后会初始化一个线程，并放入线程池中进行处理
     * (3)该线程调用DefaultRequestProcessor.processRequest()方法来处理请求
     */
    private void registerProcessor() {
        this.remotingServer.registerDefaultProcessor(new DefaultRequestProcessor(this), this.remotingExecutor);
    }

    /**
     * 将namesrv作为一个netty server启动的入口
     * @throws Exception
     */
    public void start() throws Exception {
        this.remotingServer.start();
    }

    /**
     * Name server关闭
     * (1)关闭remotingServer服务端通信层对象
     * (2)关闭remotingExecutor服务端网络请求处理线程池
     * (3)关闭scheduledExecutorService定时任务的线程池
     * @throws Exception
     */
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
