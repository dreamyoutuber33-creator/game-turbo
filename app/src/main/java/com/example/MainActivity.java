package com.example;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

/**
 * MainActivity is the primary navigation hub for GAME TURBO PRO,
 * coordinating the 5 gaming tabs: HOME, BOOST, DPI, GAMES, and SETTINGS.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private ImageView ivTopLogo;
    private TextView tvTopTitle;
    private ImageView btnQuickAccent;
    private TextView btnQuickBoostChip;

    private ThemeAccentManager accentManager;

    private HomeFragment homeFragment;
    private BoostFragment boostFragment;
    private DpiFragment dpiFragment;
    private GamesFragment gamesFragment;
    private SettingsFragment settingsFragment;

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accentManager = new ThemeAccentManager(this);

        initViews();
        setupFragments();
        setupNavigation();
        applyThemeAccent();
    }

    private void initViews() {
        bottomNav = findViewById(R.id.bottom_navigation);
        ivTopLogo = findViewById(R.id.iv_top_app_logo);
        tvTopTitle = findViewById(R.id.tv_top_title);
        btnQuickAccent = findViewById(R.id.btn_quick_accent);
        btnQuickBoostChip = findViewById(R.id.btn_quick_boost_chip);

        btnQuickAccent.setOnClickListener(v -> showAccentPickerDialog());

        btnQuickBoostChip.setOnClickListener(v -> {
            // Switch to Home tab and perform boost
            bottomNav.setSelectedItemId(R.id.nav_home);
            if (homeFragment != null) {
                homeFragment.performBoostAction();
            }
        });
    }

    private void setupFragments() {
        homeFragment = new HomeFragment();
        boostFragment = new BoostFragment();
        dpiFragment = new DpiFragment();
        gamesFragment = new GamesFragment();
        settingsFragment = new SettingsFragment();

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.fl_content_container, settingsFragment, "settings").hide(settingsFragment)
                .add(R.id.fl_content_container, gamesFragment, "games").hide(gamesFragment)
                .add(R.id.fl_content_container, dpiFragment, "dpi").hide(dpiFragment)
                .add(R.id.fl_content_container, boostFragment, "boost").hide(boostFragment)
                .add(R.id.fl_content_container, homeFragment, "home")
                .commit();

        activeFragment = homeFragment;
    }

    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment targetFragment = null;

            if (itemId == R.id.nav_home) {
                targetFragment = homeFragment;
            } else if (itemId == R.id.nav_boost) {
                targetFragment = boostFragment;
            } else if (itemId == R.id.nav_dpi) {
                targetFragment = dpiFragment;
            } else if (itemId == R.id.nav_games) {
                targetFragment = gamesFragment;
            } else if (itemId == R.id.nav_settings) {
                targetFragment = settingsFragment;
            }

            if (targetFragment != null && targetFragment != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(activeFragment)
                        .show(targetFragment)
                        .commit();
                activeFragment = targetFragment;
                return true;
            }
            return true;
        });
    }

    public void applyThemeAccent() {
        int color = accentManager.getAccentColor();
        ivTopLogo.setColorFilter(color);
        tvTopTitle.setTextColor(color);
        btnQuickAccent.setColorFilter(color);
    }

    private void showAccentPickerDialog() {
        final Dialog dialog = new Dialog(this);
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
            applyThemeAccent();

            Toast.makeText(this, "Accent updated: " + accentManager.getAccentName(), Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}
