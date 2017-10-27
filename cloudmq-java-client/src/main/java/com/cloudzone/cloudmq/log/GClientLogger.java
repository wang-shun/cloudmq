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
package com.cloudzone.cloudmq.log;

import java.lang.reflect.Method;
import java.net.URL;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.common.constant.LoggerName;


/**
 * @author GaoYanLei
 * @date 2016/7/5
 */
public class GClientLogger {

    private static Logger log;

    static {
        log = createLogger(LoggerName.CloudmqClientoggerName);
    }

    private static String clientLogConfigfile = "ROCKETMQ_CLIENT_LOG_CONFIGFILE";
    private static String log4jResourceKey = "rocketmq.client.log4j.resource.fileName";
    private static String log4jResourceValue = "log4j_cloudmq_client.xml";
    private static String logbackResourceKey = "rocketmq.client.logback.resource.fileName";
    private static String logbackResourceValue = "logback_cloudmq_client.xml";
    private static String clientLogKey = "rocketmq.client.log.configFile";
    private static String clientLoadConfig = "rocketmq.client.log.loadconfig";
    private static String log4jClazz = "org.slf4j.impl.Log4jLoggerFactory";
    private static String logbackContextClazz = "ch.qos.logback.classic.LoggerContext";
    private static String clientLogDefaultValue = System.getenv(clientLogConfigfile);
    private static String logbackCoreClazz = "ch.qos.logback.core.Context";
    private static String logbackJoranClazz = "ch.qos.logback.classic.joran.JoranConfigurator";
    private static String log4jxml = "org.apache.log4j.xml.DOMConfigurator";


    private static Logger createLogger(final String loggerName) {
        Boolean isLoadConfig = Boolean.parseBoolean(System.getProperty(clientLoadConfig, "true"));
        if (isLoadConfig) {
            try {
                ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
                Class classType = iLoggerFactory.getClass();
                String logbackResourceFile = System.getProperty(logbackResourceKey, logbackResourceValue);
                String log4jResourceFile = System.getProperty(log4jResourceKey, log4jResourceValue);

                if (classType.getName().equals(log4jClazz)) {
                    loadLog4j(log4jResourceFile);
                }
                else if (classType.getName().equals(logbackContextClazz)) {
                    loadLogback(iLoggerFactory, logbackResourceFile);
                }
                else {
                    String out = String.format("unknown classType.getName(): %s", classType.getName());
                    System.out.println(out);
                }
            }
            catch (Exception e) {
                System.err.println("createLogger err: %s" + e.getMessage());
            }
        }
        return LoggerFactory.getLogger(loggerName);
    }


    private static void loadLog4j(String log4jFile) throws Exception {
        Class<?> DOMConfigurator = null;
        Object DOMConfiguratorObj = null;
        DOMConfigurator = Class.forName(log4jxml);
        DOMConfiguratorObj = DOMConfigurator.newInstance();

        String logConfigFilePath = System.getProperty(clientLogKey, clientLogDefaultValue);
        if (null == logConfigFilePath) {
            Method configure = DOMConfiguratorObj.getClass().getMethod("configure", URL.class);
            URL url = GClientLogger.class.getClassLoader().getResource(log4jFile);
            configure.invoke(DOMConfiguratorObj, url);
        }
        else {
            Method configure = DOMConfiguratorObj.getClass().getMethod("configure", String.class);
            configure.invoke(DOMConfiguratorObj, logConfigFilePath);
        }
    }


    private static void loadLogback(ILoggerFactory iLoggerFactory, String logbackFile) throws Exception {
        Class<?> joranConfigurator = null;
        Object joranConfiguratoroObj = null;
        Class<?> context = Class.forName(logbackCoreClazz);
        joranConfigurator = Class.forName(logbackJoranClazz);
        joranConfiguratoroObj = joranConfigurator.newInstance();
        Method setContext = joranConfiguratoroObj.getClass().getMethod("setContext", context);
        setContext.invoke(joranConfiguratoroObj, iLoggerFactory);

        String logConfigFilePath = System.getProperty(clientLogKey, clientLogDefaultValue);
        if (null == logConfigFilePath) {
            URL url = GClientLogger.class.getClassLoader().getResource(logbackFile);
            Method doConfigure = joranConfiguratoroObj.getClass().getMethod("doConfigure", URL.class);
            doConfigure.invoke(joranConfiguratoroObj, url);
        }
        else {
            Method doConfigure = joranConfiguratoroObj.getClass().getMethod("doConfigure", String.class);
            doConfigure.invoke(joranConfiguratoroObj, logConfigFilePath);
        }
    }


    public static Logger getLog() {
        return log;
    }


    public static void setLog(Logger log) {
        GClientLogger.log = log;
    }
}
