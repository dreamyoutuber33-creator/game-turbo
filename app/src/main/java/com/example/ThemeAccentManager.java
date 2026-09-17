package com.example;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.core.content.ContextCompat;

/**
 * ThemeAccentManager handles user-customizable neon accent palettes.
 */
public class ThemeAccentManager {

    private static final String PREF_NAME = "game_turbo_theme";
    private static final String KEY_ACCENT = "selected_accent";

    public static final String ACCENT_CYAN = "cyan";
    public static final String ACCENT_LIME = "lime";
    public static final String ACCENT_PURPLE = "purple";
    public static final String ACCENT_ORANGE = "orange";
    public static final String ACCENT_RED = "red";

    private final Context context;
    private final SharedPreferences prefs;

    public ThemeAccentManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getAccent() {
        return prefs.getString(KEY_ACCENT, ACCENT_CYAN);
    }

    public void setAccent(String accentKey) {
        prefs.edit().putString(KEY_ACCENT, accentKey).apply();
    }

    public int getAccentColorRes() {
        String accent = getAccent();
        switch (accent) {
            case ACCENT_LIME:
                return R.color.accent_lime;
            case ACCENT_PURPLE:
                return R.color.accent_purple;
            case ACCENT_ORANGE:
                return R.color.accent_orange;
            case ACCENT_RED:
                return R.color.accent_red;
            case ACCENT_CYAN:
            default:
                return R.color.accent_cyan;
        }
    }

    public int getAccentColor() {
        return ContextCompat.getColor(context, getAccentColorRes());
    }

    public String getAccentName() {
        String accent = getAccent();
        switch (accent) {
            case ACCENT_LIME:
                return "Toxic Lime";
            case ACCENT_PURPLE:
                return "Ultra Violet";
            case ACCENT_ORANGE:
                return "Solar Blaze";
            case ACCENT_RED:
                return "Crimson Fury";
            case ACCENT_CYAN:
            default:
                return "Electric Cyan";
        }
    }
}
