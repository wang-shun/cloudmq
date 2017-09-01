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


    // Namesrv服务启动主入口
    public static void main(String[] args) {
        main0(args);
    }


    public static NamesrvController main0(String[] args) {
        // 1 设置MQ版本号、Socket发送缓冲区大小、Socket接收缓冲区大小
        // 1.1 设置MQ版本号
        System.setProperty(RemotingCommand.RemotingVersionKey, Integer.toString(MQVersion.CurrentVersion));
        // 1.2 Socket发送缓冲区大小
        if (null == System.getProperty(NettySystemConfig.SystemPropertySocketSndbufSize)) {
            NettySystemConfig.SocketSndbufSize = 2048;
        }
        // 1.3 Socket接收缓冲区大小
        if (null == System.getProperty(NettySystemConfig.SystemPropertySocketRcvbufSize)) {
            NettySystemConfig.SocketRcvbufSize = 1024;
        }

        try {
            // 2 检测Fastjson包冲突、解析命令行参数
            // 2.1 检测Fastjson包冲突
            PackageConflictDetect.detectFastjson();

            // 2.3 解析命令行参数
            Options opts = ServerUtil.buildCommandlineOptions(new Options());
            Options options = buildCommandlineOptions(opts);
            commandLine = ServerUtil.parseCmdLine("mqnamesrv", args, options, new PosixParser());
            if (null == commandLine) {
                System.exit(-1);
                return null;
            }

            // 3 初始化namesrv配置、初始化Netty配置、设置namesrv监听端口
            // 3.1 初始化配置文件
            final NamesrvConfig namesrvConfig = new NamesrvConfig();
            // 3.2 Netty相关参数
            final NettyServerConfig nettyServerConfig = new NettyServerConfig();
            // 3.3 设置namesrv监听端口
            nettyServerConfig.setListenPort(9876);
            // 3.4 加载namesrv配置文件
            if (commandLine.hasOption('c')) {
                String file = commandLine.getOptionValue('c');
                if (file != null) {
                    InputStream in = new BufferedInputStream(new FileInputStream(file));
                    properties = new Properties();
                    properties.load(in);
                    // 通过解析命令行参数,将-c后面配置配置文件，封装到namesrvConfig和nettyServerConfig
                    MixAll.properties2Object(properties, namesrvConfig);
                    MixAll.properties2Object(properties, nettyServerConfig);
                    System.out.println("load config properties file OK, " + file);
                    in.close();
                }
            }

            // 3.5 打印namesrv和netty默认配置
            if (commandLine.hasOption('p')) {
                MixAll.printObjectProperties(null, namesrvConfig);
                MixAll.printObjectProperties(null, nettyServerConfig);
                System.exit(0);
            }

            // 3.6 将commandLine对应的配置项写入namesrvConfig配置文件
            MixAll.properties2Object(ServerUtil.commandLine2Properties(commandLine), namesrvConfig);

            // 3.7 校验CLOUDMQ_HOME环境变量的值
            if (null == namesrvConfig.getRocketmqHome()) {
                String msg = "Please set the %s variable in your environment to match the location of the RocketMQ installation";
                System.out.println(String.format(msg, MixAll.CLOUDMQ_HOME_ENV));
                System.exit(-2);
            }

            // 4 初始化Logback
            // 4.1 设置namesrv日志LoggerContext属性
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            // 获取位于“/home/cloudmq-bin/conf”目录的“logback_namesrv.xml”日志文件
            configurator.doConfigure(namesrvConfig.getRocketmqHome() + "/conf/logback_namesrv.xml");
            final Logger log = LoggerFactory.getLogger(LoggerName.NamesrvLoggerName);

            // 4.2 基于Logback组件，打印服务器namesrv配置参数、nettyServer配置参数
            MixAll.printObjectProperties(log, namesrvConfig);
            MixAll.printObjectProperties(log, nettyServerConfig);

            // 5 构建NamesrvController这个真正的启动控制器
            final NamesrvController controller = new NamesrvController(namesrvConfig, nettyServerConfig);
            // 5.1 初始化Namesrv必要任务，获取初始化结果
            boolean initResult = controller.initialize();
            if (!initResult) {
                controller.shutdown();
                System.exit(-3);
            }

            // 6 设置JVM关闭钩子，当JVM关闭之前，执行controller.shutdown()来关闭Namesrv服务控制，然后再关闭JVM
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                private volatile boolean hasShutdown = false;
                private AtomicInteger shutdownTimes = new AtomicInteger(0);

                @Override
                public void run() {
                    synchronized (this) {
                        // 打印shutdown()被调用次数、以及执行shutdown()耗时
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

            // 7 启动NameServer服务、打印启动成功的Tips提示信息
            // 7.1 启动NameServer控制服务
            controller.start();

            // 7.2 打印启动成功提示信息
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
