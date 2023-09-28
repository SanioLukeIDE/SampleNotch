package com.appslock.fingerprint.samplenotchapp;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class NotchService extends Service {

    private static final String CHANNEL_ID = "MyForegroundServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private BroadcastReceiver notchBroadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("reveiver_check", "service called");
        createNotificationChannel();
        notchBroadcastReceiver = new NotchBroadcastReceiver();
        registerReceiver(notchBroadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
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
        unregisterReceiver(notchBroadcastReceiver);

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