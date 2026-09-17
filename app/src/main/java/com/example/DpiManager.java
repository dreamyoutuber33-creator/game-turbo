package com.example;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * DpiManager provides legitimate DPI inspection, preset calculation,
 * and standard ADB configuration instructions.
 * Complies strictly with Android security boundaries without hidden APIs or exploits.
 */
public class DpiManager {

    private static final String PREF_NAME = "game_turbo_dpi";
    private static final String KEY_ORIGINAL_DPI = "original_native_dpi";
    private static final String KEY_CUSTOM_TARGET_DPI = "custom_target_dpi";

    public static class DpiPreset {
        public final String name;
        public final int dpi;
        public final String description;

        public DpiPreset(String name, int dpi, String description) {
            this.name = name;
            this.dpi = dpi;
            this.description = description;
        }
    }

    private final Context context;
    private final SharedPreferences prefs;

    public DpiManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Store original device DPI if not already recorded
        if (!prefs.contains(KEY_ORIGINAL_DPI)) {
            int currentDpi = getCurrentDpi();
            prefs.edit().putInt(KEY_ORIGINAL_DPI, currentDpi).apply();
        }
    }

    public int getCurrentDpi() {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.densityDpi;
    }

    public int getOriginalDpi() {
        return prefs.getInt(KEY_ORIGINAL_DPI, getCurrentDpi());
    }

    public int getTargetDpi() {
        return prefs.getInt(KEY_CUSTOM_TARGET_DPI, getCurrentDpi());
    }

    public void setTargetDpi(int dpi) {
        prefs.edit().putInt(KEY_CUSTOM_TARGET_DPI, dpi).apply();
    }

    public List<DpiPreset> getPresets() {
        List<DpiPreset> presets = new ArrayList<>();
        int current = getCurrentDpi();

        presets.add(new DpiPreset("Native Default", getOriginalDpi(), "Factory calibrated screen scale"));
        presets.add(new DpiPreset("Ultra Wide FOV", 360, "Expanded battlefield view (Small UI)"));
        presets.add(new DpiPreset("Balanced Gaming", 411, "Standard Google Pixel baseline scale"));
        presets.add(new DpiPreset("High Precision", 440, "Sharp HUD elements and responsive touch"));
        presets.add(new DpiPreset("Comfort / Large", 480, "Larger buttons and high legibility"));
        return presets;
    }

    public String getAdbCommand(int targetDpi) {
        return "adb shell wm density " + targetDpi;
    }

    public String getAdbResetCommand() {
        return "adb shell wm density reset";
    }

    public void copyToClipboard(String text, String label) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData clip = ClipData.newPlainText(label, text);
            cm.setPrimaryClip(clip);
        }
    }

    public String getDpiExplanation() {
        return "Android Security Protocol:\n" +
                "Modifying global system density (DPI) permanently requires WRITE_SECURE_SETTINGS or official ADB authorized commands.\n\n" +
                "To apply system-wide without root:\n" +
                "1. Enable Developer Options on your phone.\n" +
                "2. Turn on USB Debugging.\n" +
                "3. Connect to PC and run the copied command:\n" +
                "   adb shell wm density [value]\n\n" +
                "Alternatively, adjust 'Smallest width' directly in Developer Options.";
    }
}
