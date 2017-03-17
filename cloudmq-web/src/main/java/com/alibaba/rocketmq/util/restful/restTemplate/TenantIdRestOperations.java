package com.alibaba.rocketmq.util.restful.restTemplate;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;


/**
 * @author gaoyanlei
 * @since 2016/7/25
 */

public interface TenantIdRestOperations extends RestOperations {

    <T> T getForObjectWithQuery(String url, Class<T> responseType, Map<String, Object> queryMap,
            Pageable pageable, Object... uriVariables) throws RestClientException;


    <T> T getForObjectWithQuery(String url, Class<T> responseType, Map<String, Object> queryMap,
            Pageable pageable, Map<String, ?> uriVariables) throws RestClientException;


    <T> ResponseEntity<T> getForEntityWithQuery(String url, Class<T> responseType,
            Map<String, Object> queryMap, Pageable pageable, Object... uriVariables)
            throws RestClientException;


    <T> ResponseEntity<T> getForEntityWithQuery(String url, Class<T> responseType,
            Map<String, Object> queryMap, Pageable pageable, Map<String, ?> uriVariables)
            throws RestClientException;

}
