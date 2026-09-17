package com.example;

import android.graphics.drawable.Drawable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * GameItem wraps an installed application for presentation in the Game Launcher.
 */
public class GameItem {
    private final String packageName;
    private String title;
    private Drawable icon;
    private boolean isInstalled = true;
    private GameProfile profile;

    public GameItem(String packageName, String title, Drawable icon) {
        this.packageName = packageName;
        this.title = title;
        this.icon = icon;
        this.profile = new GameProfile(packageName, title);
    }

    public String getPackageName() { return packageName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Drawable getIcon() { return icon; }
    public void setIcon(Drawable icon) { this.icon = icon; }
    public boolean isInstalled() { return isInstalled; }
    public void setInstalled(boolean installed) { isInstalled = installed; }

    public GameProfile getProfile() { return profile; }
    public void setProfile(GameProfile profile) { this.profile = profile; }

    public boolean isFavorite() {
        return profile != null && profile.isFavorite();
    }

    public long getLastPlayedTimestamp() {
        return profile != null ? profile.getLastPlayedTimestamp() : 0;
    }

    public String getLastPlayedFormatted() {
        long ts = getLastPlayedTimestamp();
        if (ts <= 0) return "Never played";
        long diff = System.currentTimeMillis() - ts;
        if (diff < 60 * 1000) return "Just now";
        if (diff < 60 * 60 * 1000) return (diff / (60 * 1000)) + "m ago";
        if (diff < 24 * 60 * 60 * 1000) return (diff / (3600 * 1000)) + "h ago";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
        return sdf.format(new Date(ts));
    }
}
