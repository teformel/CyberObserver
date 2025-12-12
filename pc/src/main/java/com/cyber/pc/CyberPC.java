package com.cyber.pc;

import com.cyber.common.model.CyberMessage;
import com.cyber.common.model.DeviceIdentity;
import com.cyber.common.model.DeviceStatus;
import com.cyber.common.model.DeviceType;
import com.cyber.pc.monitor.SystemMonitor;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.cyber.pc.control.CommandProcessor;
import com.cyber.pc.control.RealSystemInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CyberPC {

    private static final String SERVER_URI = "ws://localhost:8080/ws"; 
    private static final String DEVICE_ID = "PC_" + UUID.randomUUID().toString().substring(0, 8);
    private static final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(CyberPC.class);
    private static final CommandProcessor processor = new CommandProcessor(new RealSystemInterface());

    public static void main(String[] args) {
        logger.info("Initializing CyberPC Agent...");
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
                    logger.info("RX: {}", message);
                    try {
                        CyberMessage msg = gson.fromJson(message, CyberMessage.class);
                        if (msg.getType() == CyberMessage.Type.CONTROL_CMD) {
                            processor.process(msg.getPayloadJson());
                        }
                    } catch (Exception e) {
                        logger.error("Cmd Error", e);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.info("Disconnected: {}", reason);
                }

                @Override
                public void onError(Exception ex) {
                    logger.error("WebSocket Error", ex);
                }
            };
            
            logger.info("Connecting to {}...", SERVER_URI);
            client.connectBlocking(5, TimeUnit.SECONDS);

            // Reporting Loop
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                if (client.isOpen()) {
                    DeviceStatus status = monitor.captureStatus(DEVICE_ID);
                    CyberMessage msg = new CyberMessage(CyberMessage.Type.STATUS_UPDATE, gson.toJson(status));
                    client.send(gson.toJson(msg));
                    // logger.debug("Sent Heartbeat"); // too verbose
                }
            }, 1, 1, TimeUnit.SECONDS);
            
            // Keep alive
            while(true) { Thread.sleep(10000); }

        } catch (Exception e) {
            logger.error("Fatal Error", e);
        }
    }
}
