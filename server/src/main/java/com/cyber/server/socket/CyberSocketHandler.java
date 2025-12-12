package com.cyber.server.socket;

import com.cyber.common.model.CyberMessage;
import com.cyber.common.model.DeviceIdentity;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class CyberSocketHandler extends TextWebSocketHandler {

    private final Gson gson = new Gson();
    // Validated sessions
    private final ConcurrentHashMap<String, DeviceIdentity> deviceMap = new ConcurrentHashMap<>();
    // All open sessions
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("New Connection: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        deviceMap.remove(session.getId()); // Cleanup
        System.out.println("Closed Connection: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        try {
            // Basic minimal parsing to check type - real implementation might be more robust
            CyberMessage msg = gson.fromJson(payload, CyberMessage.class);
            
            if (msg.getType() == CyberMessage.Type.AUTH) {
                handleAuth(session, msg.getPayloadJson());
            } else if (msg.getType() == CyberMessage.Type.STATUS_UPDATE) {
                // Broadcast status to everyone (Simple V1)
                broadcast(payload);
            }
        } catch (Exception e) {
            System.err.println("Error processing Msg: " + e.getMessage());
        }
    }

    private void handleAuth(WebSocketSession session, String json) {
        DeviceIdentity identity = gson.fromJson(json, DeviceIdentity.class);
        deviceMap.put(session.getId(), identity);
        System.out.println("Device Authenticated: " + identity.getName() + " [" + identity.getType() + "]");
    }

    private void broadcast(String msg) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(msg));
                } catch (IOException e) {
                    // Ignore failure
                }
            }
        }
    }
}
