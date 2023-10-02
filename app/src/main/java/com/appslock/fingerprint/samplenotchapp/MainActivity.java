package com.appslock.fingerprint.samplenotchapp;

import static com.appslock.fingerprint.samplenotchapp.Functions.setStatusBarHeight;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Toast;

import com.appslock.fingerprint.samplenotchapp.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_OPEN_CAMERA_PERMISSION = 102;
    private static final int WRITE_SETTINGS_PERMISSION_REQUEST = 103;
    private static final int ADMIN_PERMISSION_REQUEST = 104;
    private static final int OVERLAY_PERMISSION = 105;
    private static final int ACCESSIBILITY_PERMISSION = 106;
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
    DisplayCutout displayCutout;
    private Rect notchArea;

    // Lock the Screen
    private DevicePolicyManager devicePolicyManager;
    private ComponentName mComponentName;
    private ActivityManager activityManager;

    Rect notchBounds = null;

    @SuppressLint({"QueryPermissionsNeeded", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lock the Screen
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mComponentName = new ComponentName(MainActivity.this, NotchDeviceAdminReceiver.class);

        setStatusBarHeight(MainActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.changeBrightnessSeekbar.setMin(1);
        }
        if (Settings.System.canWrite(this)) {
            try {
                binding.changeBrightnessSeekbar.setProgress(Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS));
            } catch (Settings.SettingNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        saveStatusBarHeight();

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

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION);
        } else {
            Intent NotchServiceIntent = new Intent(this, NotchService.class);
            startService(NotchServiceIntent);
        }

        /*boolean accessibilityEnabled = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1;
        if (!accessibilityEnabled) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, ACCESSIBILITY_PERMISSION);
        }
        else{
            if(!Functions.isServiceRunning(MainActivity.this, MyAccessibilityService.class)){
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.appslock.fingerprint.samplenotchapp", "com.appslock.fingerprint.samplenotchapp.MyAccessibilityService"));
                startService(intent);
            }
        }*/

        checkNotch();

        eventClickListners();
    }

    private void saveStatusBarHeight() {
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentViewTop - statusBarHeight;
        Functions.putSharedPreferences(getApplicationContext(), Functions.APP_SETTINGS_PREF_NAME, Functions.STATUSBAR_HEIGHT, "int", titleBarHeight);
    }

    /*@Override
    public void onUserInteraction() {
        Log.e("check", "User Interaction Listening...");
        super.onUserInteraction();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e("check", "Dispatch Listening...");
        return super.dispatchTouchEvent(ev);
    }*/

    private void checkNotch() {
        Rect notchBounds = null;
        WindowInsets windowInsets = getWindow().getDecorView().getRootWindowInsets();
        if (windowInsets != null) {
            DisplayCutout displayCutout = windowInsets.getDisplayCutout();
            if (displayCutout != null) {
                List<Rect> boundingRects = displayCutout.getBoundingRects();
                if (!boundingRects.isEmpty()) {
                    for (Rect rect : boundingRects) {
                        notchBounds = rect;
                        Log.e("check_notch_position", "Left : " + rect.left + " & Top : " + rect.top + " & Right : " + rect.top + " & Bottom : " + rect.bottom);
                    }
                }
            }
        }
    }

    private void eventClickListners() {
        binding.openPowerLongPressMenu.setOnClickListener(v -> {
        });

        binding.toggleFlashlight.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else toggleFlashlight();
        });

        binding.toggleDnd.setOnClickListener(v -> requestNotificationPolicyPermission(1));

        binding.toggleSoundMuteMode.setOnClickListener(v -> {
            requestNotificationPolicyPermission(2);
        });

        binding.toggleSoundVibrateMode.setOnClickListener(v -> {
            if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            } else {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }
        });

        binding.openCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else openCamera();
        });

        binding.openQuickDial.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + "+918692074192"));
            startActivity(intent);
        });

        binding.openQrCodes.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://lens.google/#mode/search"));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No QR Code Reader installed in your device !! Please install a qr code reader to use this feature. ", Toast.LENGTH_SHORT).show();
            }
        });

        binding.openWebsite.setOnClickListener(v -> {
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

        binding.toggleAutoOrientations.setOnClickListener(v -> {
            requestWriteSettingsPermission(1);
        });

        binding.switchScreenBrightness.setOnClickListener(v -> {
            requestWriteSettingsPermission(2);
        });

        binding.toggleMusicPlayPause.setOnClickListener(v -> {

            v.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return false;
                }
            });
            long eventtime = SystemClock.uptimeMillis();
            if (audioManager.isMusicActive()) {
                KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
                audioManager.dispatchMediaKeyEvent(downEvent);

                KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
                audioManager.dispatchMediaKeyEvent(upEvent);
            } else {
                KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
                audioManager.dispatchMediaKeyEvent(downEvent);

                KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
                audioManager.dispatchMediaKeyEvent(upEvent);
            }
        });

        binding.playNextMusic.setOnClickListener(v -> {
            long eventTime = SystemClock.uptimeMillis();
            if (audioManager.isMusicActive()) {
                KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
                audioManager.dispatchMediaKeyEvent(upEvent);
            }
        });

        binding.playPreviousMusic.setOnClickListener(v -> {
            long eventTime = SystemClock.uptimeMillis();
            if (audioManager.isMusicActive()) {
                KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
                audioManager.dispatchMediaKeyEvent(upEvent);
            }
        });

        binding.turnScreenOff.setOnClickListener(v -> {
            if (devicePolicyManager.isAdminActive(mComponentName)) devicePolicyManager.lockNow();
            else {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "By enabling this device admin permission, you can enable the feature of locking your device");
                startActivityForResult(intent, ADMIN_PERMISSION_REQUEST);
            }
        });

        binding.performScreenshot.setOnClickListener(V -> {
            Intent intent = new Intent("android.intent.action.SCREENSHOT");
            startActivity(intent);
        });

        binding.openPowerLongPressMenu.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), FullscreenActivity.class));
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_UP) {

            if (notchArea != null) {
                if (notchArea.contains((int) x, (int) y)) {
                    Toast.makeText(this, "Notch Clicked.....", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Notch not exists", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NOTIFICATION_PERMISSION_RC) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
                toggleDoNotDisturb();
            }
        } else if (requestCode == WRITE_SETTINGS_PERMISSION_REQUEST) {
            if (Settings.System.canWrite(this)) changeSystemOrientation();
        } else if (requestCode == ADMIN_PERMISSION_REQUEST) {
            if (devicePolicyManager.isAdminActive(mComponentName))
                devicePolicyManager.lockNow();
        } else if (requestCode == OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                Intent NotchServiceIntent = new Intent(this, NotchService.class);
                startService(NotchServiceIntent);
            }
        } else if (requestCode == ACCESSIBILITY_PERMISSION) {
            boolean accessibilityEnabled = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1;
            if (accessibilityEnabled) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.appslock.fingerprint.samplenotchapp", "com.appslock.fingerprint.samplenotchapp.MyAccessibilityService"));
                startService(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toggleFlashlight();
            } else {
                Toast.makeText(this, "Camera permission denied. Cannot use flashlight.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_OPEN_CAMERA_PERMISSION) {
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
    private void requestWriteSettingsPermission(int option) {
        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, WRITE_SETTINGS_PERMISSION_REQUEST);
        } else {
            if (option == 1) changeSystemOrientation();
            else if (option == 2) changeSystemBrightness();
        }
    }

    private void changeSystemOrientation() {
        ContentResolver contentResolver = getContentResolver();
        boolean autoRotateEnabled = Settings.System.getInt(
                contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1;

        // Toggle the auto-rotate setting
        Settings.System.putInt(
                contentResolver, Settings.System.ACCELEROMETER_ROTATION, autoRotateEnabled ? 0 : 1);
    }
    // -------------------------------------------- Toggle Orientations  Start -------------------------------------------- //


    // -------------------------------------------- Toggle Orientations  Start -------------------------------------------- //
    private void changeSystemBrightness() {
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, binding.changeBrightnessSeekbar.getProgress());
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

    private void requestNotificationPolicyPermission(int action) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivityForResult(intent, NOTIFICATION_PERMISSION_RC);
        } else {
            if (action == 1) toggleDoNotDisturb();
            else {
                if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                } else {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
            }
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