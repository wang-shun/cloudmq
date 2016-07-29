package com.alibaba.rocketmq.domain.system;

/**
 * @author tianyuliang
 * @since 2016/7/22
 */
public class MemoryInfo {


    private long total;
    private long used;
    private long free;

    public long getTotal() {
        return total;
    }


    public void setTotal(long total) {
        this.total = total;
    }


    public long getUsed() {
        return used;
    }


    public void setUsed(long used) {
        this.used = used;
    }


    public long getFree() {
        return free;
    }


    public void setFree(long free) {
        this.free = free;
    }

}
