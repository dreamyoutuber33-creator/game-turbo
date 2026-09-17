package com.example;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class BoostFragment extends Fragment {

    private SwitchMaterial switchGamingMode;
    private SwitchMaterial switchKeepAwake;
    private SwitchMaterial switchImmersive;
    private TextView tvMemoryStatus;
    private MaterialButton btnCleanCache;

    private MaterialButton btnDndShortcut;
    private MaterialButton btnBatteryShortcut;
    private MaterialButton btnDisplayShortcut;
    private MaterialButton btnThermalCheck;

    private DeviceMonitor deviceMonitor;
    private SettingsHelper settingsHelper;
    private SharedPreferences prefs;

    private static final String PREF_BOOST = "game_turbo_boost_prefs";
    private static final String KEY_GAMING_MODE = "pref_gaming_mode";
    private static final String KEY_KEEP_AWAKE = "pref_keep_awake";
    private static final String KEY_IMMERSIVE = "pref_immersive";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_tab_boost, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deviceMonitor = new DeviceMonitor(requireContext());
        settingsHelper = new SettingsHelper(requireContext());
        prefs = requireContext().getSharedPreferences(PREF_BOOST, Context.MODE_PRIVATE);

        initViews(view);
        loadSavedStates();
        setupListeners();
        updateMemoryStatus();
    }

    private void initViews(View view) {
        switchGamingMode = view.findViewById(R.id.switch_master_gaming_mode);
        switchKeepAwake = view.findViewById(R.id.switch_keep_screen_awake);
        switchImmersive = view.findViewById(R.id.switch_immersive_mode);
        tvMemoryStatus = view.findViewById(R.id.tv_boost_memory_status);
        btnCleanCache = view.findViewById(R.id.btn_trigger_memory_trim);

        btnDndShortcut = view.findViewById(R.id.btn_boost_dnd_shortcut);
        btnBatteryShortcut = view.findViewById(R.id.btn_boost_battery_shortcut);
        btnDisplayShortcut = view.findViewById(R.id.btn_boost_display_shortcut);
        btnThermalCheck = view.findViewById(R.id.btn_boost_thermal_check);
    }

    private void loadSavedStates() {
        boolean gamingMode = prefs.getBoolean(KEY_GAMING_MODE, true);
        boolean keepAwake = prefs.getBoolean(KEY_KEEP_AWAKE, true);
        boolean immersive = prefs.getBoolean(KEY_IMMERSIVE, false);

        switchGamingMode.setChecked(gamingMode);
        switchKeepAwake.setChecked(keepAwake);
        switchImmersive.setChecked(immersive);

        applyKeepAwake(keepAwake);
        applyImmersiveMode(immersive);
    }

    private void setupListeners() {
        switchGamingMode.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_GAMING_MODE, isChecked).apply();
            if (isChecked) {
                switchKeepAwake.setChecked(true);
                Toast.makeText(requireContext(), "Master Gaming Mode Active", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Standard Mode Restored", Toast.LENGTH_SHORT).show();
            }
        });

        switchKeepAwake.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_KEEP_AWAKE, isChecked).apply();
            applyKeepAwake(isChecked);
            Toast.makeText(requireContext(), isChecked ? "Screen Sleep Lock Enabled" : "Screen Timeout Restored", Toast.LENGTH_SHORT).show();
        });

        switchImmersive.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_IMMERSIVE, isChecked).apply();
            applyImmersiveMode(isChecked);
            Toast.makeText(requireContext(), isChecked ? "Immersive Fullscreen Enabled" : "System Bars Restored", Toast.LENGTH_SHORT).show();
        });

        btnCleanCache.setOnClickListener(v -> {
            long freed = deviceMonitor.trimMemoryCaches();
            updateMemoryStatus();
            String msg = freed > 0 ? "Optimized memory: " + freed + " MB reclaimed" : "Application caches trimmed successfully";
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        btnDndShortcut.setOnClickListener(v -> settingsHelper.openDoNotDisturbSettings());
        btnBatteryShortcut.setOnClickListener(v -> settingsHelper.openBatterySettings());
        btnDisplayShortcut.setOnClickListener(v -> settingsHelper.openDisplaySettings());
        btnThermalCheck.setOnClickListener(v -> showThermalDialog());
    }

    private void applyKeepAwake(boolean keepAwake) {
        if (getActivity() == null) return;
        if (keepAwake) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void applyImmersiveMode(boolean immersive) {
        if (getActivity() == null) return;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getActivity().getWindow(), getActivity().getWindow().getDecorView());
        if (controller != null) {
            if (immersive) {
                controller.hide(WindowInsetsCompat.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars());
            }
        }
    }

    private void updateMemoryStatus() {
        DeviceMonitor.MemoryInfo mem = deviceMonitor.getMemoryInfo();
        tvMemoryStatus.setText("Available RAM: " + mem.availableRamFormatted + " (Total: " + mem.totalRamFormatted + ")");
    }

    private void showThermalDialog() {
        DeviceMonitor.BatteryInfo bat = deviceMonitor.getBatteryInfo();
        String thermalLevel = "Normal (Safe)";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PowerManager pm = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                int status = pm.getCurrentThermalStatus();
                switch (status) {
                    case PowerManager.THERMAL_STATUS_NONE:
                        thermalLevel = "Cool (None)";
                        break;
                    case PowerManager.THERMAL_STATUS_LIGHT:
                        thermalLevel = "Light Throttling Safe";
                        break;
                    case PowerManager.THERMAL_STATUS_MODERATE:
                        thermalLevel = "Moderate Warmth";
                        break;
                    case PowerManager.THERMAL_STATUS_SEVERE:
                        thermalLevel = "High Thermal (Cooling Recommended)";
                        break;
                    case PowerManager.THERMAL_STATUS_CRITICAL:
                        thermalLevel = "Critical Temperature";
                        break;
                }
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.Theme_GameTurboPro)
                .setTitle("Hardware Thermal Diagnostics")
                .setMessage("Current Battery Temperature: " + bat.temperatureFormatted + "\n" +
                        "OS Thermal Governor State: " + thermalLevel + "\n" +
                        "Battery Voltage: " + bat.voltageFormatted + "\n\n" +
                        "Tip: For sustained 90/120 FPS gaming without thermal throttling, avoid charging while playing intensive games.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMemoryStatus();
    }
}
