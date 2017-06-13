package com.cloudzone.cloudmq;

import com.cloudzone.cloudmq.httpMQ.MQRestAPI;
import io.netty.channel.Channel;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 用于启动CloudMQ对外的httpMQ的restful API server
 *
 * @author tantexian, <my.oschina.net/tantexian>
 * @since 2017/6/12$
 */
public class StartUp {

    static final String ROOT_PATH = "cloudapi";

    private static final URI BASE_URI = URI.create("http://localhost:8989/");

    public static void main(String[] args) {
        try {
            System.out.println("httpMQ Server startup...");

            ResourceConfig resourceConfig = new ResourceConfig(MQRestAPI.class);
            final Channel server = NettyHttpContainerProvider.createHttp2Server(BASE_URI, resourceConfig, null);

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    server.close();
                }
            }));

            System.out.println(String.format("HttpMQ started. You can also use HTTP2(HTTP/2 enabled!)\n" +
                    "Try out %s%s\nStop the application using CTRL+C.", BASE_URI, ROOT_PATH));
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            Logger.getLogger(StartUp.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

