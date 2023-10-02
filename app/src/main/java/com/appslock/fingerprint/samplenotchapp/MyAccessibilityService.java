package com.appslock.fingerprint.samplenotchapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityService extends AccessibilityService{

    HandlerThread handlerThread;
    Handler handler;
    DisplayManager displayManager;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        /*CharSequence packageName = event.getPackageName();
        if (event.getEventType() == AccessibilityEvent.TYPE_ANNOUNCEMENT) {
            if ("com.android.systemui".equals(packageName)) {
                Log.e("accessibilityservice_check", "Service - Status Bar clicked....");
                Display display = ((DisplayManager) getSystemService(Context.DISPLAY_SERVICE)).getDisplay(Display.DEFAULT_DISPLAY);
            }
        }*/
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.e("accessibilityservice_check", "Accessibility Service connected....");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.packageNames = new String[]{"com.android.systemui"};
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        setServiceInfo(info);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}