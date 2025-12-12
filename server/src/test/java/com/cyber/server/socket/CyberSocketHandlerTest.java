package com.cyber.server.socket;

import com.cyber.common.model.CyberMessage;
import com.cyber.common.model.DeviceIdentity;
import com.cyber.common.model.DeviceType;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CyberSocketHandlerTest {

    private CyberSocketHandler handler;
    private final Gson gson = new Gson();

    @Mock
    private WebSocketSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new CyberSocketHandler();
        when(session.getId()).thenReturn("s1");
        when(session.isOpen()).thenReturn(true);
    }

    @Test
    void testAuthWithValidId() throws Exception {
        // Prepare AUTH message
        DeviceIdentity id = new DeviceIdentity();
        id.setDeviceId("MyPC_01");
        id.setName("My PC");
        // id.setType(DeviceType.PC); // Ignoring enum for simplicity here or import correctly

        CyberMessage cm = new CyberMessage();
        cm.setType(CyberMessage.Type.AUTH);
        cm.setPayloadJson(gson.toJson(id));

        TextMessage msg = new TextMessage(gson.toJson(cm));

        // Act
        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, msg);

        // Assert - No close method called implies success (logic in handler closes on error)
        verify(session, never()).close(any());
    }

    @Test
    void testAuthWithInvalidId() throws Exception {
        // Prepare Message with XSS ID
        DeviceIdentity id = new DeviceIdentity();
        id.setDeviceId("<script>alert(1)</script>"); 

        CyberMessage cm = new CyberMessage();
        cm.setType(CyberMessage.Type.AUTH);
        cm.setPayloadJson(gson.toJson(id));

        TextMessage msg = new TextMessage(gson.toJson(cm));

        // Act
        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, msg);

        // Assert - Should close session
        verify(session).close(CloseStatus.BAD_DATA);
    }

    @Test
    void testBroadcastNotAllowedWithoutAuth() throws Exception {
         // Prepare Status Message
        CyberMessage cm = new CyberMessage();
        cm.setType(CyberMessage.Type.STATUS_UPDATE);
        cm.setPayloadJson("{}");

        TextMessage msg = new TextMessage(gson.toJson(cm));

        // Act
        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, msg); // No Auth sent previously

        // Assert
        // Logic: if (!deviceMap.containsKey) return;
        // So broadcast() should NOT be called.
        // Since broadcast loops over sessions, and session is in sessions list, 
        // if broadcast WAS called, it would try to sendMessage to this session.
        verify(session, never()).sendMessage(any());
    }
}
