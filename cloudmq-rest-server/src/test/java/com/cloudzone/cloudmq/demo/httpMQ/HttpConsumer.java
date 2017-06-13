package com.cloudzone.cloudmq.demo.httpMQ;

import com.cloudzone.cloudmq.common.PropertiesConst;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequest;
import org.junit.Test;

/** TODO: 生产者还未完成
 *
 * @author tantexian, <my.oschina.net/tantexian>
 * @since 2017/6/12
 */
public class HttpConsumer {
    @Test
    public void testSend() {
        String url = "http://127.0.0.1:8989/cloudapi/mq/v1";
        HttpRequest req = Unirest.get(url);
        req.header("Content-Type", "application/json");

        req.header(PropertiesConst.Keys.ProducerGroupId, "httpMQTestProducerGroup");
        req.header(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY, "jcpt-qa-simple-800:01b04fdc9aeb341dca32697c1840aeaa2");
    }
}
