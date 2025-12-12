package com.cyber.pc.monitor;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeUtils {
    private static final Logger logger = LoggerFactory.getLogger(NativeUtils.class);

    public static String getActiveWindowTitle() {
        if (Platform.isWindows()) {
            return getWindowsActiveWindow();
        } else if (Platform.isLinux()) {
            // Linux implementation is complex due to Wayland/X11 diffs.
            // For now, we return a command-based fallback or "Linux Host"
            // Implementing full X11 lib calls here is out of scope for "minimal stack" unless requested.
            return "Linux Host (Active)";
        }
        return "Unknown OS";
    }

    private static String getWindowsActiveWindow() {
        try {
            char[] buffer = new char[1024];
            HWND hwnd = User32.INSTANCE.GetForegroundWindow();
            User32.INSTANCE.GetWindowText(hwnd, buffer, 1024);
            return Native.toString(buffer);
        } catch (Exception e) {
            logger.error("Failed to get active window", e);
            return "Error";
        }
    }
}
