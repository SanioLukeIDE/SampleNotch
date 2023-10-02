package com.appslock.fingerprint.samplenotchapp;

import static android.content.Context.MODE_PRIVATE;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.RelativeLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Functions {

    public static final String APP_SETTINGS_PREF_NAME = "AppDetails";
    public static final String STATUSBAR_HEIGHT = "statusBarHeight";
    public static final String STATUSBAR_WIDTH = "statusBarWidth";

    public static void clearSharedPrefs(Context context, String pref_name) {
        SharedPreferences pref = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        pref.edit().clear().commit();
    }

    public static <Any> void putSharedPreferences(@NotNull Context context, String prefs_name, String prefs_objname, @NotNull String type, Any set_val) {

        SharedPreferences.Editor pref_edit = context.getSharedPreferences(prefs_name, MODE_PRIVATE).edit();
        switch (type) {
            case "string":
                pref_edit.putString(prefs_objname, (String) set_val);
                break;

            case "int":
                pref_edit.putInt(prefs_objname, (Integer) set_val);
                break;

            case "boolean":
                pref_edit.putBoolean(prefs_objname, (Boolean) set_val);
                break;

            case "float":
                pref_edit.putFloat(prefs_objname, (Float) set_val);
                break;

            case "long":
                pref_edit.putLong(prefs_objname, (Long) set_val);
                break;
        }
        pref_edit.apply();
    }

    public static <Any> Any getSharedPreferences(@NotNull Context context, String prefs_name, String prefs_objname, @NotNull String type, Any default_val) {

        SharedPreferences pref = context.getSharedPreferences(prefs_name, MODE_PRIVATE);
        switch (type) {

            case "string":
                String stringval = pref.getString(prefs_objname, (String) default_val);
                return ((Any) (String) stringval);

            case "int":
                int intval = pref.getInt(prefs_objname, (Integer) default_val);
                return ((Any) (Integer) intval);

            case "boolean":
                Boolean boolval = pref.getBoolean(prefs_objname, (Boolean) default_val);
                return ((Any) (Boolean) boolval);

            case "float":
                Float floatval = pref.getFloat(prefs_objname, (Float) default_val);
                return ((Any) (Float) floatval);

            case "long":
                Long longval = pref.getLong(prefs_objname, (Long) default_val);
                return ((Any) (Long) longval);

            default:
                return null;
        }
    }

    public static RelativeLayout findLayoutContainingNotch(View view, Rect notchArea) {
        if (view == null || notchArea == null) {
            return null;
        }

        Rect viewRect = new Rect();
        view.getGlobalVisibleRect(viewRect);
        if (Rect.intersects(viewRect, notchArea)) {
            if (view instanceof RelativeLayout) {
                return (RelativeLayout) view;
            }

            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childView = viewGroup.getChildAt(i);
                    RelativeLayout layout = findLayoutContainingNotch(childView, notchArea);
                    if (layout != null) {
                        return layout;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void setStatusBarHeight(Activity activity) {
        Rect notchArea = new Rect(0,0,0,0);
        DisplayCutout displayCutout = null;
        Display defaultDisplay = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if (defaultDisplay == null) {
            defaultDisplay = ((DisplayManager) activity.getSystemService(Context.DISPLAY_SERVICE)).getDisplay(0);
        }
        if (defaultDisplay != null) {
            try {
                displayCutout = defaultDisplay.getCutout();
            } catch (Throwable unused) {
                if (activity.getWindow() != null && activity.getWindow().getDecorView().getRootWindowInsets() != null) {
                    displayCutout = activity.getWindow().getDecorView().getRootWindowInsets().getDisplayCutout();
                }
            }
        }
        if (displayCutout != null) {
            List<Rect> boundingRects = displayCutout.getBoundingRects();
            if (!boundingRects.isEmpty()) {
                for (Rect rect : boundingRects) {
                    notchArea = new Rect(rect.left, rect.top, rect.right, rect.bottom);
                }
            }
        }

        Log.e("notch_area", "Left : " + notchArea.left + " & Right : " + notchArea.right
                + " & Top : " + notchArea.top + " & Bottom : " + notchArea.bottom);
    }

    public static int getStatusBarHeight(Context context) {
        return Functions.getSharedPreferences(context, Functions.APP_SETTINGS_PREF_NAME, Functions.STATUSBAR_HEIGHT, "int", 24);
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

}
