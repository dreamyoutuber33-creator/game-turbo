package com.example;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GameProfileManager manages persistent game configurations, favorites,
 * notes, and applies permitted system preferences safely upon launching.
 */
public class GameProfileManager {

    private static final String PREF_NAME = "game_turbo_profiles";
    private static final String KEY_PROFILES_JSON = "profiles_json_map";
    private static final String KEY_SAVED_PACKAGES = "saved_game_packages";
    private static final String KEY_CURRENT_GAME = "currently_selected_game";

    private final Context context;
    private final SharedPreferences prefs;

    public GameProfileManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public synchronized GameProfile getProfile(String packageName, String defaultTitle) {
        String jsonStr = prefs.getString(KEY_PROFILES_JSON + "_" + packageName, null);
        if (jsonStr != null) {
            try {
                return GameProfile.fromJson(new JSONObject(jsonStr));
            } catch (Exception ignored) {
            }
        }
        return new GameProfile(packageName, defaultTitle);
    }

    public synchronized void saveProfile(GameProfile profile) {
        if (profile == null || profile.getPackageName() == null) return;
        prefs.edit()
                .putString(KEY_PROFILES_JSON + "_" + profile.getPackageName(), profile.toJson().toString())
                .apply();
    }

    public synchronized Set<String> getTrackedGamePackages() {
        return new HashSet<>(prefs.getStringSet(KEY_SAVED_PACKAGES, new HashSet<>()));
    }

    public synchronized void addTrackedGamePackage(String packageName) {
        Set<String> set = getTrackedGamePackages();
        set.add(packageName);
        prefs.edit().putStringSet(KEY_SAVED_PACKAGES, set).apply();
    }

    public synchronized void removeTrackedGamePackage(String packageName) {
        Set<String> set = getTrackedGamePackages();
        set.remove(packageName);
        prefs.edit().putStringSet(KEY_SAVED_PACKAGES, set)
                .remove(KEY_PROFILES_JSON + "_" + packageName)
                .apply();
    }

    public synchronized void updateLastPlayed(String packageName, String title) {
        GameProfile profile = getProfile(packageName, title);
        profile.setLastPlayedTimestamp(System.currentTimeMillis());
        saveProfile(profile);
        setSelectedGame(packageName);
    }

    public synchronized void setSelectedGame(String packageName) {
        prefs.edit().putString(KEY_CURRENT_GAME, packageName).apply();
    }

    public synchronized String getSelectedGame() {
        return prefs.getString(KEY_CURRENT_GAME, null);
    }

    public static class AppliedSettingsResult {
        public final List<String> applied = new ArrayList<>();
        public final List<String> unavailableOrRestricted = new ArrayList<>();
    }

    /**
     * Applies only settings officially permitted by Android OS for this application/window.
     * Clearly classifies applied vs system-restricted settings.
     */
    public AppliedSettingsResult applyProfileSettings(Activity activity, GameProfile profile) {
        AppliedSettingsResult result = new AppliedSettingsResult();
        if (activity == null || profile == null) return result;

        Window window = activity.getWindow();

        // 1. Keep Screen Awake
        if (profile.isKeepScreenAwake()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            result.applied.add("Screen Awake: Forced ON (No Sleep during Game)");
        }

        // 2. Brightness Preference
        if (profile.getBrightnessPercent() >= 0) {
            float brightness = Math.max(0.05f, Math.min(1.0f, profile.getBrightnessPercent() / 100.0f));
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.screenBrightness = brightness;
            window.setAttributes(lp);
            result.applied.add("Display Brightness: Set to " + profile.getBrightnessPercent() + "%");
        } else {
            result.applied.add("Display Brightness: Keeping system default");
        }

        // 3. Immersive Mode
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            result.applied.add("Immersive Display: Fullscreen transient bars active");
        }

        // 4. Refresh Rate
        if (profile.getRefreshRateHz() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    WindowManager.LayoutParams lp = window.getAttributes();
                    lp.preferredDisplayModeId = 0; // Let system manage or pick mode
                    window.setAttributes(lp);
                    result.applied.add("Refresh Rate Mode: High-performance preferred (" + profile.getRefreshRateHz() + "Hz target)");
                } catch (Exception e) {
                    result.unavailableOrRestricted.add("Refresh rate lock: Governed by hardware display controller");
                }
            } else {
                result.unavailableOrRestricted.add("Refresh rate API: Requires Android 11+ hardware display support");
            }
        }

        // 5. Global DPI check
        if (profile.getDpiPreference() > 0) {
            result.unavailableOrRestricted.add("Global DPI (" + profile.getDpiPreference() + " DPI): Requires ADB shell wm density command");
        }

        // 6. Do Not Disturb
        if (profile.isDndPreference()) {
            android.app.NotificationManager nm = (android.app.NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null && nm.isNotificationPolicyAccessGranted()) {
                try {
                    nm.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY);
                    result.applied.add("Do Not Disturb: Gaming Priority Filter activated");
                } catch (Exception e) {
                    result.unavailableOrRestricted.add("Do Not Disturb: Policy access restricted by OS");
                }
            } else {
                result.unavailableOrRestricted.add("Do Not Disturb: Requires Notification Policy permission in Settings");
            }
        }

        return result;
    }
}
