package com.cyber.common.model;

public class SensorData {
    // Rotation Vector (Quaternion) for 3D orientation
    private float qX;
    private float qY;
    private float qZ;
    private float qW;
    
    // Physical state inferred by device
    private String posture; // e.g., SLEEPING, WALKING, GAMING

    public SensorData() {}
    
    public SensorData(float qX, float qY, float qZ, float qW) {
        this.qX = qX;
        this.qY = qY;
        this.qZ = qZ;
        this.qW = qW;
    }

    public float getqX() { return qX; }
    public float getqY() { return qY; }
    public float getqZ() { return qZ; }
    public float getqW() { return qW; }
    public String getPosture() { return posture; }
    public void setPosture(String posture) { this.posture = posture; }
}
