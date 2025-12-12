package com.cyber.mobile.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

// Placeholder imports for common logic
// import com.cyber.common.model.*; 
// import com.google.gson.Gson;

public class CyberSensorService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor rotationVector;
    
    // Logic state
    private float[] quaternion = new float[4];

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        
        if (rotationVector != null) {
            sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI);
        }
        
        // Start WebSocket Thread here...
        new Thread(this::connectWebSocket).start();
    }

    private void connectWebSocket() {
        // WebSocket connection logic would go here
        // Sending data provided by 'quaternion' and 'CyberAccessibilityService.currentPackage'
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getQuaternionFromVector(quaternion, event.values);
            // qX = quaternion[1], qY = quaternion[2], qZ = quaternion[3], qW = quaternion[0]
            // Need to map to Three.js coordinate system (usually Y-up)
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
