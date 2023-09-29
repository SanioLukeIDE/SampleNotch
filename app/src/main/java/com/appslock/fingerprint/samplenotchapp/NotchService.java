package com.appslock.fingerprint.samplenotchapp;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.List;

public class NotchService extends Service {

    private static final String CHANNEL_ID = "MyForegroundServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private WindowManager windowManager;
    private View overlayView;
    Rect notchBounds = null;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        Log.e("service_check", "service called....");
        overlayView = View.inflate(getApplicationContext(), R.layout.notch_full_screen, null);
        overlayView.setBackgroundColor(Color.TRANSPARENT);

        overlayView.findViewById(R.id.fullscreen_content1).setOnClickListener(v -> {
            Toast.makeText(this, "Notch Clicked.....", Toast.LENGTH_SHORT).show();
        });

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.START | Gravity.TOP;
        windowManager.addView(overlayView, params);

        overlayView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    Log.e("service_check", "service - Touch Detected....");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        WindowInsets windowInsets = v.getRootWindowInsets();
                        if (windowInsets.getDisplayCutout() != null) {
                            Log.e("service_check", "service - Notch Detected...");
                            DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                            List<Rect> boundingRects = displayCutout.getBoundingRects();
                            if (!boundingRects.isEmpty()) {
                                for (Rect rect : boundingRects) notchBounds = rect;
                            }
                        }
                    }
                    if (notchBounds != null && notchBounds.contains((int) event.getX(), (int) event.getY())) {
                        Log.e("service_check", "Service - Notch Touched and action is performed....");
                    }
                }
                return true;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("reveiver_check", "service called - 1");
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("My Foreground Service")
                .setContentText("Running in the foreground")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "My Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}