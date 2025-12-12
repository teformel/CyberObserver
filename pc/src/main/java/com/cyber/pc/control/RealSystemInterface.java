package com.cyber.pc.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.net.URI;

public class RealSystemInterface implements SystemInterface {
    
    private static final Logger logger = LoggerFactory.getLogger(RealSystemInterface.class);

    @Override
    public void beep() {
        Toolkit.getDefaultToolkit().beep();
        logger.info("Executed: BEEP");
    }

    @Override
    public void lockScreen() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                Runtime.getRuntime().exec("rundll32.exe user32.dll,LockWorkStation");
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "pmset displaysleepnow"});
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "xdg-screensaver lock"});
            }
            logger.info("Executed: LOCK on " + os);
        } catch (Exception e) {
            logger.error("Failed to lock screen", e);
        }
    }

    @Override
    public void openUrl(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
                logger.info("Opened URL: {}", url);
            } catch (Exception e) {
                 logger.error("Failed to open URL: " + url, e);
            }
        } else {
             logger.warn("Desktop API not supported");
        }
    }
}
