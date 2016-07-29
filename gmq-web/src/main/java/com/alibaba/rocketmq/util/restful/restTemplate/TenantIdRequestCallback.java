package com.alibaba.rocketmq.util.restful.restTemplate;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.RequestCallback;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * @author gaoyanlei
 * @since 2016/7/25
 */

@AllArgsConstructor
@NoArgsConstructor
public class TenantIdRequestCallback implements RequestCallback {

    private String tenantId;


    @Override
    public void doWithRequest(ClientHttpRequest request) throws IOException {
        if (null != TenantIdHolder.getTenantId()) {
            request.getHeaders().set(TenantConstants.HEADER_TENANT_ID, TenantIdHolder.getTenantId());
        }
        else if (!StringUtils.isEmpty(tenantId)) {
            request.getHeaders().set(TenantConstants.HEADER_TENANT_ID, tenantId);
        }
    }
}
