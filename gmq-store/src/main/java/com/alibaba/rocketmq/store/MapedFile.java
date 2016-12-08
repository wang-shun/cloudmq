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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.constant.LoggerName;


/**
 * Pagecache文件访问封装
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-21
 */
public class MapedFile extends ReferenceResource {
    public static final int OS_PAGE_SIZE = 1024 * 4;
    private static final Logger log = LoggerFactory.getLogger(LoggerName.StoreLoggerName);
    // 当前JVM中映射的虚拟内存总大小
    private static final AtomicLong TotalMapedVitualMemory = new AtomicLong(0);
    // 当前JVM中mmap句柄数量
    private static final AtomicInteger TotalMapedFiles = new AtomicInteger(0);
    // 映射的文件名
    private final String fileName;
    // 映射的起始偏移量
    private final long fileFromOffset;
    // 映射的文件大小，定长
    private final int fileSize;
    // 映射的文件
    private final File file;
    // 映射的内存对象，position永远不变
    private final MappedByteBuffer mappedByteBuffer;
    // 当前写到什么位置
    private final AtomicInteger wrotePostion = new AtomicInteger(0);
    // Flush到什么位置
    private final AtomicInteger committedPosition = new AtomicInteger(0);
    // 映射的FileChannel对象
    private FileChannel fileChannel;
    // 最后一条消息存储时间
    private volatile long storeTimestamp = 0;
    private boolean firstCreateInQueue = false;


    public MapedFile(final String fileName, final int fileSize) throws IOException {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.file = new File(fileName);
        // 文件的名字即为映射的起始偏移量
        this.fileFromOffset = Long.parseLong(this.file.getName());
        boolean ok = false;

        ensureDirOK(this.file.getParent());

        try {
            this.fileChannel = new RandomAccessFile(this.file, "rw").getChannel();
            /**
             * this.fileChannel.map为FileChannelImpl的map方法
             * 继续调用Util.newMappedByteBuffer
             * 再调用directByteBufferConstructor.newInstance
             * 因此返回的是directByteBuffer。
             * PS: 在后续的clean方法中，使用反射invoke调用的也是directByteBuffer的cleaner方法
             *
             * @author tantexian<my.oschina.net/tantexian>
             * @since 2016/12/8
             * @params [fileName, fileSize]
             */
            this.mappedByteBuffer = this.fileChannel.map(MapMode.READ_WRITE, 0, fileSize);
            TotalMapedVitualMemory.addAndGet(fileSize);
            TotalMapedFiles.incrementAndGet();
            ok = true;
        }
        catch (FileNotFoundException e) {
            log.error("create file channel " + this.fileName + " Failed. ", e);
            throw e;
        }
        catch (IOException e) {
            log.error("map file " + this.fileName + " Failed. ", e);
            throw e;
        }
        finally {
            if (!ok && this.fileChannel != null) {
                this.fileChannel.close();
            }
        }
    }


    public static void ensureDirOK(final String dirName) {
        if (dirName != null) {
            File f = new File(dirName);
            if (!f.exists()) {
                boolean result = f.mkdirs();
                log.info(dirName + " mkdir " + (result ? "OK" : "Failed"));
            }
        }
    }


