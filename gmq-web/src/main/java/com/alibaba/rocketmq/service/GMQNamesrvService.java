package com.alibaba.rocketmq.service;

import org.springframework.stereotype.Service;

/**
 * @author: tianyuliang
 * @since: 2016/7/19
 */
@Service
public class GMQNamesrvService extends AbstractService {

    public String getGMQNamesrvAddr(){
        return configureInitializer.getNamesrvAddr();
    }

}
