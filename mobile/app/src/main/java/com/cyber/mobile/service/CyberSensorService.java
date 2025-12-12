package com.cyber.mobile.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import com.google.gson.Gson;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class CyberSensorService extends Service implements SensorEventListener {

    private static final String TAG = "CyberSensor";
    private static final String SERVER_URL = "ws://10.0.2.2:8080/ws"; // Android Emulator

    private WebSocket webSocket;
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    
    private SensorManager sensorManager;
    private Sensor rotationVector;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private String deviceId;
    
    // Data Buffers
    private float[] quaternion = new float[4];
    private float[] gravity;
    private float[] geomagnetic;
    private float azimuth = 0;
    
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // Persistent ID in real app, random for demo
        deviceId = "MOBILE_" + UUID.randomUUID().toString().substring(0, 8);
        Log.d(TAG, "Service Created, ID: " + deviceId);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        registerSensors();
        connectWebSocket();
        
        isRunning = true;
        startReportingLoop();
    }
    
    private void registerSensors() {
        if (accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (magnetometer != null) sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        if (rotationVector != null) sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI);
    }

    private void connectWebSocket() {
        Request request = new Request.Builder().url(SERVER_URL).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "Connected to Server");
                // Auth
                AuthPayload auth = new AuthPayload(deviceId, "Pixel_Scout", "MOBILE_SCOUT", "KEY_MOBILE");
                send(new CyberMessage("AUTH", gson.toJson(auth)));
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket Error: " + t.getMessage());
                // Simple Reconnect
                new Handler(Looper.getMainLooper()).postDelayed(() -> connectWebSocket(), 5000);
            }
            
            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "Closing: " + reason);
            }
        });
    }

    private void send(CyberMessage msg) {
        if (webSocket != null) {
            webSocket.send(gson.toJson(msg));
        }
    }

    private void startReportingLoop() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(100); // 10Hz
                    
                    if (webSocket != null) {
                        DeviceStatus status = new DeviceStatus();
                        status.deviceId = deviceId;
                        status.timestamp = System.currentTimeMillis();
                        status.batteryLevel = 0.85; // Mock
                        status.activeApp = CyberAccessibilityService.currentPackage != null ? 
                                           CyberAccessibilityService.currentPackage : "Unknown";
                        
                        SensorData data = new SensorData();
                        data.qX = quaternion[0];
                        data.qY = quaternion[1];
                        data.qZ = quaternion[2];
                        data.qW = quaternion[3];
                        data.azimuth = azimuth;
                        
                        status.sensorData = data;
                        
                        // Infer simple posture locally or let server do it? Server does it.
                        // But we can send extras if needed.
                        
                        CyberMessage cm = new CyberMessage("STATUS_UPDATE", gson.toJson(status));
                        send(cm);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Loop Error", e);
                }
            }
        }).start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getQuaternionFromVector(quaternion, event.values);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }
        
        if (gravity != null && geomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // orientation[0] is azimuth in radians
                azimuth = (float) Math.toDegrees(orientation[0]); 
                if (azimuth < 0) azimuth += 360;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        sensorManager.unregisterListener(this);
        if (webSocket != null) webSocket.close(1000, "Service Destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    // --- DTOs matching Server ---
    private static class CyberMessage {
        String type;
        String payloadJson;
        CyberMessage(String t, String p) { type = t; payloadJson = p; }
    }
    
    private static class AuthPayload {
        String deviceId, name, type, authKey;
        AuthPayload(String d, String n, String t, String a) { deviceId=d; name=n; type=t; authKey=a; }
    }
    
    // Matching com.cyber.common.model.DeviceStatus structure flattened or nested?
    // Server expects JSON that maps to DeviceStatus class.
    private static class DeviceStatus {
        String deviceId;
        long timestamp;
        double batteryLevel;
        boolean isCharging;
        double cpuLoad;
        double memoryUsage;
        String activeApp;
        SensorData sensorData;
        Map<String, String> extras;
    }
    
    private static class SensorData {
        float qX, qY, qZ, qW;
        float azimuth; // Added this field
        String posture;
    }
}

