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

    private okhttp3.WebSocket webSocket;
    private final okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
    private final com.google.gson.Gson gson = new com.google.gson.Gson();
    private static final String SERVER_URL = "ws://10.0.2.2:8080/ws"; // Android Emulator -> Host

    // Data
    private String deviceId;

    @Override
    public void onCreate() {
        super.onCreate();
        deviceId = "MOBILE_" + java.util.UUID.randomUUID().toString().substring(0, 8);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        
        if (rotationVector != null) {
            sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI);
        }
        
        connectWebSocket();
    }

    private void connectWebSocket() {
        okhttp3.Request request = new okhttp3.Request.Builder().url(SERVER_URL).build();
        webSocket = client.newWebSocket(request, new okhttp3.WebSocketListener() {
            @Override
            public void onOpen(okhttp3.WebSocket webSocket, okhttp3.Response response) {
                Log.d("CyberMobile", "Connected!");
                // Auth
                // Construct Identity manually (no shared lib in Android usually unless shaded)
                // Just sending raw JSON for simplicity if shared lib isn't linked
                String authJson = "{\"deviceId\":\"" + deviceId + "\", \"name\":\"Android Scout\", \"type\":\"MOBILE_SCOUT\", \"authKey\":\"KEY_MOB\"}";
                String msg = "{\"type\":\"AUTH\", \"payloadJson\":" + com.google.gson.JsonParser.parseString(authJson).toString() + "}";
                // Actually, let's just use a simple string construction to avoid dependency complexity if Gson isn't fully set up for nested string escaping
                // Proper way: Use inner helper class
                
                AuthPayload auth = new AuthPayload(deviceId, "Android Scout", "MOBILE_SCOUT", "KEY_MOB");
                CyberMessage cm = new CyberMessage("AUTH", gson.toJson(auth));
                webSocket.send(gson.toJson(cm));
            }

            @Override
            public void onMessage(okhttp3.WebSocket webSocket, String text) {
                Log.d("CyberMobile", "RX: " + text);
            }

            @Override
            public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
                Log.d("CyberMobile", "Closed: " + reason);
            }

            @Override
            public void onFailure(okhttp3.WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e("CyberMobile", "Error: " + t.getMessage());
            }
        });

        // Reporting Loop
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(200); // 5Hz
                    if (webSocket != null) {
                        StatusPayload status = new StatusPayload();
                        status.setRotation(quaternion);
                        status.setActiveApp(CyberAccessibilityService.currentPackage); // Static access
                        
                        CyberMessage cm = new CyberMessage("STATUS_UPDATE", gson.toJson(status));
                        webSocket.send(gson.toJson(cm));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getQuaternionFromVector(quaternion, event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    // Inner classes to mimic Shared Models specific for Mobile
    private static class CyberMessage {
        String type;
        String payloadJson;
        CyberMessage(String t, String p) { type = t; payloadJson = p; }
    }
    private static class AuthPayload {
        String deviceId, name, type, authKey;
        AuthPayload(String d, String n, String t, String a) { deviceId=d; name=n; type=t; authKey=a; }
    }
    private static class StatusPayload {
        float[] rotation;
        String activeApp;
        void setRotation(float[] r) { rotation = r; }
        void setActiveApp(String a) { activeApp = a; }
    }
}
