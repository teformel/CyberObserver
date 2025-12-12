package com.cyber.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.cyber.server.socket.CyberSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CyberSocketHandler cyberSocketHandler;

    public WebSocketConfig(CyberSocketHandler cyberSocketHandler) {
        this.cyberSocketHandler = cyberSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(cyberSocketHandler, "/ws")
                .setAllowedOrigins("*"); // Allow Debugging from local HTML files
    }
}
