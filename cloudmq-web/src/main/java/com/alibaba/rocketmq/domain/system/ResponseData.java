package com.alibaba.rocketmq.domain.system;

import java.util.List;

/**
 * @author: tianyuliang
 * @since: 2016/7/27
 */
public class ResponseData {

    private List<CpuInfo> cpu;

    private OSInfo os;

    private JvmInfo jvm;

    private JavaInfo java;

    private MemoryInfo memory;

    public List<CpuInfo> getCpu() {
        return cpu;
    }

    public void setCpu(List<CpuInfo> cpu) {
        this.cpu = cpu;
    }

    public OSInfo getOs() {
        return os;
    }

    public void setOs(OSInfo os) {
        this.os = os;
    }

    public JvmInfo getJvm() {
        return jvm;
    }

    public void setJvm(JvmInfo jvm) {
        this.jvm = jvm;
    }

    public JavaInfo getJava() {
        return java;
    }

    public void setJava(JavaInfo java) {
        this.java = java;
    }

    public MemoryInfo getMemory() {
        return memory;
    }

    public void setMemory(MemoryInfo memory) {
        this.memory = memory;
    }
}
