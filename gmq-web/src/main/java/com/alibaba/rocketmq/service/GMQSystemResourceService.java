package com.alibaba.rocketmq.service;

import com.alibaba.rocketmq.domain.gmq.Broker;
import com.alibaba.rocketmq.domain.gmq.Cluster;
import com.alibaba.rocketmq.domain.system.MemoryInfo;
import com.alibaba.rocketmq.util.restful.domian.AbstractEntity;
import com.alibaba.rocketmq.util.restful.handle.ObjectHandle;
import com.alibaba.rocketmq.util.restful.restTemplate.TenantIdRestOperations;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.alibaba.rocketmq.util.restful.handle.ObjectHandle.getForObject;

/**
 * @author gaoyanlei
 * @since 2016/7/25
 */
@Service
public class GMQSystemResourceService extends AbstractService {

    private static final Logger logger = LoggerFactory.getLogger(GMQSystemResourceService.class);

    @Value("#{configProperties['sigar.cpu.url']}")
    private String cpuUrl;

    @Value("#{configProperties['sigar.all.url']}")
    private String allUrl;

    @Value("#{configProperties['sigar.memory.url']}")
    private String memoryUrl;

    @Value("#{configProperties['sigar.http.prefix']}")
    private String httpPrefix;

    @Value("#{configProperties['sigar.http.port']}")
    private String port;

    @Autowired
    GMQClusterService clusterService;

    @Autowired
    TenantIdRestOperations restOperations;


    public MemoryInfo memory(String brokerAddr) throws RuntimeException {
        String url = httpPrefix + brokerAddr.trim() + port + memoryUrl;
        AbstractEntity abstractEntity = restOperations.getForObject(url, AbstractEntity.class);
        MemoryInfo memory = getForObject(abstractEntity, MemoryInfo.class);
        return memory;
    }

    public List<String> getBrokerAddrs() throws Throwable {
        List<String> brokers = new ArrayList<>();
        List<Cluster> clusters = clusterService.list();
        if (clusters != null && clusters.size() > 0) {
            for (Cluster cluster : clusters) {
                if (CollectionUtils.isNotEmpty((cluster.getBrokerList())) && cluster.getBrokerList().size() > 0) {
                    for (Broker broker : cluster.getBrokerList()) {
                        brokers.add(broker.getAddr().substring(0, broker.getAddr().indexOf(":")));
                    }
                }
            }
        }
        Collections.sort(brokers);
        logger.info("broker ip list: {}", StringUtils.join(brokers, ","));
        return brokers;
    }

    public Map<String, Object> getAllStats(String brokerAddr) throws RuntimeException {
        String url = httpPrefix + brokerAddr + port + allUrl;
        logger.info("http allStats.url {}", url);
        AbstractEntity abstractEntity = restOperations.getForObject(url, AbstractEntity.class);
        Map<String, Object> params = ObjectHandle.getForObject(abstractEntity, new HashMap<String, Object>().getClass());
        return params;
    }


}
