package com.gome.rocketmq.example.ttx.ha;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.gome.rocketmq.common.MyUtils;
import com.gome.rocketmq.dao.MsgInfoDao;
import com.gome.rocketmq.dao.UserDemoDao;
import com.gome.rocketmq.domain.MsgInfo;
import com.gome.rocketmq.domain.UserDemo;

import java.util.Date;
import java.util.Random;


public class HAProducer {



    public static void main(String[] args) throws MQClientException {

        UserDemo userDemo = new UserDemo();
        userDemo.setCreatedTime(new Date());
        userDemo.setUserName("ttx" + new Random().nextInt(100));
        userDemo.setPassword("123456");
        save2db(userDemo);



        String msgBody = "I'm message body!!!";
        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setCreatedTime(new Date());
        msgInfo.setTopic("my_topic");
        msgInfo.setBody(msgBody);
       // msgInfo.setRepeatNum(1);
        //msgInfo.setRepeatNum(1);
        msgInfo.setRepeatNum(1);
        msgInfo.setBodyHashcode(msgBody.hashCode());
        MsgInfoDao msgInfoDao = (MsgInfoDao)MyUtils.getSpringBean("msgInfoDao");
        msgInfoDao.insertEntry(msgInfo);

    }

    private static void save2db(UserDemo userDemo){
        UserDemoDao userDemoDao = (UserDemoDao)MyUtils.getSpringBean("userDemoDao");
        userDemoDao.insertEntry(userDemo);
        System.out.println(userDemoDao.selectEntryList(userDemo));
    }
}
