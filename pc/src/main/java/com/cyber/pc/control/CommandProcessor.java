package com.cyber.pc.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);
    private final SystemInterface system;

    public CommandProcessor(SystemInterface system) {
        this.system = system;
    }

    public void process(String cmdJson) {
        if (cmdJson == null || cmdJson.isEmpty()) return;

        // V1: Simple String commands
        // In future replace with strict JSON parsing
        String action = cmdJson.replace("\"", "").trim();
        
        logger.info("Processing Command: {}", action);

        if ("BEEP".equalsIgnoreCase(action)) {
            system.beep();
        } else if ("LOCK".equalsIgnoreCase(action)) {
            system.lockScreen();
        } else if (action.startsWith("KILL:")) {
            String processName = action.substring(5).trim();
            if (isSafeToKill(processName)) {
                logger.warn("AUDIT: Executing KILL on process '{}'", processName);
                killProcess(processName);
            } else {
                logger.warn("AUDIT: BLOCKED KILL attempt on protected process '{}'", processName);
            }
        } else if (action.startsWith("URL:")) {
            String url = action.substring(4).trim();
            if (isValidUrl(url)) {
                system.openUrl(url);
            } else {
                logger.warn("Blocked potentially unsafe or invalid URL: {}", url);
            }
        } else {
             logger.warn("Unknown Command: {}", action);
        }
    }

    private boolean isSafeToKill(String process) {
        // Blacklist critical system processes
        String[] protectedProcs = {"system", "svchost", "csrss", "winlogon", "explorer", "java", "kernel"};
        String target = process.toLowerCase();
        
        // 1. Basic sanity check (alphanumeric + dot/dash/underscore)
        if (!target.matches("^[a-zA-Z0-9._-]+$")) return false;

        // 2. Blacklist check
        for (String p : protectedProcs) {
            if (target.contains(p)) return false;
        }
        return true;
    }

    private void killProcess(String processName) {
        system.killProcess(processName);
    }

    private boolean isValidUrl(String url) {
        // Security: Prevent weird URI schemes or injection
        // Allow http/https only
        return url.matches("^https?://[\\w\\-\\.]+(?::\\d+)?(?:/\\S*)?$");
    }
}
