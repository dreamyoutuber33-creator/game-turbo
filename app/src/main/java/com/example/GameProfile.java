package com.example;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * GameProfile encapsulates per-game settings preferences, sensitivity calibration,
 * and graphics targets.
 */
public class GameProfile {
    private String packageName;
    private String gameTitle;
    private int dpiPreference = 0; // 0 = system default
    private int brightnessPercent = -1; // -1 = keep current, 0-100%
    private int screenTimeoutSeconds = -1; // -1 = keep current, 60, 300, 600, 0 = keep awake
    private int refreshRateHz = -1; // -1 = auto/default, 60, 90, 120, 144
    private boolean gamingModeAutoBoost = true;
    private boolean dndPreference = false;
    private boolean keepScreenAwake = true;
    private String sensitivityNotes = "";
    private String graphicsFpsNotes = "";
    private boolean isFavorite = false;
    private long lastPlayedTimestamp = 0;

    public GameProfile(String packageName, String gameTitle) {
        this.packageName = packageName;
        this.gameTitle = gameTitle;
    }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }

    public int getDpiPreference() { return dpiPreference; }
    public void setDpiPreference(int dpi) { this.dpiPreference = dpi; }

    public int getBrightnessPercent() { return brightnessPercent; }
    public void setBrightnessPercent(int brightness) { this.brightnessPercent = brightness; }

    public int getScreenTimeoutSeconds() { return screenTimeoutSeconds; }
    public void setScreenTimeoutSeconds(int timeout) { this.screenTimeoutSeconds = timeout; }

    public int getRefreshRateHz() { return refreshRateHz; }
    public void setRefreshRateHz(int rate) { this.refreshRateHz = rate; }

    public boolean isGamingModeAutoBoost() { return gamingModeAutoBoost; }
    public void setGamingModeAutoBoost(boolean autoBoost) { this.gamingModeAutoBoost = autoBoost; }

    public boolean isDndPreference() { return dndPreference; }
    public void setDndPreference(boolean dnd) { this.dndPreference = dnd; }

    public boolean isKeepScreenAwake() { return keepScreenAwake; }
    public void setKeepScreenAwake(boolean keepAwake) { this.keepScreenAwake = keepAwake; }

    public String getSensitivityNotes() { return sensitivityNotes; }
    public void setSensitivityNotes(String notes) { this.sensitivityNotes = notes; }

    public String getGraphicsFpsNotes() { return graphicsFpsNotes; }
    public void setGraphicsFpsNotes(String notes) { this.graphicsFpsNotes = notes; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public long getLastPlayedTimestamp() { return lastPlayedTimestamp; }
    public void setLastPlayedTimestamp(long timestamp) { this.lastPlayedTimestamp = timestamp; }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("packageName", packageName);
            obj.put("gameTitle", gameTitle);
            obj.put("dpiPreference", dpiPreference);
            obj.put("brightnessPercent", brightnessPercent);
            obj.put("screenTimeoutSeconds", screenTimeoutSeconds);
            obj.put("refreshRateHz", refreshRateHz);
            obj.put("gamingModeAutoBoost", gamingModeAutoBoost);
            obj.put("dndPreference", dndPreference);
            obj.put("keepScreenAwake", keepScreenAwake);
            obj.put("sensitivityNotes", sensitivityNotes);
            obj.put("graphicsFpsNotes", graphicsFpsNotes);
            obj.put("isFavorite", isFavorite);
            obj.put("lastPlayedTimestamp", lastPlayedTimestamp);
        } catch (JSONException ignored) {
        }
        return obj;
    }

    public static GameProfile fromJson(JSONObject obj) {
        if (obj == null) return null;
        String pkg = obj.optString("packageName", "");
        String title = obj.optString("gameTitle", "");
        GameProfile profile = new GameProfile(pkg, title);
        profile.setDpiPreference(obj.optInt("dpiPreference", 0));
        profile.setBrightnessPercent(obj.optInt("brightnessPercent", -1));
        profile.setScreenTimeoutSeconds(obj.optInt("screenTimeoutSeconds", -1));
        profile.setRefreshRateHz(obj.optInt("refreshRateHz", -1));
        profile.setGamingModeAutoBoost(obj.optBoolean("gamingModeAutoBoost", true));
        profile.setDndPreference(obj.optBoolean("dndPreference", false));
        profile.setKeepScreenAwake(obj.optBoolean("keepScreenAwake", true));
        profile.setSensitivityNotes(obj.optString("sensitivityNotes", ""));
        profile.setGraphicsFpsNotes(obj.optString("graphicsFpsNotes", ""));
        profile.setFavorite(obj.optBoolean("isFavorite", false));
        profile.setLastPlayedTimestamp(obj.optLong("lastPlayedTimestamp", 0));
        return profile;
    }
}
