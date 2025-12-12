package com.cyber.pc;

import com.cyber.common.model.CyberMessage;
import com.cyber.common.model.DeviceIdentity;
import com.cyber.common.model.DeviceStatus;
import com.cyber.common.model.DeviceType;
import com.cyber.pc.monitor.SystemMonitor;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CyberPC {

    private static final String SERVER_URI = "ws://localhost:8080/ws"; // Adjust if needed
    private static final String DEVICE_ID = "PC_" + UUID.randomUUID().toString().substring(0, 8);
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        System.out.println("Initializing CyberPC Agent...");
        SystemMonitor monitor = new SystemMonitor();

        try {
            WebSocketClient client = new WebSocketClient(new URI(SERVER_URI)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to CyberServer!");
                    // Auth
                    DeviceIdentity identity = new DeviceIdentity(DEVICE_ID, "MyPC", DeviceType.PC_SENTRY, "KEY_123");
                    send(gson.toJson(new CyberMessage(CyberMessage.Type.AUTH, gson.toJson(identity))));
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("RX: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };

            System.out.println("Connecting to " + SERVER_URI + "...");
            client.connectBlocking(5, TimeUnit.SECONDS);

            // Reporting Loop
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                if (client.isOpen()) {
                    DeviceStatus status = monitor.captureStatus(DEVICE_ID);
                    CyberMessage msg = new CyberMessage(CyberMessage.Type.STATUS_UPDATE, gson.toJson(status));
                    client.send(gson.toJson(msg));
                    System.out.print(".");
                }
            }, 1, 1, TimeUnit.SECONDS);
            
            // Keep alive
            while(true) { Thread.sleep(10000); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
