package com.appslock.fingerprint.samplenotchapp;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class Functions {

    public static boolean isAccessibilityServiceEnabled(Context context, String accessibilityServiceName) {
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        if (enabledServices != null) {
            TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
            colonSplitter.setString(enabledServices);

            while (colonSplitter.hasNext()) {
                String service = colonSplitter.next();
                if (service.equals(accessibilityServiceName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
