package com.gome.rocketmq.common;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MyUtils {

    private static String namesrvAddr = "10.128.31.103:9876;10.128.31.104:9876;10.128.31.105:9876;10.128.31.106:9876";

    public static ClassPathXmlApplicationContext applicationContext = null;

    /**
     * 获取getNamesrvAddr地址
     *
     * @author tantexian
     * @since 2016/6/15
     * @params
     */
    public static String getNamesrvAddr() {
        return namesrvAddr;
    }

    /**
     * 用于初始化spring及mybatis配置文件
     *
     * @author tantexian
     * @since 2016/6/15
     * @params
     */
    private static void initSpringAndMybatis() {
        if (applicationContext == null) {
            applicationContext = new ClassPathXmlApplicationContext(new String[]{"spring/spring-config.xml", "mybatis/spring-config-dao.xml"});
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                applicationContext.close();
            }
        });
    }

    /**
     * 获取Spring的Bean(最上层的Spring Bean获取需要使用此方法，嵌套对象可以使用@Resource注解获取)
     *
     * @author tantexian
     * @since 2016/6/15
     * @params
     */
    public static Object getSpringBean(String name) {
        initSpringAndMybatis();
        return applicationContext.getBean(name);

    }

    /**
     * 获取默认的集群名称
     *
     * @author tianyuliang
     * @since 2016/6/20
     */
    public static String getDefaultCluster(){
        return "DefaultCluster";
    }

}
