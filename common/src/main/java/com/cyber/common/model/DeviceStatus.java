package com.cyber.common.model;

import java.util.Map;

/**
 * Periodic status update from a device.
 */
public class DeviceStatus {
    private String deviceId;
    private long timestamp;
    
    // Core Vitals
    private double batteryLevel; // 0.0 - 1.0
    private boolean isCharging;
    
    // System Vitals
    private double cpuLoad;
    private double memoryUsage;
    
    // Context
    private String activeApp; // Currently focused window/app
    private SensorData sensorData;

    // Extensible properties
    private Map<String, String> extras;

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public double getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(double batteryLevel) { this.batteryLevel = batteryLevel; }
    
    public boolean isCharging() { return isCharging; }
    public void setCharging(boolean charging) { isCharging = charging; }
    
    public double getCpuLoad() { return cpuLoad; }
    public void setCpuLoad(double cpuLoad) { this.cpuLoad = cpuLoad; }
    
    public double getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
    
    public String getActiveApp() { return activeApp; }
    public void setActiveApp(String activeApp) { this.activeApp = activeApp; }
    
    public SensorData getSensorData() { return sensorData; }
    public void setSensorData(SensorData sensorData) { this.sensorData = sensorData; }

    public Map<String, String> getExtras() { return extras; }
    public void setExtras(Map<String, String> extras) { this.extras = extras; }
}
