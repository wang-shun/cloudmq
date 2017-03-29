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
 *
 *
 * @author GaoYanLei
 * @date 2016/7/5
 */
public class GClientLogger {
    private static Logger log;

    static {
        log = createLogger(LoggerName.CloudmqClientoggerName);
    }


    private static Logger createLogger(final String loggerName) {
        String logConfigFilePath =
                System.getProperty("rocketmq.client.log.configFile",
                        System.getenv("ROCKETMQ_CLIENT_LOG_CONFIGFILE"));
        Boolean isloadconfig =
                Boolean.parseBoolean(System.getProperty("rocketmq.client.log.loadconfig", "true"));

        final String log4j_resource_file =
                System.getProperty("rocketmq.client.log4j.resource.fileName", "log4j_cloudmq_client.xml");

        final String logback_resource_file =
                System
                        .getProperty("rocketmq.client.logback.resource.fileName", "logback_cloudmq_client.xml");

        if (isloadconfig) {
            try {
                ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
                Class classType = iLoggerFactory.getClass();
                if (classType.getName().equals("org.slf4j.impl.Log4jLoggerFactory")) {
                    Class<?> DOMConfigurator = null;
                    Object DOMConfiguratorObj = null;
                    DOMConfigurator = Class.forName("org.apache.log4j.xml.DOMConfigurator");
                    DOMConfiguratorObj = DOMConfigurator.newInstance();
                    if (null == logConfigFilePath) {
                        Method configure = DOMConfiguratorObj.getClass().getMethod("configure", URL.class);
                        URL url = GClientLogger.class.getClassLoader().getResource(log4j_resource_file);
                        configure.invoke(DOMConfiguratorObj, url);
                    } else {
                        Method configure = DOMConfiguratorObj.getClass().getMethod("configure", String.class);
                        configure.invoke(DOMConfiguratorObj, logConfigFilePath);
                    }

                } else if (classType.getName().equals("ch.qos.logback.classic.LoggerContext")) {
                    Class<?> joranConfigurator = null;
                    Class<?> context = Class.forName("ch.qos.logback.core.Context");
                    Object joranConfiguratoroObj = null;
                    joranConfigurator = Class.forName("ch.qos.logback.classic.joran.JoranConfigurator");
                    joranConfiguratoroObj = joranConfigurator.newInstance();
                    Method setContext = joranConfiguratoroObj.getClass().getMethod("setContext", context);
                    setContext.invoke(joranConfiguratoroObj, iLoggerFactory);
                    if (null == logConfigFilePath) {
                        URL url = GClientLogger.class.getClassLoader().getResource(logback_resource_file);
                        Method doConfigure =
                                joranConfiguratoroObj.getClass().getMethod("doConfigure", URL.class);
                        doConfigure.invoke(joranConfiguratoroObj, url);
                    } else {
                        Method doConfigure =
                                joranConfiguratoroObj.getClass().getMethod("doConfigure", String.class);
                        doConfigure.invoke(joranConfiguratoroObj, logConfigFilePath);
                    }

                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        return LoggerFactory.getLogger(LoggerName.CloudmqClientoggerName);
    }


    public static Logger getLog() {
        return log;
    }


    public static void setLog(Logger log) {
        GClientLogger.log = log;
    }
}
