package com.cyber.pc.config;

import java.util.concurrent.atomic.AtomicBoolean;

public class CyberPCConfig {
    
    private static final CyberPCConfig INSTANCE = new CyberPCConfig();
    
    // Thread-safe settings
    private final AtomicBoolean privacyMode = new AtomicBoolean(false);

    private CyberPCConfig() {}

    public static CyberPCConfig getInstance() {
        return INSTANCE;
    }

    public boolean isPrivacyMode() {
        return privacyMode.get();
    }

    public void setPrivacyMode(boolean enabled) {
        privacyMode.set(enabled);
    }
}
