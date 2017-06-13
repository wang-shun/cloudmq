package com.cloudzone.cloudmq.demo.dilatation;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yintongjiang on 2016/7/18.
 */
public class CloudTPS {

    private static boolean postLicense() {
        HttpResult result =
                null;
        try {
            result = httpPost("http://10.128.31.109:8800/httpMQ/license", null, "{\n" +
                    "    \"authKey\": \"0565ceda6619d4c59a3a2db7c3946e61c\",\n" +
                    "    \"appMetaName\": \"jcpt-client-to-cloudzone-800\",\n" +
                    "    \"appType\": 1\n" +
                    "}", "UTF-8", 3000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result != null && result.code == 200;
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                synchronized (this) {
                    System.out.println("---------------------------------");
                }
            }
        }, "ShutdownHook"));
        final int nThreads = args.length >= 1 ? Integer.parseInt(args[0]) : 10;
        final int num = args.length >= 2 ? Integer.parseInt(args[1]) : 100;
        final AtomicLong atomicSuccessNums = new AtomicLong(0L);
        final AtomicLong atomicSlaveNotNums = new AtomicLong(0L);
        final AtomicLong atomicMsgIds = new AtomicLong(0L);
        final AtomicLong atomicFail = new AtomicLong(0L);
        final AtomicLong flushDiskTimeOutCount = new AtomicLong(0L);
        final AtomicLong flushSlaveTimeOutCount = new AtomicLong(0L);
        final ExecutorService exec = Executors.newFixedThreadPool(nThreads);
        final long startCurrentTimeMillis = System.currentTimeMillis();
        final CyclicBarrier barrier = new CyclicBarrier(nThreads, new Runnable() {
            @Override
            public void run() {
                long endCurrentTimeMillis = System.currentTimeMillis();
                long escapedTimeMillis = endCurrentTimeMillis - startCurrentTimeMillis;
                System.out.printf(
                        "All message has send, send topicNums is : %d, " + "Success nums is : %d, " +
                                "Slave not available nums is : %d, " + "Flush disk time out nums is : %d, " +
                                "Flush slave time out nums is : %d, " + "\n" + "Send fail nums is : %d, "
                                + " msgCount is : %d, "
                                + "TPS : %d !!!",
                        nThreads * num, atomicSuccessNums.get(), atomicSlaveNotNums.get(), flushDiskTimeOutCount.get(),
                        flushSlaveTimeOutCount.get(), atomicFail.get(), atomicMsgIds.get(),
                        atomicSuccessNums.get() * 1000 / escapedTimeMillis);
                exec.shutdown();

            }
        });
        for (int i = 0; i < nThreads; i++) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int j = 0; j < num; j++) {
                            try {
                                if (postLicense()) {
                                    atomicSuccessNums.incrementAndGet();
                                } else {
                                    atomicFail.incrementAndGet();
                                }
                            } catch (Exception e) {
                                atomicFail.incrementAndGet();
                                throw e;
                            }
                        }
                        barrier.await();
                    } catch (Exception e) {
                        try {
                            barrier.await();
                            System.out.println((Thread.currentThread().getName() + "未完成"));
                        } catch (InterruptedException | BrokenBarrierException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
        }

    }

    static public HttpResult httpPost(String url, List<String> headers, String encodedContent, String encoding,
                                      long readTimeoutMs) throws IOException {

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout((int) readTimeoutMs);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            setHeaders(conn, headers, encoding);

            conn.getOutputStream().write(encodedContent.getBytes());

            int respCode = conn.getResponseCode(); // 这里内部发送请求
            String resp = null;

            if (HttpURLConnection.HTTP_OK == respCode) {
                resp = toString(conn.getInputStream(), encoding);
            } else {
                resp = toString(conn.getErrorStream(), encoding);
            }
            return new HttpResult(respCode, resp);
        } finally {
            if (null != conn) {
                conn.disconnect();
            }
        }

    }


    public static String toString(InputStream input, String encoding) throws IOException {
        return (null == encoding) ? toString(new InputStreamReader(input))
                : toString(new InputStreamReader(input, encoding));
    }

    private static String toString(Reader reader) throws IOException {
        CharArrayWriter sw = new CharArrayWriter();
        copy(reader, sw);
        return sw.toString();
    }

    static public long copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[1 << 12];
        long count = 0;
        for (int n = 0; (n = input.read(buffer)) >= 0; ) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    static private void setHeaders(HttpURLConnection conn, List<String> headers, String encoding) {
        if (null != headers) {
            for (Iterator<String> iter = headers.iterator(); iter.hasNext(); ) {
                conn.addRequestProperty(iter.next(), iter.next());
            }
        }
        conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + encoding);

        // 其它
        String ts = String.valueOf(System.currentTimeMillis());
        conn.addRequestProperty("Metaq-Client-RequestTS", ts);
    }


    static private String encodingParams(List<String> paramValues, String encoding)
            throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (null == paramValues) {
            return null;
        }

        for (Iterator<String> iter = paramValues.iterator(); iter.hasNext(); ) {
            sb.append(iter.next()).append("=");
            sb.append(URLEncoder.encode(iter.next(), encoding));
            if (iter.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    static public class HttpResult {
        final public int code;
        final public String content;


        public HttpResult(int code, String content) {
            this.code = code;
            this.content = content;
        }
    }
}
