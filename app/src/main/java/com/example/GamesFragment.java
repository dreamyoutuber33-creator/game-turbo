package com.example;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class GamesFragment extends Fragment implements GamesAdapter.GameActionListener {

    private GameLauncher gameLauncher;
    private GameProfileManager profileManager;

    private TextView tvGamesCountSummary;
    private MaterialButton btnAddGame;
    private View layoutLastPlayedSection;
    private RecyclerView rvLastPlayed;
    private RecyclerView rvGamesList;
    private View layoutEmptyState;

    private GamesAdapter gamesAdapter;
    private LastPlayedAdapter lastPlayedAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_tab_games, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileManager = new GameProfileManager(requireContext());
        gameLauncher = new GameLauncher(requireContext(), profileManager);

        initViews(view);
        setupRecyclerViews();
        loadGamesData();
    }

    private void initViews(View view) {
        tvGamesCountSummary = view.findViewById(R.id.tv_games_count_summary);
        btnAddGame = view.findViewById(R.id.btn_add_game);
        layoutLastPlayedSection = view.findViewById(R.id.layout_last_played_section);
        rvLastPlayed = view.findViewById(R.id.rv_last_played);
        rvGamesList = view.findViewById(R.id.rv_games_list);
        layoutEmptyState = view.findViewById(R.id.layout_games_empty_state);

        btnAddGame.setOnClickListener(v -> showAddGameDialog());
    }

    private void setupRecyclerViews() {
        gamesAdapter = new GamesAdapter(this);
        rvGamesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvGamesList.setAdapter(gamesAdapter);

        lastPlayedAdapter = new LastPlayedAdapter(this::onPlayClicked);
        rvLastPlayed.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvLastPlayed.setAdapter(lastPlayedAdapter);
    }

    public void loadGamesData() {
        List<GameItem> myGames = gameLauncher.getMyGames();
        List<GameItem> lastPlayed = gameLauncher.getLastPlayedGames(6);

        gamesAdapter.setItems(myGames);
        lastPlayedAdapter.setItems(lastPlayed);

        if (lastPlayed.isEmpty()) {
            layoutLastPlayedSection.setVisibility(View.GONE);
        } else {
            layoutLastPlayedSection.setVisibility(View.VISIBLE);
        }

        if (myGames.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvGamesList.setVisibility(View.GONE);
            tvGamesCountSummary.setText("0 games configured");
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvGamesList.setVisibility(View.VISIBLE);
            tvGamesCountSummary.setText(myGames.size() + " games configured in launcher");
        }
    }

    @Override
    public void onPlayClicked(GameItem game) {
        gameLauncher.launchGame(requireActivity(), game, new GameLauncher.LaunchCallback() {
            @Override
            public void onGameLaunched(GameItem item, GameProfileManager.AppliedSettingsResult settingsResult) {
                Toast.makeText(requireContext(), "Launched " + item.getTitle(), Toast.LENGTH_SHORT).show();
                loadGamesData();
            }

            @Override
            public void onGameNotInstalled(String packageName) {
                Toast.makeText(requireContext(), "This game is not installed on your device.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLaunchFailed(String reason) {
                Toast.makeText(requireContext(), reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProfileClicked(GameItem game) {
        showProfileDialog(game);
    }

    @Override
    public void onFavoriteToggled(GameItem game) {
        GameProfile profile = game.getProfile();
        profile.setFavorite(!profile.isFavorite());
        profileManager.saveProfile(profile);
        loadGamesData();
    }

    @Override
    public void onRemoveClicked(GameItem game) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.Theme_GameTurboPro)
                .setTitle("Remove Game")
                .setMessage("Remove '" + game.getTitle() + "' from Game Turbo Pro launcher? (The app will remain installed on your phone).")
                .setPositiveButton("Remove", (dialog, which) -> {
                    gameLauncher.removeGameFromLibrary(game.getPackageName());
                    loadGamesData();
                    Toast.makeText(requireContext(), "Removed from launcher", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showProfileDialog(final GameItem game) {
        final GameProfile profile = profileManager.getProfile(game.getPackageName(), game.getTitle());

        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_game_profile);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = dialog.findViewById(R.id.dialog_profile_game_title);
        EditText etDpi = dialog.findViewById(R.id.et_profile_dpi);
        TextView tvBrightVal = dialog.findViewById(R.id.tv_profile_brightness_val);
        SeekBar sbBright = dialog.findViewById(R.id.sb_profile_brightness);
        SwitchMaterial switchAwake = dialog.findViewById(R.id.switch_profile_keep_awake);
        SwitchMaterial switchDnd = dialog.findViewById(R.id.switch_profile_dnd);
        SwitchMaterial switchAutoBoost = dialog.findViewById(R.id.switch_profile_auto_boost);
        Spinner spinnerRefresh = dialog.findViewById(R.id.spinner_profile_refresh_rate);
        EditText etSensitivity = dialog.findViewById(R.id.et_profile_sensitivity);
        EditText etGraphics = dialog.findViewById(R.id.et_profile_graphics);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_profile_cancel);
        MaterialButton btnSave = dialog.findViewById(R.id.btn_profile_save);

        tvTitle.setText(game.getTitle());

        // Refresh rate choices
        String[] refreshRates = new String[]{"Device Auto (Default)", "60 Hz Standard", "90 Hz High", "120 Hz Ultra", "144 Hz Max Pro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, refreshRates);
        spinnerRefresh.setAdapter(adapter);

        if (profile.getRefreshRateHz() == 60) spinnerRefresh.setSelection(1);
        else if (profile.getRefreshRateHz() == 90) spinnerRefresh.setSelection(2);
        else if (profile.getRefreshRateHz() == 120) spinnerRefresh.setSelection(3);
        else if (profile.getRefreshRateHz() == 144) spinnerRefresh.setSelection(4);
        else spinnerRefresh.setSelection(0);

        if (profile.getDpiPreference() > 0) {
            etDpi.setText(String.valueOf(profile.getDpiPreference()));
        }

        int curBright = profile.getBrightnessPercent();
        if (curBright >= 0) {
            sbBright.setProgress(curBright);
            tvBrightVal.setText(curBright + "%");
        } else {
            sbBright.setProgress(80);
            tvBrightVal.setText("Default");
        }

        sbBright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvBrightVal.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        switchAwake.setChecked(profile.isKeepScreenAwake());
        switchDnd.setChecked(profile.isDndPreference());
        switchAutoBoost.setChecked(profile.isGamingModeAutoBoost());

        etSensitivity.setText(profile.getSensitivityNotes());
        etGraphics.setText(profile.getGraphicsFpsNotes());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            try {
                String dpiStr = etDpi.getText().toString().trim();
                profile.setDpiPreference(dpiStr.isEmpty() ? 0 : Integer.parseInt(dpiStr));
            } catch (Exception ignored) {
                profile.setDpiPreference(0);
            }

            profile.setBrightnessPercent(sbBright.getProgress());
            profile.setKeepScreenAwake(switchAwake.isChecked());
            profile.setDndPreference(switchDnd.isChecked());
            profile.setGamingModeAutoBoost(switchAutoBoost.isChecked());

            int sel = spinnerRefresh.getSelectedItemPosition();
            if (sel == 1) profile.setRefreshRateHz(60);
            else if (sel == 2) profile.setRefreshRateHz(90);
            else if (sel == 3) profile.setRefreshRateHz(120);
            else if (sel == 4) profile.setRefreshRateHz(144);
            else profile.setRefreshRateHz(-1);

            profile.setSensitivityNotes(etSensitivity.getText().toString().trim());
            profile.setGraphicsFpsNotes(etGraphics.getText().toString().trim());

            profileManager.saveProfile(profile);
            dialog.dismiss();
            loadGamesData();
            Toast.makeText(requireContext(), "Profile saved for " + game.getTitle(), Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void showAddGameDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_game);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etSearch = dialog.findViewById(R.id.et_search_apps);
        RecyclerView rvApps = dialog.findViewById(R.id.rv_select_apps);
        MaterialButton btnClose = dialog.findViewById(R.id.btn_close_add_game);

        List<GameItem> installedApps = gameLauncher.getInstalledLaunchableApps();
        AppSelectAdapter adapter = new AppSelectAdapter(installedApps, item -> {
            gameLauncher.addGameToLibrary(item.getPackageName());
            profileManager.setSelectedGame(item.getPackageName());
            loadGamesData();
            Toast.makeText(requireContext(), "Added " + item.getTitle() + " to Game Turbo Pro", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        rvApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvApps.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
            }
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGamesData();
    }
}
