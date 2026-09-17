package com.example;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class HomeFragment extends Fragment implements DeviceMonitor.MetricsListener {

    private DeviceMonitor deviceMonitor;
    private NetworkMonitor networkMonitor;
    private GameProfileManager profileManager;
    private GameLauncher gameLauncher;
    private ThemeAccentManager accentManager;

    private TextView tvDeviceModel;
    private TextView tvAndroidVersion;
    private TextView tvResolution;
    private TextView tvRefreshRate;
    private TextView tvDpi;
    private TextView tvGamingModeTag;

    private View viewBoostGlowRing;
    private View btnBoostNow;
    private ImageView ivBoostBolt;
    private TextView tvBoostButtonText;
    private TextView tvBoostStatusSubtext;

    private TextView tvRamPercent;
    private ProgressBar pbRam;
    private TextView tvRamDetails;

    private TextView tvBatteryPercent;
    private TextView tvBatteryTemp;
    private TextView tvBatteryStatus;

    private TextView tvNetworkType;
    private TextView tvNetworkDetails;
    private TextView tvPingVal;
    private TextView tvPingQuality;
    private MaterialButton btnTestPing;

    private MaterialCardView cardSelectedGame;
    private ImageView ivGameIcon;
    private TextView tvGameTitle;
    private TextView tvGameProfileStatus;
    private MaterialButton btnPlaySelected;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isBoosting = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_tab_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deviceMonitor = new DeviceMonitor(requireContext());
        networkMonitor = new NetworkMonitor(requireContext());
        profileManager = new GameProfileManager(requireContext());
        gameLauncher = new GameLauncher(requireContext(), profileManager);
        accentManager = new ThemeAccentManager(requireContext());

        initViews(view);
        setupGlowAnimation();
        setupClickListeners();
        loadHardwareSpecs();
        loadNetworkInfo();
        refreshSelectedGame();
    }

    private void initViews(View view) {
        tvDeviceModel = view.findViewById(R.id.tv_home_device_model);
        tvAndroidVersion = view.findViewById(R.id.tv_home_android_version);
        tvResolution = view.findViewById(R.id.tv_home_resolution);
        tvRefreshRate = view.findViewById(R.id.tv_home_refresh_rate);
        tvDpi = view.findViewById(R.id.tv_home_dpi);
        tvGamingModeTag = view.findViewById(R.id.tv_home_gaming_mode_tag);

        viewBoostGlowRing = view.findViewById(R.id.view_boost_glow_ring);
        btnBoostNow = view.findViewById(R.id.btn_home_boost_now);
        ivBoostBolt = view.findViewById(R.id.iv_home_boost_bolt);
        tvBoostButtonText = view.findViewById(R.id.tv_boost_button_text);
        tvBoostStatusSubtext = view.findViewById(R.id.tv_home_boost_status_subtext);

        tvRamPercent = view.findViewById(R.id.tv_home_ram_percent);
        pbRam = view.findViewById(R.id.pb_home_ram);
        tvRamDetails = view.findViewById(R.id.tv_home_ram_details);

        tvBatteryPercent = view.findViewById(R.id.tv_home_battery_percent);
        tvBatteryTemp = view.findViewById(R.id.tv_home_battery_temp);
        tvBatteryStatus = view.findViewById(R.id.tv_home_battery_status);

        tvNetworkType = view.findViewById(R.id.tv_home_network_type);
        tvNetworkDetails = view.findViewById(R.id.tv_home_network_details);
        tvPingVal = view.findViewById(R.id.tv_home_ping_val);
        tvPingQuality = view.findViewById(R.id.tv_home_ping_quality);
        btnTestPing = view.findViewById(R.id.btn_home_test_ping);

        cardSelectedGame = view.findViewById(R.id.card_home_selected_game);
        ivGameIcon = view.findViewById(R.id.iv_home_game_icon);
        tvGameTitle = view.findViewById(R.id.tv_home_game_title);
        tvGameProfileStatus = view.findViewById(R.id.tv_home_game_profile_status);
        btnPlaySelected = view.findViewById(R.id.btn_home_play_selected);

        // Apply accent color
        int accentColor = accentManager.getAccentColor();
        btnPlaySelected.setBackgroundTintList(ColorStateList.valueOf(accentColor));
    }

    private void setupGlowAnimation() {
        try {
            Animation pulse = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_glow);
            viewBoostGlowRing.startAnimation(pulse);
        } catch (Exception ignored) {
        }
    }

    private void setupClickListeners() {
        btnBoostNow.setOnClickListener(v -> performBoostAction());

        btnTestPing.setOnClickListener(v -> runPingBenchmark());

        btnPlaySelected.setOnClickListener(v -> {
            String selectedPkg = profileManager.getSelectedGame();
            if (selectedPkg != null) {
                GameItem item = gameLauncher.createGameItem(selectedPkg);
                launchGameItem(item);
            } else {
                Toast.makeText(requireContext(), "Select a game from the GAMES tab first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHardwareSpecs() {
        DeviceMonitor.DeviceInfo specs = deviceMonitor.getDeviceSpecs();
        tvDeviceModel.setText(specs.manufacturer + " " + specs.model);
        tvAndroidVersion.setText("Android " + specs.androidVersion + " (API " + specs.apiLevel + ") • " + specs.hardware);
        tvResolution.setText(specs.screenResolution);
        tvRefreshRate.setText(specs.refreshRate);
        tvDpi.setText(specs.densityDpi);
    }

    private void loadNetworkInfo() {
        NetworkMonitor.NetworkDetails net = networkMonitor.getNetworkDetails();
        tvNetworkType.setText(net.connectionType);
        tvNetworkDetails.setText(net.detailedStatus);
    }

    private void runPingBenchmark() {
        btnTestPing.setEnabled(false);
        btnTestPing.setText("TESTING...");
        tvPingVal.setText("Pinging...");
        tvPingQuality.setText("Testing DNS socket latency...");

        networkMonitor.runPingTest(new NetworkMonitor.PingCallback() {
            @Override
            public void onPingProgress(int step, int totalSteps, long currentLatencyMs) {
                if (!isAdded()) return;
                tvPingVal.setText(currentLatencyMs + " ms");
                tvPingQuality.setText("Packet " + step + "/" + totalSteps);
            }

            @Override
            public void onPingComplete(NetworkMonitor.PingResult result) {
                if (!isAdded()) return;
                btnTestPing.setEnabled(true);
                btnTestPing.setText("TEST PING");

                if (result.isSuccess) {
                    tvPingVal.setText(result.avgLatencyMs + " ms");
                    tvPingQuality.setText(result.qualityRating + " (Jitter: " + result.jitterMs + "ms)");
                } else {
                    tvPingVal.setText("Timed Out");
                    tvPingQuality.setText("Host unreachable or offline");
                }
            }
        });
    }

    public void performBoostAction() {
        if (isBoosting) return;
        isBoosting = true;

        tvBoostButtonText.setText("OPTIMIZING...");
        tvBoostStatusSubtext.setText("Trimming process caches & applying Gaming Mode...");

        try {
            Animation rotate = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_infinite);
            ivBoostBolt.startAnimation(rotate);
        } catch (Exception ignored) {
        }

        // Measure memory before and trigger clean
        final DeviceMonitor.MemoryInfo memBefore = deviceMonitor.getMemoryInfo();

        handler.postDelayed(() -> {
            if (!isAdded()) return;

            // System cache clean and Garbage Collection
            long freedMb = deviceMonitor.trimMemoryCaches();
            final DeviceMonitor.MemoryInfo memAfter = deviceMonitor.getMemoryInfo();

            ivBoostBolt.clearAnimation();
            tvBoostButtonText.setText("BOOSTED!");
            tvBoostStatusSubtext.setText("Optimization Complete • Gaming profile active");

            // Show Boost Summary Dialog
            showBoostDialog(freedMb, memBefore, memAfter);

            handler.postDelayed(() -> {
                if (isAdded()) {
                    tvBoostButtonText.setText("BOOST NOW");
                    tvBoostStatusSubtext.setText("Tap to optimize memory cache & engage Gaming Mode");
                    isBoosting = false;
                }
            }, 3000);
        }, 1100);
    }

    private void showBoostDialog(long freedMb, DeviceMonitor.MemoryInfo before, DeviceMonitor.MemoryInfo after) {
        if (!isAdded()) return;
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_boost_summary);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvDetail = dialog.findViewById(R.id.tv_boost_summary_detail);
        TextView tvMem = dialog.findViewById(R.id.tv_boost_stat_memory);
        MaterialButton btnDismiss = dialog.findViewById(R.id.btn_dismiss_boost);

        tvDetail.setText("Application caches trimmed and device memory reclaimed through official Android Runtime GC. Display locked to max refresh rate.");
        tvMem.setText("Memory Reclaimed: " + (freedMb > 0 ? freedMb + " MB" : "Cache optimized") + " (Available: " + after.availableRamFormatted + ")");

        btnDismiss.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void refreshSelectedGame() {
        String selectedPkg = profileManager.getSelectedGame();
        List<GameItem> myGames = gameLauncher.getMyGames();

        if (selectedPkg == null && !myGames.isEmpty()) {
            selectedPkg = myGames.get(0).getPackageName();
            profileManager.setSelectedGame(selectedPkg);
        }

        if (selectedPkg != null) {
            GameItem item = gameLauncher.createGameItem(selectedPkg);
            tvGameTitle.setText(item.getTitle());
            ivGameIcon.setImageDrawable(item.getIcon());
            tvGameProfileStatus.setText("Profile: Awake ON • High Refresh Ready");
            btnPlaySelected.setEnabled(item.isInstalled());
            btnPlaySelected.setAlpha(item.isInstalled() ? 1.0f : 0.5f);
        } else {
            tvGameTitle.setText("No Game Selected");
            tvGameProfileStatus.setText("Tap GAMES tab to add installed games");
            btnPlaySelected.setEnabled(false);
            btnPlaySelected.setAlpha(0.5f);
        }
    }

    private void launchGameItem(GameItem item) {
        gameLauncher.launchGame(requireActivity(), item, new GameLauncher.LaunchCallback() {
            @Override
            public void onGameLaunched(GameItem item, GameProfileManager.AppliedSettingsResult settingsResult) {
                Toast.makeText(requireContext(), "Launching " + item.getTitle() + " with Game Turbo Pro", Toast.LENGTH_SHORT).show();
                refreshSelectedGame();
            }

            @Override
            public void onGameNotInstalled(String packageName) {
                Toast.makeText(requireContext(), "This game is not installed on this device", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLaunchFailed(String reason) {
                Toast.makeText(requireContext(), reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        deviceMonitor.startMonitoring(this);
        refreshSelectedGame();
    }

    @Override
    public void onPause() {
        super.onPause();
        deviceMonitor.stopMonitoring();
    }

    @Override
    public void onBatteryMetricsUpdated(DeviceMonitor.BatteryInfo info) {
        if (!isAdded()) return;
        tvBatteryPercent.setText(info.levelPercent + "%");
        tvBatteryTemp.setText(info.temperatureFormatted);
        tvBatteryStatus.setText(info.status + " (" + info.voltageFormatted + ")");
    }

    @Override
    public void onMemoryMetricsUpdated(DeviceMonitor.MemoryInfo info) {
        if (!isAdded()) return;
        tvRamPercent.setText(info.usedPercentage + "%");
        pbRam.setProgress(info.usedPercentage);
        tvRamDetails.setText("Used: " + info.usedRamFormatted + " / " + info.totalRamFormatted);
    }
}
