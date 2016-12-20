package com.alibaba.rocketmq.util.base;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;


/**
 * @author: tianyuliang
 * @since: 2016/12/3
 */
public class HttpRequestUtil {
    private final static String CHARSET = "utf-8";
    private final static Integer CONNECTIMREOUT = 5000;
    private final static Integer SOCKETTIMROUT = 5000;


    private static String prepareParam(Map<?, ?> paramMap) {
        StringBuffer sb = new StringBuffer();
        if (paramMap.isEmpty()) {
            return "";
        }
        else {
            for (Object key : paramMap.keySet()) {
                String value = (String) paramMap.get(key);
                if (sb.length() < 1) {
                    sb.append(key).append("=").append(value);
                }
                else {
                    sb.append("&").append(key).append("=").append(value);
                }
            }
            return sb.toString();
        }
    }


    /**
     * Do GET request
     *
     * @param url
     * @return
     * @throws Exception
     * @throws IOException
     */
    public static String doGet(String url) throws Exception {
        URL localURL = new URL(url);
        URLConnection connection = openConnection(localURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setRequestProperty("Accept-Charset", CHARSET);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;
        if (httpURLConnection.getResponseCode() >= 300) {
            throw new Exception(
                "HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
        }
        try {
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, CHARSET);
            reader = new BufferedReader(inputStreamReader);
            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
        }
        finally {
            close(null, null, reader, inputStreamReader, inputStream);
        }
        return resultBuffer.toString();
    }


    /**
     * Do PUT request
     *
     * @param url
     * @param param
     * @return
     * @throws Exception
     */
    public static String doPut(String url, String param) throws Exception {
        System.out.println("PUT parameter : " + param);
        URL localURL = new URL(url);
        URLConnection connection = openConnection(localURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("PUT");
        httpURLConnection.setRequestProperty("Accept-Charset", CHARSET);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Content-Length", String.valueOf(param.length()));

        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuilder resultBuffer = new StringBuilder();
        String tempLine;
        try {
            outputStream = httpURLConnection.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(param);
            outputStreamWriter.flush();
            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception(
                    "HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, CHARSET);
            reader = new BufferedReader(inputStreamReader);

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }

        }
        finally {
            close(outputStreamWriter, outputStream, reader, inputStreamReader, inputStream);
        }
        return resultBuffer.toString();
    }


    /**
     * Do POST request
     *
     * @param url
     * @param parameterMap
     * @return
     * @throws Exception
     */
    public static String doPost(String url, Map<?, ?> parameterMap) throws Exception {
        String paramStr = prepareParam(parameterMap);
        System.out.println("POST parameter : " + paramStr);
        URL localURL = new URL(url);
        URLConnection connection = openConnection(localURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Accept-Charset", CHARSET);
        httpURLConnection.setRequestProperty("Content-Length", String.valueOf(paramStr.length()));

        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuilder resultBuffer = new StringBuilder();
        String tempLine;
        try {
            outputStream = httpURLConnection.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(paramStr);
            outputStreamWriter.flush();
            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception(
                    "HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, CHARSET);
            reader = new BufferedReader(inputStreamReader);

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }

        }
        finally {
            close(outputStreamWriter, outputStream, reader, inputStreamReader, inputStream);
        }
        return resultBuffer.toString();
    }


    public static void close(OutputStreamWriter outputStreamWriter, OutputStream outputStream,
            BufferedReader reader, InputStreamReader inputStreamReader, InputStream inputStream)
            throws Exception {
        if (outputStreamWriter != null) {
            outputStreamWriter.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        if (reader != null) {
            reader.close();
        }
        if (inputStreamReader != null) {
            inputStreamReader.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }

    }


    /**
     * request body post 方式请求
     *
     * @param url
     * @param param
     * @param headerMap
     * @return
     * @throws Exception
     */
    public static String doPost(String url, String param, Map<String, String> headerMap) throws Exception {
        URL localURL = new URL(url);
        URLConnection connection = openConnection(localURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Accept-Encoding", "gzip");
        httpURLConnection.setRequestProperty("Accept-Charset", CHARSET);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("ContentType", CHARSET);
        httpURLConnection.setRequestProperty("Content-Length", String.valueOf(param.length()));
        if (null != headerMap && !headerMap.isEmpty()) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();
                httpURLConnection.setRequestProperty(key, val);
            }
        }
        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuilder resultBuffer = new StringBuilder();
        String tempLine;
        try {
            outputStream = httpURLConnection.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream, CHARSET);
            outputStreamWriter.write(param);
            outputStreamWriter.flush();
            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception(
                    "HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, CHARSET);
            reader = new BufferedReader(inputStreamReader);
            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
        }
        finally {
            close(outputStreamWriter, outputStream, reader, inputStreamReader, inputStream);
        }
        return resultBuffer.toString();
    }


    /**
     * Do DELETE request
     *
     * @param url
     * @param parameterMap
     * @return
     * @throws Exception
     */
    public static String doDelete(String url, Map<?, ?> parameterMap) throws Exception {
        String paramStr = prepareParam(parameterMap);
        System.out.println("DELETE parameter : " + paramStr);
        URL localURL = new URL(url + "?" + paramStr);
        URLConnection connection = openConnection(localURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("DELETE");
        httpURLConnection.setRequestProperty("Accept-Charset", CHARSET);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Content-Length", String.valueOf(paramStr.length()));

        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuilder resultBuffer = new StringBuilder();
        String tempLine;
        try {
            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception(
                    "HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, CHARSET);
            reader = new BufferedReader(inputStreamReader);

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }

        }
        finally {
            close(null, null, reader, inputStreamReader, inputStream);
        }
        return resultBuffer.toString();
    }


    /**
     * Do DELETE request
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static String doDelete(String url) throws Exception {
        URL localURL = new URL(url);
        URLConnection connection = openConnection(localURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("DELETE");
        httpURLConnection.setRequestProperty("Accept-Charset", CHARSET);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");

        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuilder resultBuffer = new StringBuilder();
        String tempLine;
        try {
            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception(
                    "HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, CHARSET);
            reader = new BufferedReader(inputStreamReader);

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }

        }
        finally {
            close(null, null, reader, inputStreamReader, inputStream);
        }
        return resultBuffer.toString();
    }


    private static URLConnection openConnection(URL localURL) throws IOException {
        URLConnection connection;
        connection = localURL.openConnection();
        connection.setConnectTimeout(CONNECTIMREOUT);
        connection.setReadTimeout(SOCKETTIMROUT);
        return connection;
    }


    public static void main(String[] args) throws Exception {
        System.out.println(HttpMethod.DELETE.name());
        Map<String, String> body = new HashMap<String, String>();
        body.put("userId", "123456");
        body.put("userName", "yanqing-kg");
        Map<String, String> header = new HashMap<String, String>();
        header.put("from", "sso");
        System.out.println(HttpRequestUtil
            .doDelete("http://10.128.31.72:8000/relay/user/deleteuser" + "/123456" + "/yanqing-kg/"));
        // System.out.println(HttpRequestUtil.doDelete("http://127.0.0.1:8080/app/test",
        // body));
        // System.out.println(HttpRequestUtil.doDelete("http://127.0.0.1:8080/app/test/111/yintong/"));
        // System.out.println(HttpRequestUtil.doPut("http://127.0.0.1:8080/app/test",
        // JSON.toJSONString(body)));
        // System.out.println(HttpRequestUtil.doPost("http://10.128.31.72:8000/relay/apiapi/?method=update_out_user_passwd",
        // body));
    }
}
