package com.alibaba.rocketmq.service;

import com.alibaba.rocketmq.domain.Memory;
import com.alibaba.rocketmq.util.restful.domian.AbstractEntity;
import com.alibaba.rocketmq.util.restful.handle.ObjectHandle;
import com.alibaba.rocketmq.util.restful.restTemplate.TenantIdRestOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author gaoyanlei
 * @since 2016/7/25
 */

@Service
public class GMQSystemResourceService {

    @Value("#{configProperties['cpu.url']}")
    private String cpuUrl;
    @Value("#{configProperties['memory.url']}")
    private String memoryUrl;
    @Value("#{configProperties['all.url']}")
    private String allUrl;
    @Value("#{configProperties['http']}")
    private String http;

    @Autowired
    private TenantIdRestOperations restOperations;


    public Memory memory(String ipAndPort)
            throws InvocationTargetException, IllegalAccessException, InstantiationException {
        String url = http + ipAndPort + memoryUrl;
        AbstractEntity abstractEntity = restOperations.getForObject(url, AbstractEntity.class);
        Memory memory = ObjectHandle.getForObject(abstractEntity, Memory.class);
        return memory;
    }


    public Object all(String ipAndPort)
            throws InvocationTargetException, IllegalAccessException, InstantiationException {
        String url = http + ipAndPort + allUrl;
        AbstractEntity abstractEntity = restOperations.getForObject(url, AbstractEntity.class);
        return abstractEntity.getData();
    }
}
