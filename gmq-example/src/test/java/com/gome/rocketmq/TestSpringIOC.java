package com.gome.rocketmq;

import com.gome.rocketmq.common.MyUtils;
import org.junit.Test;

/**
 * @author tantexian
 * @since 2016/6/15
 */
public class TestSpringIOC {

    @Test
    public void test(){
        System.out.println("userDemoDao: " + MyUtils.getSpringBean("userDemoDao"));
    }
}
