package com.cyber.pc.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CyberPCConfigTest {

    @Test
    void testSingleton() {
        CyberPCConfig config1 = CyberPCConfig.getInstance();
        CyberPCConfig config2 = CyberPCConfig.getInstance();
        assertSame(config1, config2, "Should be same instance");
    }

    @Test
    void testPrivacyModeToggle() {
        CyberPCConfig config = CyberPCConfig.getInstance();
        
        // Default false
        assertFalse(config.isPrivacyMode());
        
        // Toggle true
        config.setPrivacyMode(true);
        assertTrue(config.isPrivacyMode());
        
        // Toggle false
        config.setPrivacyMode(false);
        assertFalse(config.isPrivacyMode());
    }
}
