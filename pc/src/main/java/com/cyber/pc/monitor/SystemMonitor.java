package com.cyber.pc.monitor;

import com.cyber.common.model.DeviceStatus;
import com.cyber.common.model.SensorData;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OSProcess;

import java.util.List;

public class SystemMonitor {
    private final SystemInfo si;
    private final HardwareAbstractionLayer hal;
    private final OperatingSystem os;
    private long[] prevTicks;

    public SystemMonitor() {
        this.si = new SystemInfo();
        this.hal = si.getHardware();
        this.os = si.getOperatingSystem();
        this.prevTicks = hal.getProcessor().getSystemCpuLoadTicks();
    }

    public DeviceStatus captureStatus(String deviceId) {
        DeviceStatus status = new DeviceStatus();
        status.setDeviceId(deviceId);
        status.setTimestamp(System.currentTimeMillis());

        // CPU
        CentralProcessor processor = hal.getProcessor();
        status.setCpuLoad(processor.getSystemCpuLoadBetweenTicks(prevTicks));
        prevTicks = processor.getSystemCpuLoadTicks();

        // Memory
        GlobalMemory memory = hal.getMemory();
        double usedMem = (double)(memory.getTotal() - memory.getAvailable()) / memory.getTotal();
        status.setMemoryUsage(usedMem);

        // Power (if laptop)
        if (!hal.getPowerSources().isEmpty()) {
            status.setBatteryLevel(hal.getPowerSources().get(0).getRemainingCapacityPercent());
        } else {
            status.setBatteryLevel(1.0); // Desktop assumed 100%
        }

        // Active Window (Topmost)
        // Note: OSHI getDesktopWindows is heavy, used sparingly or assuming simple active window check
        // For efficiency in this loop, we might just get the foreground process if possible, 
        // or just return "Running"
        status.setActiveApp(getActiveWindowTitle());

        // Dummy Sensor Data for PC (could be mouse pos mapped to movement)
        status.setSensorData(new SensorData(0, 0, 0, 1)); 

        return status;
    }

    private String getActiveWindowTitle() {
        // Cross-platform 'Active Window' is tricky without JNA platform specific calls.
        // OSHI provides a listing, let's try to find the one with focus/z-order if possible,
        // but OSHI 6.x implies fetching all windows.
        // For simplicity in this demo, we'll return the OS Name + Uptime
        return os.getFamily() + " Uptime: " + os.getSystemUptime() + "s";
    }
}
