package com.example;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

/**
 * SettingsHelper provides reliable, safe official Android settings shortcuts
 * with graceful fallbacks.
 */
public class SettingsHelper {

    private final Context context;

    public SettingsHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean openDisplaySettings() {
        return launchIntent(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
    }

    public boolean openRefreshRateSettings() {
        // High refresh rate / Smooth display is under Display or Advanced Display
        Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
        return launchIntent(intent);
    }

    public boolean openBatterySettings() {
        Intent intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
        if (!hasActivity(intent)) {
            intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
        }
        return launchIntent(intent);
    }

    public boolean openBatteryOptimizationSettings() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        } else {
            intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
        }
        return launchIntent(intent);
    }

    public boolean openDoNotDisturbSettings() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        } else {
            intent = new Intent(Settings.ACTION_SOUND_SETTINGS);
        }
        return launchIntent(intent);
    }

    public boolean openDeveloperOptions() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        if (!launchIntent(intent)) {
            // If developer options are not unlocked yet, navigate to About Phone
            Intent aboutIntent = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
            Toast.makeText(context, "Tap Build Number 7 times in About Phone to enable Developer Options", Toast.LENGTH_LONG).show();
            return launchIntent(aboutIntent);
        }
        return true;
    }

    public boolean openAppDetails(String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + (packageName != null ? packageName : context.getPackageName())));
        return launchIntent(intent);
    }

    public boolean openGameDashboard() {
        // On Android 12+ (API 31+), some devices support Game Mode settings
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                Intent intent = new Intent("android.settings.GAME_SETTINGS");
                if (hasActivity(intent)) {
                    return launchIntent(intent);
                }
            } catch (Exception ignored) {
            }
        }
        // Fallback to display / system settings with clear notice
        Toast.makeText(context, "Game Dashboard integrated via Game Turbo Pro HUD & Android Display", Toast.LENGTH_SHORT).show();
        return openDisplaySettings();
    }

    public boolean isDndGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            return nm != null && nm.isNotificationPolicyAccessGranted();
        }
        return true;
    }

    public boolean isIgnoringBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm != null && pm.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true;
    }

    private boolean launchIntent(Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "Setting shortcut unavailable on this ROM", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean hasActivity(Intent intent) {
        return intent.resolveActivity(context.getPackageManager()) != null;
    }
}
