package com.cyber.server.socket;

import com.cyber.common.model.CyberMessage;
import com.cyber.common.model.DeviceIdentity;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.cyber.server.service.InferenceService;
import com.cyber.common.model.DeviceStatus;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class CyberSocketHandler extends TextWebSocketHandler {

    private final Gson gson = new Gson();
    private final InferenceService inferenceService;
    
    // Validated sessions
    private final ConcurrentHashMap<String, DeviceIdentity> deviceMap = new ConcurrentHashMap<>();
    // All open sessions
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public CyberSocketHandler(InferenceService inferenceService) {
        this.inferenceService = inferenceService;
    }

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

    private static final int MAX_MSG_SIZE = 1024 * 64; // 64KB

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        // 1. Security: Size Limit
        if (payload.length() > MAX_MSG_SIZE) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        try {
            CyberMessage msg = gson.fromJson(payload, CyberMessage.class);
            if (msg == null || msg.getType() == null) return;
            
            if (msg.getType() == CyberMessage.Type.AUTH) {
                handleAuth(session, msg.getPayloadJson());
            } else if (msg.getType() == CyberMessage.Type.STATUS_UPDATE) {
                // 2. Security: Ensure authenticated before broadcasting
                if (!deviceMap.containsKey(session.getId())) {
                    return;
                }
                
                // 3. Intelligence: Infer Posture
                DeviceStatus status = gson.fromJson(msg.getPayloadJson(), DeviceStatus.class);
                inferenceService.inferPosture(status);
                
                // Re-wrap and broadcast
                msg.setPayloadJson(gson.toJson(status));
                broadcast(gson.toJson(msg));
            }
        } catch (Exception e) {
            System.err.println("Error processing Msg: " + e.getMessage());
        }
    }

    private void handleAuth(WebSocketSession session, String json) {
        if (json == null || json.isEmpty()) return;
        
        try {
            DeviceIdentity identity = gson.fromJson(json, DeviceIdentity.class);
            
            // 3. Security: Input Validation
            if (!isValidId(identity.getDeviceId())) {
                System.err.println("Invalid Device ID: " + identity.getDeviceId());
                session.close(CloseStatus.BAD_DATA);
                return;
            }
            
            // 4. Security: sanitize name
            identity.setName(sanitize(identity.getName()));

            deviceMap.put(session.getId(), identity);
            System.out.println("Device Authenticated: " + identity.getName() + " [" + identity.getType() + "]");
        } catch (Exception e) {
             System.err.println("Auth Error: " + e.getMessage());
        }
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

    private boolean isValidId(String id) {
        // Alphanumeric, underscores, hyphens only. Length 1-50.
        return id != null && id.matches("^[a-zA-Z0-9_-]{1,50}$");
    }

    private String sanitize(String input) {
        if (input == null) return "";
        // Remove HTML tags / scripts
        return input.replaceAll("<[^>]*>", "").trim();
    }
}
