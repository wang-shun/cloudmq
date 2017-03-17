package com.alibaba.rocketmq.util.restful.restTemplate;

import java.util.Iterator;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;


/**
 * @author gaoyanlei
 * @since 2016/7/25
 */
@Service
@RequiredArgsConstructor
public class TenantIdRestTemplate extends RestTemplate implements TenantIdRestOperations {

    @Override
    public <T> T getForObjectWithQuery(String url, Class<T> responseType, Map<String, Object> queryMap,
                                       Pageable pageable, Object... uriVariables) throws RestClientException {
        url = this.urlQueryAndPage(url, queryMap, pageable);
        return super.getForObject(url, responseType, uriVariables);
    }


    @Override
    public <T> T getForObjectWithQuery(String url, Class<T> responseType, Map<String, Object> queryMap,
                                       Pageable pageable, Map<String, ?> uriVariables) throws RestClientException {
        url = this.urlQueryAndPage(url, queryMap, pageable);
        return super.getForObject(url, responseType, uriVariables);
    }


    @Override
    public <T> ResponseEntity<T> getForEntityWithQuery(String url, Class<T> responseType,
                                                       Map<String, Object> queryMap, Pageable pageable, Object... uriVariables)
            throws RestClientException {
        url = this.urlQueryAndPage(url, queryMap, pageable);
        return super.getForEntity(url, responseType, queryMap, pageable, uriVariables);
    }


    @Override
    public <T> ResponseEntity<T> getForEntityWithQuery(String url, Class<T> responseType,
                                                       Map<String, Object> queryMap, Pageable pageable, Map<String, ?> uriVariables)
            throws RestClientException {
        url = this.urlQueryAndPage(url, queryMap, pageable);
        return super.getForEntity(url, responseType, uriVariables);
    }


    /**
     * 封装可变查询参数的URL
     *
     * @param url
     * @param queryMap
     * @param pageable
     * @return
     */
    private String urlQueryAndPage(String url, Map<String, Object> queryMap, Pageable pageable) {
        if (!url.contains("?"))
            url += "?";
        else
            url += "&";
        if (null != queryMap) {
            for (Map.Entry<String, Object> entry : queryMap.entrySet())
                if (null != entry.getKey() && null != entry.getValue()) {
                    url += entry.getKey() + "=" + entry.getValue() + "&";
                }
        }
        if (null != pageable) {
            url += "page=" + pageable.getPageNumber() + "&size=" + pageable.getPageSize();
            if (null != pageable.getSort() && null != pageable.getSort().iterator()) {
                Iterator<Sort.Order> orderIterator = pageable.getSort().iterator();
                while (orderIterator.hasNext()) {
                    Sort.Order order = orderIterator.next();
                    url += "&sort=" + order.getProperty() + "," + order.getDirection();
                }
            }
        }
        return url;
    }
}
