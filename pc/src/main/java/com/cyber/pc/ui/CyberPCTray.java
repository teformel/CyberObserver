package com.cyber.pc.ui;

import com.cyber.pc.config.CyberPCConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class CyberPCTray {
    
    private static final Logger logger = LoggerFactory.getLogger(CyberPCTray.class);

    public static void init() {
        if (!SystemTray.isSupported()) {
            logger.warn("SystemTray is not supported on this platform. UI disabled.");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        
        // Simple 16x16 red/green box icon if no image
        Image icon = createIcon(Color.CYAN);

        PopupMenu popup = new PopupMenu();
        
        // Privacy Mode Checkbox
        CheckboxMenuItem privacyItem = new CheckboxMenuItem("Privacy Mode (Hide Data)");
        privacyItem.setState(CyberPCConfig.getInstance().isPrivacyMode());
        privacyItem.addItemListener(e -> {
            boolean newState = ((CheckboxMenuItem) e.getSource()).getState();
            CyberPCConfig.getInstance().setPrivacyMode(newState);
            logger.info("Privacy Mode toggled to: {}", newState);
            updateIcon(tray, icon, newState);
        });
        
        MenuItem exitItem = new MenuItem("Exit Agent");
        exitItem.addActionListener(e -> {
            logger.info("User requested Exit via Tray.");
            System.exit(0);
        });

        popup.add(privacyItem);
        popup.addSeparator();
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(icon, "CyberObserver Agent", popup);
        trayIcon.setImageAutoSize(true);
        
        try {
            tray.add(trayIcon);
            logger.info("System Tray initialized.");
        } catch (AWTException e) {
            logger.error("TrayIcon could not be added.", e);
        }
    }

    private static void updateIcon(SystemTray tray, Image baseIcon, boolean privacy) {
         // Could change icon color here indicating protected state
         // For now just log
    }

    private static Image createIcon(Color color) {
        int w = 16, h = 16;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillOval(1, 1, w-2, h-2);
        g.setColor(Color.BLACK);
        g.drawOval(1, 1, w-2, h-2);
        g.dispose();
        return img;
    }
}
