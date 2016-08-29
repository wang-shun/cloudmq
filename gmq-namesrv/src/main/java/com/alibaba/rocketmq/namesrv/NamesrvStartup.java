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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import com.alibaba.rocketmq.common.MQVersion;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.conflict.PackageConflictDetect;
import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.namesrv.NamesrvConfig;
import com.alibaba.rocketmq.remoting.netty.NettyServerConfig;
import com.alibaba.rocketmq.remoting.netty.NettySystemConfig;
import com.alibaba.rocketmq.remoting.protocol.RemotingCommand;
import com.alibaba.rocketmq.srvutil.ServerUtil;


/**
 * Name server 启动入口
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-5
 */
public class NamesrvStartup {
    public static Properties properties = null;
    public static CommandLine commandLine = null;


    public static Options buildCommandlineOptions(final Options options) {
        Option opt = new Option("c", "configFile", true, "Name server config properties file");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("p", "printConfigItem", false, "Print all config item");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    // 此处为Namesrv启动的入口函数 2016/7/6 Add by tantexixan
    public static void main(String[] args) {
        main0(args);// 调用main0函数
    }


    public static NamesrvController main0(String[] args) {
        System.setProperty(RemotingCommand.RemotingVersionKey, Integer.toString(MQVersion.CurrentVersion));

        // Socket发送缓冲区大小
        if (null == System.getProperty(NettySystemConfig.SystemPropertySocketSndbufSize)) {
            NettySystemConfig.SocketSndbufSize = 2048;
        }

        // Socket接收缓冲区大小
        if (null == System.getProperty(NettySystemConfig.SystemPropertySocketRcvbufSize)) {
            NettySystemConfig.SocketRcvbufSize = 1024;
        }

        try {
            // 检测包冲突（fastjson包冲突检查）
            PackageConflictDetect.detectFastjson();

            // 解析命令行（/home/MyRocketMQ/target/gmq-1.0.0-gmq/gmq/bin/mqnamesrv命令执行相关）
            Options options = ServerUtil.buildCommandlineOptions(new Options());
            commandLine =
                    ServerUtil.parseCmdLine("mqnamesrv", args, buildCommandlineOptions(options),
                        new PosixParser());
            if (null == commandLine) {
                System.exit(-1);
                return null;
            }

            // 初始化配置文件
            final NamesrvConfig namesrvConfig = new NamesrvConfig();// 加载namesrv的相关配置项
            // 加载nettyServerConfig配置项( Netty是一个高性能、异步事件驱动的NIO框架，用来与其他模块通信交互，例如broker模块通信）
            // netty具体的启动工作为后续函数controller.start();
            final NettyServerConfig nettyServerConfig = new NettyServerConfig();
            // 设置netty监听端口为9876
            nettyServerConfig.setListenPort(9876);
            if (commandLine.hasOption('c')) {
                String file = commandLine.getOptionValue('c');
                if (file != null) {
                    InputStream in = new BufferedInputStream(new FileInputStream(file));
                    properties = new Properties();
                    properties.load(in);
                    MixAll.properties2Object(properties, namesrvConfig);
                    MixAll.properties2Object(properties, nettyServerConfig);
                    System.out.println("load config properties file OK, " + file);
                    in.close();
                }
            }

            // 打印默认配置
            if (commandLine.hasOption('p')) {
                MixAll.printObjectProperties(null, namesrvConfig);
                MixAll.printObjectProperties(null, nettyServerConfig);
                System.exit(0);
            }

            MixAll.properties2Object(ServerUtil.commandLine2Properties(commandLine), namesrvConfig);

            if (null == namesrvConfig.getRocketmqHome()) {
                System.out.println("Please set the " + MixAll.GMQ_HOME_ENV
                        + " variable in your environment to match the location of the RocketMQ installation");
                System.exit(-2);
            }

            // 初始化Logback（日志打印配置，类似于log4j）
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            configurator.doConfigure(namesrvConfig.getRocketmqHome() + "/conf/logback_namesrv.xml");
            final Logger log = LoggerFactory.getLogger(LoggerName.NamesrvLoggerName);

            // 打印服务器配置参数
            MixAll.printObjectProperties(log, namesrvConfig);
            MixAll.printObjectProperties(log, nettyServerConfig);

            // 初始化服务控制对象（NamesrvController为核心类，保存了大量的namesrv需要的信息）
            final NamesrvController controller = new NamesrvController(namesrvConfig, nettyServerConfig);
            // initialize核心函数，其中主要作用为：
            // 1、初始化netty相关配置
            // 2、定义broker与namesrv通过netty进行通信，的通信协议（即请求中带上code，来代表对应调用哪个方法函数）
            // 3、定时每10s扫描broker信息，如果过期则移除
            // 4、定时每10s将configTable的信息记录到日志文件中
            boolean initResult = controller.initialize();
            if (!initResult) {
                controller.shutdown();
                System.exit(-3);
            }

            // 设置一个jvm退出勾子函数，即jvm退出时，此处线程调用controller.shutdown()，清理controller相关资源
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                private volatile boolean hasShutdown = false;
                private AtomicInteger shutdownTimes = new AtomicInteger(0);


                @Override
                public void run() {
                    synchronized (this) {
                        log.info("shutdown hook was invoked, " + this.shutdownTimes.incrementAndGet());
                        if (!this.hasShutdown) {
                            this.hasShutdown = true;
                            long begineTime = System.currentTimeMillis();
                            controller.shutdown();
                            long consumingTimeTotal = System.currentTimeMillis() - begineTime;
                            log.info("shutdown hook over, consuming time total(ms): " + consumingTimeTotal);
                        }
                    }
                }
            }, "ShutdownHook"));

            // 启动服务（主要就是启动netty监听网络通信请求，即初始化netty启动异步通信server）
            controller.start();

            String tip = "The Name Server boot success.";
            log.info(tip);
            System.out.println(tip);

            return controller;
        }
        catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return null;
    }
}
