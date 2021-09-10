package ru.obvilion.launcher.utils;

import java.lang.management.ManagementFactory;

public class SystemStats {
    public static long getUsedMemory() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    public static int getUsedMemoryMB() {
        return (int) (getUsedMemory() / 1024 / 1024);
    }
}
