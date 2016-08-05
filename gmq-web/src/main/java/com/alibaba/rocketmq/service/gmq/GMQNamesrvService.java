package com.alibaba.rocketmq.service.gmq;

import com.alibaba.rocketmq.config.ConfigureInitializer;
import com.alibaba.rocketmq.service.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: tianyuliang
 * @since: 2016/7/19
 */
@Service
public class GMQNamesrvService extends AbstractService {

    @Autowired
    private ConfigureInitializer configureInitializer;

    public String getGMQNamesrvAddr() {
        return configureInitializer.getNamesrvAddr();
    }

}
