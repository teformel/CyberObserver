package com.cyber.server.service;

import com.cyber.common.model.DeviceStatus;
import org.springframework.stereotype.Service;

import com.cyber.common.model.DeviceStatus;
import com.cyber.common.model.SensorData;
import org.springframework.stereotype.Service;

@Service
public class InferenceService {

    /**
     * Analyzes raw sensor data to determine semantic posture.
     * @param status The incoming status update containing raw data.
     */
    public void inferPosture(DeviceStatus status) {
        if (status == null || status.getSensorData() == null) return;

        SensorData sensor = status.getSensorData();
        
        // Calculate Euler angles or use Up vector dot product
        // Simplified approach: Check alignment with Gravity (Z-axis)
        // Assuming quaternion represents rotation from Earth frame to Device frame
        
        float qX = sensor.getqX();
        float qY = sensor.getqY();
        float qZ = sensor.getqZ();
        float qW = sensor.getqW();

        // Calculate pitch (x-axis rotation) and roll (y-axis rotation)
        // This math depends heavily on the sensor coordinate system (Android usually Y is North/Up)
        
        // Let's use a simpler heuristic: The major axis of gravity
        // Transform the Up vector (0,0,1) by the quaternion
        // For Android Rotation Vector: 
        // Ref: https://developer.android.com/reference/android/hardware/SensorManager#getRotationMatrixFromVector(float[],%20float[])
        
        double gravityY = 2 * (qY * qZ - qW * qX);
        double gravityZ = 1 - 2 * (qX * qX + qY * qY);
        
        String inferred = "UNKNOWN";

        // If Z is close to 1 or -1, device is FLAT on table (Face up/down)
        if (Math.abs(gravityZ) > 0.85) {
            inferred = (gravityZ > 0) ? "FLAT_FACE_UP" : "FLAT_FACE_DOWN";
        } 
        else if (Math.abs(gravityY) > 0.85) {
             inferred = (gravityY > 0) ? "UPRIGHT_PORTRAIT" : "UPSIDE_DOWN";
        }
        else {
             inferred = "HANDHELD_TIILTED";
        }
        
        sensor.setPosture(inferred);

        // Human IK Inference
        // Heuristic:
        // FLAT_FACE_UP + Night Time (not implemented yet) -> SLEEPING
        // UPRIGHT_PORTRAIT -> SITTING/STANDING
        // HANDHELD -> WORKING/INTERACTING
        
        String activity = "UNKNOWN";
        if (inferred.contains("FLAT")) {
            activity = "SLEEPING"; // Simplified
        } else if (inferred.contains("UPRIGHT")) {
            activity = "SITTING";
        } else if (inferred.contains("HANDHELD")) {
            activity = "INTERACTING";
        }
        
        // Store in a way that Web can see. Re-using posture field relative to device
        // Ideally we add a new field "userActivity" to SensorData or DeviceStatus
        // For strict compatibility, let's append it or use extras if available.
        // Or just let Frontend interpret "FLAT" as Sleeping.
        // Let's stick to returning Device Posture, and Frontend maps Posture -> Avatar Animation.
        // Actually, let's inject it into status.extras if possible to be cleaner
        // But DeviceStatus.extras is a Map<String,String>.
        if (status.getExtras() == null) {
            status.setExtras(new java.util.HashMap<>());
        }
        status.getExtras().put("human_ik", activity);
    }
    
    // Detailed math implementation to be added in next step
    // keeping this file clean for structure first.
}