    public static void clean(final ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect() || buffer.capacity() == 0)
            return;
        // 使用反射invoke调用的是directByteBuffer的cleaner方法
        invoke(invoke(viewed(buffer), "cleaner"), "clean");
    }


    private static Object invoke(final Object target, final String methodName, final Class<?>... args) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    Method method = method(target, methodName, args);
                    method.setAccessible(true);
                    return method.invoke(target);
                }
                catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }


    private static Method method(Object target, String methodName, Class<?>[] args)
            throws NoSuchMethodException {
        try {
            return target.getClass().getMethod(methodName, args);
        }
        catch (NoSuchMethodException e) {
            return target.getClass().getDeclaredMethod(methodName, args);
        }
    }


    private static ByteBuffer viewed(ByteBuffer buffer) {
        String methodName = "viewedBuffer";

        // JDK7中将DirectByteBuffer类中的viewedBuffer方法换成了attachment方法
        Method[] methods = buffer.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("attachment")) {
                methodName = "attachment";
                break;
            }
        }

        ByteBuffer viewedBuffer = (ByteBuffer) invoke(buffer, methodName);
        if (viewedBuffer == null)
            return buffer;
        else
            return viewed(viewedBuffer);
    }


    public static int getTotalmapedfiles() {
        return TotalMapedFiles.get();
    }


    public static long getTotalMapedVitualMemory() {
        return TotalMapedVitualMemory.get();
    }


    public long getLastModifiedTimestamp() {
        return this.file.lastModified();
    }


    public String getFileName() {
        return fileName;
    }


    /**
     * 获取文件大小
     */
    public int getFileSize() {
        return fileSize;
    }


    public FileChannel getFileChannel() {
        return fileChannel;
    }


    /**
     * 向MapedBuffer追加消息<br>
     * 
     * @param msg
     *            要追加的消息
     * @param cb
     *            用来对消息进行序列化，尤其对于依赖MapedFile Offset的属性进行动态序列化
     * @return 是否成功，写入多少数据
     */
    public AppendMessageResult appendMessage(final Object msg, final AppendMessageCallback cb) {
        assert msg != null;
        assert cb != null;

        int currentPos = this.wrotePostion.get();

        // 表示有空余空间
        if (currentPos < this.fileSize) {
            ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
            byteBuffer.position(currentPos);
            AppendMessageResult result =
                    cb.doAppend(this.getFileFromOffset(), byteBuffer, this.fileSize - currentPos, msg);
            this.wrotePostion.addAndGet(result.getWroteBytes());
            this.storeTimestamp = result.getStoreTimestamp();
            return result;
        }

        // 上层应用应该保证不会走到这里
        log.error("MapedFile.appendMessage return null, wrotePostion: " + currentPos + " fileSize: "
                + this.fileSize);
        return new AppendMessageResult(AppendMessageStatus.UNKNOWN_ERROR);
    }


    /**
     * 文件起始偏移量
     */
    public long getFileFromOffset() {
        return this.fileFromOffset;
    }


    /**
     * 向存储层追加数据，一般在SLAVE存储结构中使用
     * 
     * @return 返回写入了多少数据
     */
    public boolean appendMessage(final byte[] data) {
        int currentPos = this.wrotePostion.get();

        // 表示有空余空间
        if ((currentPos + data.length) <= this.fileSize) {
            ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
            byteBuffer.position(currentPos);
            byteBuffer.put(data);
            this.wrotePostion.addAndGet(data.length);
            return true;
        }

        return false;
    }


    /**
     * 消息刷盘
     * 
     * @param flushLeastPages
     *            至少刷几个page
     * @return 当前flush到磁盘的位置
     */
    public int commit(final int flushLeastPages) {
        if (this.isAbleToFlush(flushLeastPages)) {
            // 判断当前是否需要立即刷缓冲数据到磁盘
            if (this.hold()) {
                // 获取当前写的位置
                int value = this.wrotePostion.get();
                // 将mappedByteBuffer的数据强制刷新到磁盘文件中
                this.mappedByteBuffer.force();
                // 刷新完毕，则将committedPosition即flush的位置更新为当前位置记录
                this.committedPosition.set(value);
                // 释放资源
                this.release();
            }
            else {
                log.warn("in commit, hold failed, commit offset = " + this.committedPosition.get());
                // 如果没有获取到资源，则flush的位置设置为当前缓冲区当期写入的位置
                this.committedPosition.set(this.wrotePostion.get());
            }
        }
        // 返回当前flush到磁盘的位置（offset是相对于当前的mappedFile）
        return this.getCommittedPosition();
    }


    public int getCommittedPosition() {
        return committedPosition.get();
    }


    public void setCommittedPosition(int pos) {
        this.committedPosition.set(pos);
    }

    /**
     * 根据最少需要刷盘page数值来判断当前是否需要立即刷新缓存数据到磁盘
     * @author tantexian<my.oschina.net/tantexian>
     * @since 2016/12/7
     * @params
     */
    private boolean isAbleToFlush(final int flushLeastPages) {
        // 获取当前flush到磁盘的位置
        int flush = this.committedPosition.get();
        // 获取当前write到缓冲区的位置
        int write = this.wrotePostion.get();

        // 如果当前文件已经写满，应该立刻刷盘（即当前写的位置==整个映射文件filesize的位置）
        if (this.isFull()) {
            return true;
        }

        // 只有未刷盘数据满足指定page数目才刷盘
        // OS_PAGE_SIZE默认为1024*4=4k
        if (flushLeastPages > 0) {
            // 计算出前期写缓冲区的位置到已刷盘的数据位置之间的数据，是否大于等于设置的至少得刷盘page个数
            // 超过则需要刷盘
            return ((write / OS_PAGE_SIZE) - (flush / OS_PAGE_SIZE)) >= flushLeastPages;
        }

        // 如果flushLeastPages为0，那么则是每次有数据写入缓冲区则则直接刷盘
        return write > flush;
    }


    public boolean isFull() {
        return this.fileSize == this.wrotePostion.get();
    }


    public SelectMapedBufferResult selectMapedBuffer(int pos, int size) {
        // 有消息
        if ((pos + size) <= this.wrotePostion.get()) {
            // 从MapedBuffer读
            if (this.hold()) {
                ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
                byteBuffer.position(pos);
                ByteBuffer byteBufferNew = byteBuffer.slice();
                byteBufferNew.limit(size);
                return new SelectMapedBufferResult(this.fileFromOffset + pos, byteBufferNew, size, this);
            }
            else {
                log.warn("matched, but hold failed, request pos: " + pos + ", fileFromOffset: "
                        + this.fileFromOffset);
            }
        }
        // 请求参数非法
        else {
            log.warn("selectMapedBuffer request pos invalid, request pos: " + pos + ", size: " + size
                    + ", fileFromOffset: " + this.fileFromOffset);
        }

        // 非法参数或者mmap资源已经被释放
        return null;
    }


    /**
     * 读逻辑分区
     */
    public SelectMapedBufferResult selectMapedBuffer(int pos) {
        if (pos < this.wrotePostion.get() && pos >= 0) {
            if (this.hold()) {
                ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
                byteBuffer.position(pos);
                int size = this.wrotePostion.get() - pos;
                ByteBuffer byteBufferNew = byteBuffer.slice();
                byteBufferNew.limit(size);
                return new SelectMapedBufferResult(this.fileFromOffset + pos, byteBufferNew, size, this);
            }
        }

        // 非法参数或者mmap资源已经被释放
        return null;
    }


    @Override
    public boolean cleanup(final long currentRef) {
        // 如果没有被shutdown，则不可以unmap文件，否则会crash
        if (this.isAvailable()) {
            log.error("this file[REF:" + currentRef + "] " + this.fileName
                    + " have not shutdown, stop unmaping.");
            return false;
        }

        // 如果已经cleanup，再次操作会引起crash
        if (this.isCleanupOver()) {
            log.error("this file[REF:" + currentRef + "] " + this.fileName
                    + " have cleanup, do not do it again.");
            // 必须返回true
            return true;
        }

        // 清理资源
        clean(this.mappedByteBuffer);
        TotalMapedVitualMemory.addAndGet(this.fileSize * (-1));
        TotalMapedFiles.decrementAndGet();
        log.info("unmap file[REF:" + currentRef + "] " + this.fileName + " OK");
        return true;
    }


    /**
     * 清理资源，destroy与调用shutdown的线程必须是同一个
     * 
     * @return 是否被destory成功，上层调用需要对失败情况处理，失败后尝试重试
     */
    public boolean destroy(final long intervalForcibly) {
        // 清理资源（调用this.cleanup函数）
        this.shutdown(intervalForcibly);

        if (this.isCleanupOver()) {
            try {
                this.fileChannel.close();
                log.info("close file channel " + this.fileName + " OK");

                long beginTime = System.currentTimeMillis();
                boolean result = this.file.delete();
                log.info("delete file[REF:" + this.getRefCount() + "] " + this.fileName
                        + (result ? " OK, " : " Failed, ") + "W:" + this.getWrotePostion() + " M:"
                        + this.getCommittedPosition() + ", "
                        + UtilAll.computeEclipseTimeMilliseconds(beginTime));
            }
            catch (Exception e) {
                log.warn("close file channel " + this.fileName + " Failed. ", e);
            }

            return true;
        }
        else {
            log.warn("destroy maped file[REF:" + this.getRefCount() + "] " + this.fileName
                    + " Failed. cleanupOver: " + this.cleanupOver);
        }

        return false;
    }


    public int getWrotePostion() {
        return wrotePostion.get();
    }


    public void setWrotePostion(int pos) {
        this.wrotePostion.set(pos);
    }


    public MappedByteBuffer getMappedByteBuffer() {
        return mappedByteBuffer;
    }


    /**
     * 方法不能在运行时调用，不安全。只在启动时，reload已有数据时调用
     */
    public ByteBuffer sliceByteBuffer() {
        return this.mappedByteBuffer.slice();
    }


    public long getStoreTimestamp() {
        return storeTimestamp;
    }


    public boolean isFirstCreateInQueue() {
        return firstCreateInQueue;
    }


    public void setFirstCreateInQueue(boolean firstCreateInQueue) {
        this.firstCreateInQueue = firstCreateInQueue;
    }
}
