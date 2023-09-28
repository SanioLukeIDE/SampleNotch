package com.appslock.fingerprint.samplenotchapp;

import static android.content.Context.WINDOW_SERVICE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import java.util.List;

public class NotchBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("reveiver_check", "notch service detected....");
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        View overlayView = new View(context);
        overlayView.setBackgroundColor(Color.TRANSPARENT);

        overlayView.setOnTouchListener((v, event) -> {

            Rect notchBounds = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Log.e("reveiver_check", "notch detected");
                WindowInsets windowInsets = v.getRootWindowInsets();
                if (windowInsets.getDisplayCutout() != null) {
                    DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                    List<Rect> boundingRects = displayCutout.getBoundingRects();
                    if (!boundingRects.isEmpty()) {
                        for (Rect rect : boundingRects) notchBounds = rect;
                    }
                }
            }

            if (notchBounds!=null && notchBounds.contains((int) event.getX(), (int) event.getY())) {
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                long eventtime = SystemClock.uptimeMillis();
                if (audioManager.isMusicActive()) {
                    KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
                    audioManager.dispatchMediaKeyEvent(downEvent);
                    Log.e("reveiver_check", "notch service triggered and function performed");
                    KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
                    audioManager.dispatchMediaKeyEvent(upEvent);
                } else {
                    KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
                    audioManager.dispatchMediaKeyEvent(downEvent);
                    Log.e("reveiver_check", "notch service triggered and function performed");
                    KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
                    audioManager.dispatchMediaKeyEvent(upEvent);
                }
                return true;
            }
            Log.e("reveiver_check", "notch service triggered but function not performed");
            return false;
        });
        windowManager.addView(overlayView, params);
    }
}
