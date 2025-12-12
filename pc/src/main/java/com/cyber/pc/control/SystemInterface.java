package com.cyber.pc.control;

public interface SystemInterface {
    void beep();
    void lockScreen();
    void openUrl(String url);
    void killProcess(String processName);
}
