package com.cyber.common.model;

/**
 * Initial handshake packet sent by a device when connecting.
 */
public class DeviceIdentity {
    private String deviceId;
    private String name;
    private DeviceType type;
    private String authKey; // Simple security measure

    public DeviceIdentity() {}

    public DeviceIdentity(String deviceId, String name, DeviceType type, String authKey) {
        this.deviceId = deviceId;
        this.name = name;
        this.type = type;
        this.authKey = authKey;
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DeviceType getType() { return type; }
    public void setType(DeviceType type) { this.type = type; }

    public String getAuthKey() { return authKey; }
    public void setAuthKey(String authKey) { this.authKey = authKey; }
}
