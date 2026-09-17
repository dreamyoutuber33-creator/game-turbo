package com.example;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class SettingsFragment extends Fragment {

    private ThemeAccentManager accentManager;
    private SettingsHelper settingsHelper;

    private ImageView ivAccentPreview;
    private TextView tvCurrentAccent;
    private MaterialButton btnChangeAccent;

    private View rowDisplay;
    private View rowRefreshRate;
    private View rowBattery;
    private View rowBatteryOpt;
    private View rowDnd;
    private View rowDeveloperOptions;
    private View rowGameDashboard;
    private View rowAppInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_tab_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        accentManager = new ThemeAccentManager(requireContext());
        settingsHelper = new SettingsHelper(requireContext());

        initViews(view);
        updateAccentDisplay();
        setupShortcuts();
    }

    private void initViews(View view) {
        ivAccentPreview = view.findViewById(R.id.iv_settings_accent_preview);
        tvCurrentAccent = view.findViewById(R.id.tv_settings_current_accent);
        btnChangeAccent = view.findViewById(R.id.btn_settings_change_accent);

        rowDisplay = view.findViewById(R.id.row_shortcut_display);
        rowRefreshRate = view.findViewById(R.id.row_shortcut_refresh_rate);
        rowBattery = view.findViewById(R.id.row_shortcut_battery);
        rowBatteryOpt = view.findViewById(R.id.row_shortcut_battery_opt);
        rowDnd = view.findViewById(R.id.row_shortcut_dnd);
        rowDeveloperOptions = view.findViewById(R.id.row_shortcut_developer_options);
        rowGameDashboard = view.findViewById(R.id.row_shortcut_game_dashboard);
        rowAppInfo = view.findViewById(R.id.row_shortcut_app_info);

        btnChangeAccent.setOnClickListener(v -> showAccentPickerDialog());
    }

    private void updateAccentDisplay() {
        tvCurrentAccent.setText(accentManager.getAccentName());
        ivAccentPreview.setColorFilter(accentManager.getAccentColor());
    }

    private void setupShortcuts() {
        rowDisplay.setOnClickListener(v -> settingsHelper.openDisplaySettings());
        rowRefreshRate.setOnClickListener(v -> settingsHelper.openRefreshRateSettings());
        rowBattery.setOnClickListener(v -> settingsHelper.openBatterySettings());
        rowBatteryOpt.setOnClickListener(v -> settingsHelper.openBatteryOptimizationSettings());
        rowDnd.setOnClickListener(v -> settingsHelper.openDoNotDisturbSettings());
        rowDeveloperOptions.setOnClickListener(v -> settingsHelper.openDeveloperOptions());
        rowGameDashboard.setOnClickListener(v -> settingsHelper.openGameDashboard());
        rowAppInfo.setOnClickListener(v -> settingsHelper.openAppDetails(null));
    }

    private void showAccentPickerDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_accent_picker);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        RadioGroup rg = dialog.findViewById(R.id.rg_accent_theme);
        RadioButton rbCyan = dialog.findViewById(R.id.rb_accent_cyan);
        RadioButton rbLime = dialog.findViewById(R.id.rb_accent_lime);
        RadioButton rbPurple = dialog.findViewById(R.id.rb_accent_purple);
        RadioButton rbOrange = dialog.findViewById(R.id.rb_accent_orange);
        RadioButton rbRed = dialog.findViewById(R.id.rb_accent_red);
        MaterialButton btnApply = dialog.findViewById(R.id.btn_save_accent);

        String current = accentManager.getAccent();
        if (ThemeAccentManager.ACCENT_LIME.equals(current)) rbLime.setChecked(true);
        else if (ThemeAccentManager.ACCENT_PURPLE.equals(current)) rbPurple.setChecked(true);
        else if (ThemeAccentManager.ACCENT_ORANGE.equals(current)) rbOrange.setChecked(true);
        else if (ThemeAccentManager.ACCENT_RED.equals(current)) rbRed.setChecked(true);
        else rbCyan.setChecked(true);

        btnApply.setOnClickListener(v -> {
            String selected = ThemeAccentManager.ACCENT_CYAN;
            int id = rg.getCheckedRadioButtonId();
            if (id == R.id.rb_accent_lime) selected = ThemeAccentManager.ACCENT_LIME;
            else if (id == R.id.rb_accent_purple) selected = ThemeAccentManager.ACCENT_PURPLE;
            else if (id == R.id.rb_accent_orange) selected = ThemeAccentManager.ACCENT_ORANGE;
            else if (id == R.id.rb_accent_red) selected = ThemeAccentManager.ACCENT_RED;

            accentManager.setAccent(selected);
            dialog.dismiss();
            updateAccentDisplay();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).applyThemeAccent();
            }
            Toast.makeText(requireContext(), "Accent updated: " + accentManager.getAccentName(), Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}
