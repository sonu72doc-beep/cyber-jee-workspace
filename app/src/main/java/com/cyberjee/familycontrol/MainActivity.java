package com.cyberjee.familycontrol;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private EditText nameInput;
    private Spinner roleSpinner;
    private TextView profileStatus;
    private TextView deviceStatus;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFFF5F7FB);
        getWindow().setNavigationBarColor(0xFFF5F7FB);
        prefs = getSharedPreferences("family_control_settings", MODE_PRIVATE);
        render();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, int size, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(0xFF172033);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        v.setPadding(0, dp(6), 0, dp(6));
        return v;
    }

    private LinearLayout card(LinearLayout page) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(14), dp(16), dp(14));
        box.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(14);
        page.addView(box, params);
        return box;
    }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(28), dp(22), dp(24));
        page.setBackgroundColor(0xFFF5F7FB);
        scroll.addView(page);

        TextView title = text("FamGuard", 30, true);
        title.setTextColor(0xFF1646A0);
        page.addView(title);
        page.addView(text("Family device safety", 21, true));
        page.addView(text("Set up this phone first. Secure pairing will let family members connect their devices.", 14, false));

        LinearLayout setup = card(page);
        setup.addView(text("Set up your profile", 18, true));
        setup.addView(text("Profile details are saved only on this phone for now.", 14, false));

        nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.setHint("Your name");
        nameInput.setText(prefs.getString("profile_name", ""));
        setup.addView(nameInput);

        roleSpinner = new Spinner(this);
        String[] roles = {"Family admin", "Family member"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
        String savedRole = prefs.getString("profile_role", "Family admin");
        roleSpinner.setSelection("Family member".equals(savedRole) ? 1 : 0);
        setup.addView(roleSpinner);

        Button saveProfile = new Button(this);
        saveProfile.setText("Save profile");
        setup.addView(saveProfile);
        saveProfile.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                nameInput.setError("Enter your name");
                return;
            }
            String role = roleSpinner.getSelectedItem().toString();
            prefs.edit().putString("profile_name", name).putString("profile_role", role).apply();
            updateProfileStatus();
            Toast.makeText(this, "Profile saved on this phone", Toast.LENGTH_SHORT).show();
        });
        profileStatus = text("", 14, false);
        setup.addView(profileStatus);
        updateProfileStatus();

        LinearLayout devices = card(page);
        devices.addView(text("Family devices", 18, true));
        deviceStatus = text("No devices connected yet.", 15, false);
        devices.addView(deviceStatus);
        devices.addView(text("Next step: secure QR / invite-link pairing and shared family data. Pairing is not active in this build.", 14, false));

        LinearLayout privacy = card(page);
        privacy.addView(text("Privacy & permissions", 18, true));
        privacy.addView(text("FamGuard does not access payment apps, SMS, call history, camera, or location in this build. Sensitive features must request permission before use.", 14, false));

        TextView footer = text("FamGuard · Version 0.2.0 · Profile stored locally", 12, false);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        footerParams.topMargin = dp(18);
        page.addView(footer, footerParams);

        setContentView(scroll);
    }

    private void updateProfileStatus() {
        String name = prefs.getString("profile_name", "");
        String role = prefs.getString("profile_role", "Family admin");
        if (name.isEmpty()) {
            profileStatus.setText("Profile not saved yet.");
        } else {
            profileStatus.setText("Saved profile: " + name + " · " + role);
        }
    }
}
