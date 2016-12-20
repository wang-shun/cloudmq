package com.alibaba.rocketmq.util.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;


/**
 * @author: tianyuliang
 * @since: 2016/12/2
 */
public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * <ul>
     * <li>1、发送HTTP请求(json/xml数据)</li>
     * <li>2、可适当优化SocketTimeoutException异常，增加100ms后重试，即重新调用接口的机制</li>
     * </ul>
     *
     * @param methodType     枚举类型 Get/Post
     * @param requestUrl     请求Url
     * @param requestData    请求参数(json/xml字符串)
     * @param timeoutInMills 超时时间(ms)
     * @return 响应体的字符串
     * @throws SocketTimeoutException
     * @throws MalformedURLException
     * @throws IOException
     */
    public static String send(MethodType methodType, String requestUrl, String requestData, int timeoutInMills, String appKey)
            throws SocketTimeoutException, MalformedURLException, IOException, Exception {
        String ret = null;
        URL topUrl = null;
        HttpURLConnection http = null;
        long s = System.currentTimeMillis();
        try {
            topUrl = new URL(requestUrl);
            http = (HttpURLConnection) topUrl.openConnection();
            http.setDoInput(true);
            http.setDoOutput(true);
            http.setUseCaches(false);
            http.setRequestProperty("appKey", appKey);
            http.setRequestProperty("Content-type", "text/xml;charset=" + DEFAULT_ENCODING);
            http.setRequestMethod(methodType.getMethodName());
            if (timeoutInMills > 0) {
                /* 建立连接的超时时间 */
                http.setConnectTimeout(timeoutInMills);
                /* 等待服务器响应超时时间 */
                http.setReadTimeout(timeoutInMills);
            }
            http.connect();

            if (!Strings.isNullOrEmpty(requestData)) {
                // 写数据
                writeOutputStream(http.getOutputStream(), requestData);
                logger.info(String.format("third-request(http):%s\r\n%s", requestUrl, requestData));
            }

            if (http.getResponseCode() >= 300) {
                throw new Exception("HTTP Request is not success, response.code=" + http.getResponseCode());
            }
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 读数据
                ret = readInputStream(http.getInputStream());
                logger.info(String.format("%s ms elapsed, third-request(http):%s\r\n\t%s", String.valueOf(System.currentTimeMillis() - s), requestUrl, ret));
            }
        } catch (SocketTimeoutException e) {
            // 此处忽略异常日志，由外层捕获并自行处理
            throw e;
        } catch (MalformedURLException e) {
            logger.error("then http with malformedURLException error.", e);
            throw e;
        } catch (IOException e) {
            logger.error("then http with ioException error.", e);
            throw e;
        } catch (Exception e) {
            logger.error("then http with exception error.", e);
            throw e;
        } finally {
            try {
                http.disconnect();
                http = null;
            } catch (Exception e) {
                logger.error("close http connection error.", e);
            }
        }
        return ret;
    }


    /**
     * Write to OutputStream
     *
     * @throws IOException
     * @author tianyuliang
     * @params os
     * @params data
     * @since 2016/12/2
     */
    private static void writeOutputStream(OutputStream os, String data) throws IOException {
        try {
            os.write(data.getBytes(Charset.forName(DEFAULT_ENCODING)));
            os.flush();
        } catch (Exception e) {
            logger.error(String.format("write out stream error. message=%s", e.getMessage()), e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                logger.error(String.format("close out stream error. message=%s", e.getMessage()), e);
            }
        }
    }

    /**
     * Read from InputStream
     *
     * @param is
     * @return
     */
    public static String readInputStream(InputStream is) {
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            int r;
            byte[] b = new byte[1024];
            while ((r = is.read(b, 0, b.length)) != -1) {
                bos.write(b, 0, r);
            }
            return new String(bos.toByteArray(), DEFAULT_ENCODING);
        } catch (Exception e) {
            logger.error(String.format("read input stream error. message=%s", e.getMessage()), e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                logger.error(String.format("close input stream error. message=%s", e.getMessage()), e);
            }
        }
        return null;
    }


    public enum MethodType {

        GET("GET"),

        POST("POST");

        private String methodName;


        private MethodType(String methodName) {
            this.methodName = methodName;
        }


        public String getMethodName() {
            return methodName;
        }
    }

}