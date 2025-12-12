package com.cyber.pc.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class CommandProcessorTest {

    private CommandProcessor processor;

    @Mock
    private SystemInterface system;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        processor = new CommandProcessor(system);
    }

    @Test
    void testBeep() {
        processor.process("\"BEEP\"");
        verify(system).beep();
    }

    @Test
    void testLock() {
        processor.process("LOCK");
        verify(system).lockScreen();
    }

    @Test
    void testOpenUrl() {
        processor.process("URL:https://example.com");
        verify(system).openUrl("https://example.com");
    }

    @Test
    void testInvalidUrlBlocked() {
        // Prevent file schema or risky chars
        processor.process("URL:file:///etc/passwd");
        verify(system, never()).openUrl(anyString());
    }

    @Test
    void testMalformedUrlLimited() {
         processor.process("URL:javascript:alert(1)");
         verify(system, never()).openUrl(anyString());
    }

    @Test
    void testEmptyCommand() {
        processor.process("");
        verifyNoInteractions(system);
    }
}
