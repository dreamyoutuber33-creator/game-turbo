package com.example;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GameLauncher handles discovering installed applications, filtering games,
 * managing the user's game library, and launching packages through official Android APIs.
 */
public class GameLauncher {

    public interface LaunchCallback {
        void onGameLaunched(GameItem item, GameProfileManager.AppliedSettingsResult settingsResult);
        void onGameNotInstalled(String packageName);
        void onLaunchFailed(String reason);
    }

    private final Context context;
    private final PackageManager packageManager;
    private final GameProfileManager profileManager;

    public GameLauncher(Context context, GameProfileManager profileManager) {
        this.context = context.getApplicationContext();
        this.packageManager = this.context.getPackageManager();
        this.profileManager = profileManager;
    }

    /**
     * Retrieves all games currently added to the user's "My Games" dashboard.
     */
    public List<GameItem> getMyGames() {
        Set<String> trackedPackages = profileManager.getTrackedGamePackages();
        List<GameItem> items = new ArrayList<>();

        // If tracked list is empty on first run, auto-seed with detected installed games or launchable apps
        if (trackedPackages.isEmpty()) {
            autoDiscoverAndSeedGames();
            trackedPackages = profileManager.getTrackedGamePackages();
        }

        for (String pkg : trackedPackages) {
            GameItem item = createGameItem(pkg);
            items.add(item);
        }

        // Sort: Favorites first, then last played descending, then title alphabetical
        Collections.sort(items, (a, b) -> {
            if (a.isFavorite() != b.isFavorite()) {
                return a.isFavorite() ? -1 : 1;
            }
            if (a.getLastPlayedTimestamp() != b.getLastPlayedTimestamp()) {
                return Long.compare(b.getLastPlayedTimestamp(), a.getLastPlayedTimestamp());
            }
            return a.getTitle().compareToIgnoreCase(b.getTitle());
        });

        return items;
    }

    /**
     * Retrieves the most recently played games for the "LAST PLAYED" section.
     */
    public List<GameItem> getLastPlayedGames(int maxCount) {
        List<GameItem> all = getMyGames();
        List<GameItem> played = new ArrayList<>();
        for (GameItem item : all) {
            if (item.getLastPlayedTimestamp() > 0) {
                played.add(item);
            }
        }
        Collections.sort(played, (a, b) -> Long.compare(b.getLastPlayedTimestamp(), a.getLastPlayedTimestamp()));
        if (played.size() > maxCount) {
            return played.subList(0, maxCount);
        }
        return played;
    }

    /**
     * Discovers all launchable applications installed on the system to allow
     * user manual selection.
     */
    public List<GameItem> getInstalledLaunchableApps() {
        List<GameItem> apps = new ArrayList<>();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(mainIntent, 0);
        Set<String> processedPackages = new HashSet<>();

        String myPackage = context.getPackageName();

        for (ResolveInfo ri : resolveInfos) {
            String pkg = ri.activityInfo.packageName;
            if (myPackage.equals(pkg) || processedPackages.contains(pkg)) {
                continue;
            }
            processedPackages.add(pkg);

            String label = ri.loadLabel(packageManager).toString();
            Drawable icon = ri.loadIcon(packageManager);

            GameItem item = new GameItem(pkg, label, icon);
            item.setProfile(profileManager.getProfile(pkg, label));
            apps.add(item);
        }

        Collections.sort(apps, Comparator.comparing(a -> a.getTitle().toLowerCase()));
        return apps;
    }

    /**
     * Automatically identifies games and popular gaming applications on first launch.
     */
    private void autoDiscoverAndSeedGames() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(mainIntent, 0);
        String myPackage = context.getPackageName();

        int addedCount = 0;
        for (ResolveInfo ri : resolveInfos) {
            String pkg = ri.activityInfo.packageName;
            if (myPackage.equals(pkg)) continue;

            boolean isGame = false;
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(pkg, 0);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    if (appInfo.category == ApplicationInfo.CATEGORY_GAME) {
                        isGame = true;
                    }
                }
                if ((appInfo.flags & ApplicationInfo.FLAG_IS_GAME) != 0) {
                    isGame = true;
                }
            } catch (Exception ignored) {
            }

            if (isGame) {
                profileManager.addTrackedGamePackage(pkg);
                addedCount++;
            }
        }

        // If no categorized games found, add a few top user apps so the library is ready
        if (addedCount == 0) {
            for (ResolveInfo ri : resolveInfos) {
                String pkg = ri.activityInfo.packageName;
                if (myPackage.equals(pkg)) continue;
                if (!pkg.startsWith("com.android.") && !pkg.startsWith("com.google.android.inputmethod")) {
                    profileManager.addTrackedGamePackage(pkg);
                    addedCount++;
                    if (addedCount >= 4) break;
                }
            }
        }
    }

    public boolean isAppInstalled(String packageName) {
        if (packageName == null || packageName.isEmpty()) return false;
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public GameItem createGameItem(String packageName) {
        String title = packageName;
        Drawable icon = context.getDrawable(R.drawable.ic_gamepad);
        boolean installed = isAppInstalled(packageName);

        if (installed) {
            try {
                ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
                title = packageManager.getApplicationLabel(ai).toString();
                icon = packageManager.getApplicationIcon(packageName);
            } catch (Exception ignored) {
            }
        } else {
            title = packageName + " (Not Installed)";
        }

        GameItem item = new GameItem(packageName, title, icon);
        item.setInstalled(installed);
        item.setProfile(profileManager.getProfile(packageName, title));
        return item;
    }

    /**
     * Executes official Android package launch Intent.
     */
    public void launchGame(android.app.Activity activity, GameItem item, LaunchCallback callback) {
        if (item == null) {
            if (callback != null) callback.onLaunchFailed("Invalid game selection");
            return;
        }

        if (!isAppInstalled(item.getPackageName())) {
            item.setInstalled(false);
            if (callback != null) {
                callback.onGameNotInstalled(item.getPackageName());
            }
            return;
        }

        try {
            Intent launchIntent = packageManager.getLaunchIntentForPackage(item.getPackageName());
            if (launchIntent == null) {
                if (callback != null) {
                    callback.onLaunchFailed("No launchable activity found for " + item.getTitle());
                }
                return;
            }

            // Apply profile
            GameProfile profile = item.getProfile();
            GameProfileManager.AppliedSettingsResult appliedSettings = profileManager.applyProfileSettings(activity, profile);

            // Update timestamp
            profileManager.updateLastPlayed(item.getPackageName(), item.getTitle());

            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(launchIntent);

            if (callback != null) {
                callback.onGameLaunched(item, appliedSettings);
            }

        } catch (Exception e) {
            if (callback != null) {
                callback.onLaunchFailed("Error starting game: " + e.getMessage());
            }
        }
    }

    public void addGameToLibrary(String packageName) {
        profileManager.addTrackedGamePackage(packageName);
    }

    public void removeGameFromLibrary(String packageName) {
        profileManager.removeTrackedGamePackage(packageName);
    }
}
