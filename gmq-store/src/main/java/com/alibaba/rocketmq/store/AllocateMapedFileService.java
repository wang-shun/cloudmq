/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.rocketmq.store;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.common.ServiceThread;
import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.constant.LoggerName;


/**
 * Create MapedFile in advance
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-21
 */
public class AllocateMapedFileService extends ServiceThread {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.StoreLoggerName);
    private static int WaitTimeOut = 1000 * 5;
    // requestTable的key-val分别保存文件的filePath及当前req请求
    private ConcurrentHashMap<String, AllocateRequest> requestTable =
            new ConcurrentHashMap<String, AllocateRequest>();
    // 请求优先级queue
    private PriorityBlockingQueue<AllocateRequest> requestQueue =
            new PriorityBlockingQueue<AllocateRequest>();
    private volatile boolean hasException = false;


    public MapedFile putRequestAndReturnMapedFile(String nextFilePath, String nextNextFilePath, int fileSize) {
        AllocateRequest nextReq = new AllocateRequest(nextFilePath, fileSize);
        AllocateRequest nextNextReq = new AllocateRequest(nextNextFilePath, fileSize);
        /**
         * 将当前创建nextFilePath mappedfile的请求放置到requestTable请求列表中，
         * putIfAbsent（原子操作，返回老的key-val的对应关系val值）：
         * 1、如果返回null，则说明之前没有key-val对应关系，put成功
         * 2、如果之前有key-val的对应关系，则返回老的key对应val值，且put更新val失败
         *
         * @author tantexian<my.oschina.net/tantexian>
         * @since 2016/12/12
         * @params [nextFilePath, nextNextFilePath, fileSize]
         */
        // 返回值为true则说明，创建请求放置到requestTable列表中成功
        boolean nextPutOK = (this.requestTable.putIfAbsent(nextFilePath, nextReq) == null);
        boolean nextNextPutOK = (this.requestTable.putIfAbsent(nextNextFilePath, nextNextReq) == null);

        if (nextPutOK) {
            // offer方法：如果超过queue的边界长度，则添加失败返回false，否则添加成功返回true
            boolean offerOK = this.requestQueue.offer(nextReq);
            if (!offerOK) {
                log.warn("add a request to preallocate queue failed");
            }
        }

        if (nextNextPutOK) {
            boolean offerOK = this.requestQueue.offer(nextNextReq);
            if (!offerOK) {
                log.warn("add a request to preallocate queue failed");
            }
        }

        if (hasException) {
            log.warn(this.getServiceName() + " service has exception. so return null");
            return null;
        }

        AllocateRequest result = this.requestTable.get(nextFilePath);
        try {
            if (result != null) {
                // 调用AllocateRequest的CountDownLatch方法（只有等到该对象资源没有被其他地方使用才返回true）
                boolean waitOK = result.getCountDownLatch().await(WaitTimeOut, TimeUnit.MILLISECONDS);
                if (!waitOK) {
                    log.warn("create mmap timeout " + result.getFilePath() + " " + result.getFileSize());
                }
                // AllocateRequest对应的mappedfile创建成功，则从requestTable移除 2016/12/12 Add by tantexixan
                // 如果上述waitOK为超时，也依然移除？下次会自动添加进来创建？
                this.requestTable.remove(nextFilePath);
                // 返回mappedfile文件
                return result.getMapedFile();
            }
            else {
                log.error("find preallocate mmap failed, this never happen");
            }
        }
        catch (InterruptedException e) {
            log.warn(this.getServiceName() + " service has exception. ", e);
        }

        return null;
    }


    @Override
    public String getServiceName() {
        return AllocateMapedFileService.class.getSimpleName();
    }


    public void shutdown() {
        this.stoped = true;
        this.thread.interrupt();

        try {
            this.thread.join(this.getJointime());
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (AllocateRequest req : this.requestTable.values()) {
            if (req.mapedFile != null) {
                log.info("delete pre allocated maped file, {}", req.mapedFile.getFileName());
                req.mapedFile.destroy(1000);
            }
        }
    }


    public void run() {
        log.info(this.getServiceName() + " service started");

        // 调用mmapOperation函数
        while (!this.isStoped() && this.mmapOperation())
            ;

        log.info(this.getServiceName() + " service end");
    }


    /**
     * Only interrupted by the external thread, will return false
     */
    private boolean mmapOperation() {
        AllocateRequest req = null;
        try {
            // 从分配MappedFile的请求优先级队列中，出队一个请求
            req = this.requestQueue.take();
            // 判断当前请求是否保存在requestTable中
            if (null == this.requestTable.get(req.getFilePath())) {
                log.warn("this mmap request expired, maybe cause timeout " + req.getFilePath() + " "
                        + req.getFileSize());
                return true;
            }

            // 如果当前mmap请求的mapedFile为空，则创建新的mapedFile文件
            if (req.getMapedFile() == null) {
                long beginTime = System.currentTimeMillis();
                // 根据当前请求的文件路径及文件大小创建对于的mmap内存映射
                MapedFile mapedFile = new MapedFile(req.getFilePath(), req.getFileSize());
                long eclipseTime = UtilAll.computeEclipseTimeMilliseconds(beginTime);
                if (eclipseTime > 10) {// 如果创建内存映射超过10ms则记录warn日志
                    int queueSize = this.requestQueue.size();
                    log.warn("create mapedFile spent time(ms) " + eclipseTime + " queue size " + queueSize
                            + " " + req.getFilePath() + " " + req.getFileSize());
                }
                // 设置req的mapedFile为当前新创建的mapedFile
                req.setMapedFile(mapedFile);
                this.hasException = false;
            }
        }
        catch (InterruptedException e) {
            log.warn(this.getServiceName() + " service has exception, maybe by shutdown");
            this.hasException = true;
            return false;
        }
        catch (IOException e) {
            log.warn(this.getServiceName() + " service has exception. ", e);
            this.hasException = true;
        }
        finally {
            // 如果此处从mmap请求队列中获取的req不为空，则通过countDown唤醒线程await（count值减到0位唤醒）
            if (req != null)
                req.getCountDownLatch().countDown();
        }
        return true;
    }

    class AllocateRequest implements Comparable<AllocateRequest> {
        // Full file path
        private String filePath;
        private int fileSize;
        private CountDownLatch countDownLatch = new CountDownLatch(1);
        private volatile MapedFile mapedFile = null;


        public AllocateRequest(String filePath, int fileSize) {
            this.filePath = filePath;
            this.fileSize = fileSize;
        }


        public String getFilePath() {
            return filePath;
        }


        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }


        public int getFileSize() {
            return fileSize;
        }


        public void setFileSize(int fileSize) {
            this.fileSize = fileSize;
        }


        public CountDownLatch getCountDownLatch() {
            return countDownLatch;
        }


        public void setCountDownLatch(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }


        public MapedFile getMapedFile() {
            return mapedFile;
        }


        public void setMapedFile(MapedFile mapedFile) {
            this.mapedFile = mapedFile;
        }


        public int compareTo(AllocateRequest other) {
            return this.fileSize < other.fileSize ? 1 : this.fileSize > other.fileSize ? -1 : 0;
        }
    }
}
