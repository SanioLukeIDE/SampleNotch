package com.appslock.fingerprint.samplenotchapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import com.appslock.fingerprint.samplenotchapp.databinding.ActivityMainBinding;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_OPEN_CAMERA_PERMISSION = 102;
    private static final int WRITE_SETTINGS_PERMISSION_REQUEST = 103;
    private static final int NOTIFICATION_PERMISSION_RC = 123;

    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;

    // DND Toggle
    private static final String CHANNEL_ID = "my_channel";

    // Sound/Mute & Sound/Vibrate Toggle
    private AudioManager audioManager;

    // Music Play/Pause Toggle
    private MediaSession mediaSession;

    @SuppressLint("QueryPermissionsNeeded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        createNotificationChannel();
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Sound/Mute Toggle
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Music Play/Pause Toggle
        mediaSession = new MediaSession(this, "MyMediaSession");

        binding.toggleFlashlight.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else toggleFlashlight();
        });

        binding.turnScreenOff.setOnClickListener(v -> {});

        binding.toggleDnd.setOnClickListener(v -> requestNotificationPolicyPermission());

        binding.toggleSoundMuteMode.setOnClickListener(v->{
            if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            } else {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        });

        binding.toggleSoundVibrateMode.setOnClickListener(v->{
            if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            } else {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }
        });

        binding.openCamera.setOnClickListener(v->{
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else openCamera();
        });

        binding.openQuickDial.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + "+918692074192"));
            startActivity(intent);
        });

        binding.openWebsite.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
            intent.setPackage("com.android.chrome");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Uri webpage = Uri.parse("https://www.google.com");
                Intent intent1 = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent1.resolveActivity(getPackageManager()) != null) startActivity(intent1);
            }
        });

        binding.toggleAutoOrientations.setOnClickListener(v->{
            requestWriteSettingsPermission();
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NOTIFICATION_PERMISSION_RC) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
                toggleDoNotDisturb();
            }
        }
        else if (requestCode == WRITE_SETTINGS_PERMISSION_REQUEST) {
            if (android.provider.Settings.System.canWrite(this)) changeSystemOrientation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toggleFlashlight();
            } else {
                Toast.makeText(this, "Camera permission denied. Cannot use flashlight.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_OPEN_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied. Cannot open camera !!.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // -------------------------------------------- Flashlight Toggle Start -------------------------------------------- //
    private void toggleFlashlight() {
        if (cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, isFlashOn);
                isFlashOn = !isFlashOn;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
    // -------------------------------------------- Flashlight Toggle End -------------------------------------------- //


    // -------------------------------------------- Open Camera Start -------------------------------------------- //
    private void openCamera() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivity(openCameraIntent);
    }
    // -------------------------------------------- Open Camera End -------------------------------------------- //


    // -------------------------------------------- Toggle Orientations Start -------------------------------------------- //
    private void requestWriteSettingsPermission() {
        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, WRITE_SETTINGS_PERMISSION_REQUEST);
        } else {
            changeSystemOrientation();
        }
    }

    private void changeSystemOrientation() {
        ContentResolver contentResolver = getContentResolver();
        boolean autoRotateEnabled = android.provider.Settings.System.getInt(
                contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1;

        // Toggle the auto-rotate setting
        android.provider.Settings.System.putInt(
                contentResolver, Settings.System.ACCELEROMETER_ROTATION, autoRotateEnabled ? 0 : 1);
    }
    // -------------------------------------------- Toggle Orientations  Start -------------------------------------------- //


    // -------------------------------------------- DND Toggle Start -------------------------------------------- //
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "My Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPolicyPermission() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivityForResult(intent, NOTIFICATION_PERMISSION_RC);
        } else {
            toggleDoNotDisturb();
        }
    }

    private void toggleDoNotDisturb() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            if (notificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_NONE) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            } else {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
            }
        }
    }
    // -------------------------------------------- DND Toggle End -------------------------------------------- //
}

// 632001