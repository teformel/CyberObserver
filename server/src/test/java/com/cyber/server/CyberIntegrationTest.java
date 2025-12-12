package com.cyber.server;

import com.cyber.common.model.CyberMessage;
import com.cyber.common.model.DeviceIdentity;
import com.cyber.common.model.DeviceType;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CyberIntegrationTest {

    @LocalServerPort
    private int port;

    private final StandardWebSocketClient client = new StandardWebSocketClient();
    private final Gson gson = new Gson();

    @Test
    void testWebSocketConnectionAndAuth() throws Exception {
        BlockingQueue<String> messages = new LinkedBlockingDeque<>();

        WebSocketSession session = client.execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                messages.offer(message.getPayload());
            }
        }, "ws://localhost:" + port + "/ws").get(1, TimeUnit.SECONDS);

        assertTrue(session.isOpen(), "Session should be open");

        // Prepare AUTH message
        DeviceIdentity id = new DeviceIdentity();
        id.setDeviceId("TestDevice_Integration");
        id.setName("Integration Tester");
        id.setType(DeviceType.PC_SENTRY);
        id.setAuthKey("secret");

        CyberMessage authMsg = new CyberMessage();
        authMsg.setType(CyberMessage.Type.AUTH);
        authMsg.setPayloadJson(gson.toJson(id));

        // Send AUTH
        session.sendMessage(new TextMessage(gson.toJson(authMsg)));

        // In a real scenario, the server might echo back an ACK or we might verify side effects.
        // For now, we verify that the connection remains open and doesn't crash.
        Thread.sleep(500);
        assertTrue(session.isOpen());

        session.close();
    }
}
