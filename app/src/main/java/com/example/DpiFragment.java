package com.example;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class DpiFragment extends Fragment {

    private DpiManager dpiManager;
    private SettingsHelper settingsHelper;

    private TextView tvCurrentDpi;
    private TextView tvDpiCategory;
    private TextView tvOriginalDpi;
    private EditText etTargetDpi;
    private MaterialButton btnMinus;
    private MaterialButton btnPlus;
    private MaterialButton btnApply;
    private MaterialButton btnRestore;
    private LinearLayout layoutPresetsContainer;
    private TextView tvDpiExplanation;
    private TextView tvAdbCommandPreview;
    private ImageView btnCopyAdbCmd;
    private MaterialButton btnOpenDevOptions;
    private MaterialButton btnOpenDisplaySize;

    private int currentTargetDpi = 420;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_tab_dpi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dpiManager = new DpiManager(requireContext());
        settingsHelper = new SettingsHelper(requireContext());

        initViews(view);
        loadCurrentDpi();
        setupPresets();
        setupListeners();
    }

    private void initViews(View view) {
        tvCurrentDpi = view.findViewById(R.id.tv_dpi_current_val);
        tvDpiCategory = view.findViewById(R.id.tv_dpi_category);
        tvOriginalDpi = view.findViewById(R.id.tv_dpi_original_val);
        etTargetDpi = view.findViewById(R.id.et_target_dpi);
        btnMinus = view.findViewById(R.id.btn_dpi_minus);
        btnPlus = view.findViewById(R.id.btn_dpi_plus);
        btnApply = view.findViewById(R.id.btn_dpi_apply);
        btnRestore = view.findViewById(R.id.btn_dpi_restore);
        layoutPresetsContainer = view.findViewById(R.id.layout_dpi_presets_container);
        tvDpiExplanation = view.findViewById(R.id.tv_dpi_explanation);
        tvAdbCommandPreview = view.findViewById(R.id.tv_adb_command_preview);
        btnCopyAdbCmd = view.findViewById(R.id.btn_copy_adb_cmd);
        btnOpenDevOptions = view.findViewById(R.id.btn_open_developer_options);
        btnOpenDisplaySize = view.findViewById(R.id.btn_open_display_size);

        tvDpiExplanation.setText(dpiManager.getDpiExplanation());
    }

    private void loadCurrentDpi() {
        int currentDpi = dpiManager.getCurrentDpi();
        int originalDpi = dpiManager.getOriginalDpi();
        currentTargetDpi = dpiManager.getTargetDpi();

        tvCurrentDpi.setText(String.valueOf(currentDpi));
        tvOriginalDpi.setText(String.valueOf(originalDpi));
        etTargetDpi.setText(String.valueOf(currentTargetDpi));

        tvDpiCategory.setText(getDensityBucketName(currentDpi));
        updateAdbCommandPreview();
    }

    private String getDensityBucketName(int dpi) {
        if (dpi <= 120) return "ldpi (Low Density)";
        if (dpi <= 160) return "mdpi (Standard Density)";
        if (dpi <= 240) return "hdpi (High Density)";
        if (dpi <= 320) return "xhdpi (Extra High)";
        if (dpi <= 480) return "xxhdpi (Ultra High)";
        if (dpi <= 640) return "xxxhdpi (Max High)";
        return "Custom Screen Density";
    }

    private void setupPresets() {
        layoutPresetsContainer.removeAllViews();
        List<DpiManager.DpiPreset> presets = dpiManager.getPresets();

        for (DpiManager.DpiPreset preset : presets) {
            MaterialCardView card = new MaterialCardView(requireContext());
            card.setCardBackgroundColor(requireContext().getColor(R.color.bg_card));
            card.setRadius(24f);
            card.setStrokeColor(requireContext().getColor(R.color.border_subtle));
            card.setStrokeWidth(2);
            card.setElevation(2f);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 16;
            card.setLayoutParams(lp);

            LinearLayout inner = new LinearLayout(requireContext());
            inner.setOrientation(LinearLayout.HORIZONTAL);
            inner.setPadding(32, 28, 32, 28);
            inner.setBackgroundResource(android.R.drawable.list_selector_background);

            LinearLayout textCol = new LinearLayout(requireContext());
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            textCol.setLayoutParams(textLp);

            TextView tvName = new TextView(requireContext());
            tvName.setText(preset.name + " (" + preset.dpi + " DPI)");
            tvName.setTextColor(requireContext().getColor(R.color.text_primary));
            tvName.setTextSize(14);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvDesc = new TextView(requireContext());
            tvDesc.setText(preset.description);
            tvDesc.setTextColor(requireContext().getColor(R.color.text_secondary));
            tvDesc.setTextSize(11);
            tvDesc.setPadding(0, 4, 0, 0);

            textCol.addView(tvName);
            textCol.addView(tvDesc);

            MaterialButton btnUse = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.borderlessButtonStyle);
            btnUse.setText("SELECT");
            btnUse.setTextColor(requireContext().getColor(R.color.accent_cyan));
            btnUse.setTextSize(11);
            btnUse.setOnClickListener(v -> {
                currentTargetDpi = preset.dpi;
                etTargetDpi.setText(String.valueOf(currentTargetDpi));
                updateAdbCommandPreview();
            });

            inner.addView(textCol);
            inner.addView(btnUse);
            card.addView(inner);
            layoutPresetsContainer.addView(card);
        }
    }

    private void setupListeners() {
        btnMinus.setOnClickListener(v -> {
            if (currentTargetDpi > 120) {
                currentTargetDpi -= 10;
                etTargetDpi.setText(String.valueOf(currentTargetDpi));
                updateAdbCommandPreview();
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (currentTargetDpi < 800) {
                currentTargetDpi += 10;
                etTargetDpi.setText(String.valueOf(currentTargetDpi));
                updateAdbCommandPreview();
            }
        });

        etTargetDpi.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int val = Integer.parseInt(s.toString());
                    if (val >= 100 && val <= 1000) {
                        currentTargetDpi = val;
                        dpiManager.setTargetDpi(val);
                        updateAdbCommandPreview();
                    }
                } catch (Exception ignored) {}
            }
        });

        btnApply.setOnClickListener(v -> {
            dpiManager.setTargetDpi(currentTargetDpi);
            String cmd = dpiManager.getAdbCommand(currentTargetDpi);
            dpiManager.copyToClipboard(cmd, "ADB Command");
            Toast.makeText(requireContext(), "Target DPI set to " + currentTargetDpi + ". ADB command copied to clipboard!", Toast.LENGTH_LONG).show();
        });

        btnRestore.setOnClickListener(v -> {
            int orig = dpiManager.getOriginalDpi();
            currentTargetDpi = orig;
            etTargetDpi.setText(String.valueOf(orig));
            dpiManager.setTargetDpi(orig);
            String resetCmd = dpiManager.getAdbResetCommand();
            dpiManager.copyToClipboard(resetCmd, "ADB Reset Command");
            Toast.makeText(requireContext(), "Default DPI (" + orig + ") selected. Reset command copied!", Toast.LENGTH_LONG).show();
            updateAdbCommandPreview();
        });

        btnCopyAdbCmd.setOnClickListener(v -> {
            String cmd = dpiManager.getAdbCommand(currentTargetDpi);
            dpiManager.copyToClipboard(cmd, "ADB Command");
            Toast.makeText(requireContext(), "Copied: " + cmd, Toast.LENGTH_SHORT).show();
        });

        btnOpenDevOptions.setOnClickListener(v -> settingsHelper.openDeveloperOptions());
        btnOpenDisplaySize.setOnClickListener(v -> settingsHelper.openDisplaySettings());
    }

    private void updateAdbCommandPreview() {
        if (tvAdbCommandPreview != null) {
            tvAdbCommandPreview.setText(dpiManager.getAdbCommand(currentTargetDpi));
        }
    }
}
