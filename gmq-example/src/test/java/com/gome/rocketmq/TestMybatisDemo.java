package com.gome.rocketmq;

import com.gome.rocketmq.common.MyUtils;
import com.gome.rocketmq.dao.UserDemoDao;
import com.gome.rocketmq.domain.UserDemo;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Random;

/**
 * @author tantexian
 * @since 2016/6/17
 */
public class TestMybatisDemo {
    private static UserDemoDao userDemoDao;

    @Before
    public void init(){
        userDemoDao = (UserDemoDao) MyUtils.getSpringBean("userDemoDao");
    }

    @Test
    public void testuserDemoSave2DB() {
        UserDemo userDemo = new UserDemo();
        userDemo.setCreatedTime(new Date());
        userDemo.setUserName("ttx" + new Random().nextInt(100));
        userDemo.setPassword("123456");
        save2db(userDemo);
    }

    private static void save2db(UserDemo userDemo) {

        userDemoDao.insertEntry(userDemo);
        System.out.println("[Result]----------: " + userDemoDao.selectEntryList(userDemo));
    }


}
