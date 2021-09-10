package ru.obvilion.launcher.utils;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class SystemStats {
    public static long getUsedMemory() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    public static long getFreeMemory() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
                .getFreePhysicalMemorySize();
    }

    public static long getTotalMemory() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
                .getTotalPhysicalMemorySize();
    }

    public static int getUsedMemoryMB() {
        return (int) (getUsedMemory() / 1024 / 1024);
    }

    public static int getFreeMemoryMB() {
        return (int) (getTotalMemory() / 1024 / 1024);
    }

    public static int getTotalMemoryMB() {
        return (int) (getTotalMemory() / 1024 / 1024);
    }

    public static int recommendedMin() {
        int mem = getTotalMemoryMB();

        if (mem >= 8 * 1024) {
            return 1024;
        }
        else if (mem >= 4 * 1024) {
            return 512;
        }
        else {
            return 256;
        }
    }

    public static int recommendedMax() {
        int mem = getTotalMemoryMB();

        if (mem >= 12 * 1024) {
            return 6 * 1024;
        }
        else if (mem >= 8 * 1024) {
            return 4 * 1024;
        }
        else if (mem >= 6 * 1024) {
            return (int) (2.5 * 1024);
        }
        else if (mem >= 4 * 1024) {
            return 2 * 1024;
        }
        else if (mem >= 3 * 1024) {
            return (int) (1.6 * 1024);
        }
        else {
            return 1024;
        }
    }
}
