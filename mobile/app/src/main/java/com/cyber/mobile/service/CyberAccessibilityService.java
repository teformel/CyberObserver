package com.cyber.mobile.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class CyberAccessibilityService extends AccessibilityService {

    private static final String TAG = "CyberAccess";
    public static String currentPackage = "UNKNOWN";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null) {
                currentPackage = event.getPackageName().toString();
                Log.d(TAG, "Current App: " + currentPackage);
                // In a real app, send this to the SensorService via EventBus or Singleton
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "Service Interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        setServiceInfo(info);
        Log.d(TAG, "CyberObserver Accessibility Connected");
    }
}
