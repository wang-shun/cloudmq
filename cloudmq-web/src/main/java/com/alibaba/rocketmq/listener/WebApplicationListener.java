package com.alibaba.rocketmq.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author: tianyuliang
 * @since: 2016/12/5
 */
public class WebApplicationListener implements ServletContextListener {

    private final static Logger logger = LoggerFactory.getLogger(WebApplicationListener.class);


    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // close ScheduledExecutorService in web container
        // AccessTokenScheduled.shutdownScheduledExecutor();
        // SystemTimer.shutdownScheduledExecutor();
        // super.contextDestroyed(event);
        logger.info("WebApplicationListener # context destroyed ... ");
    }


    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        logger.info("WebApplicationListener # context init ...");
    }
}
