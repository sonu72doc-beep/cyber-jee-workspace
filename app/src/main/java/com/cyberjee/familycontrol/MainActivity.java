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
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private TextView budgetSummary;
    private NumberFormat numberFormat;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFFF5F7FB);
        getWindow().setNavigationBarColor(0xFFF5F7FB);
        prefs = getSharedPreferences("family_control_settings", MODE_PRIVATE);
        numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault());
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
        page.addView(text("Family spending & family safety", 21, true));
        page.addView(text("Your family settings stay on this phone until secure pairing is added.", 14, false));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dp(18);
        page.addView(card, cardParams);

        card.addView(text("Monthly spending limit", 18, true));
        budgetSummary = text("", 16, false);
        card.addView(budgetSummary);
        EditText amount = new EditText(this);
        amount.setSingleLine(true);
        amount.setInputType(2);
        amount.setHint("Enter limit in rupees (e.g. 3000)");
        amount.setText(String.valueOf(prefs.getInt("monthly_limit", 3000)));
        card.addView(amount);

        Button save = new Button(this);
        save.setText("Save spending limit");
        card.addView(save);
        save.setOnClickListener(v -> {
            String raw = amount.getText().toString().trim();
            try {
                int value = Integer.parseInt(raw);
                if (value < 0 || value > 10000000) throw new NumberFormatException();
                prefs.edit().putInt("monthly_limit", value).apply();
                updateSummary();
                Toast.makeText(this, "Limit saved on this phone", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException ex) {
                amount.setError("Enter a valid amount");
            }
        });
        updateSummary();

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(16), dp(14), dp(16), dp(14));
        info.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        infoParams.topMargin = dp(14);
        page.addView(info, infoParams);
        info.addView(text("Device pairing", 18, true));
        info.addView(text("Not connected yet. QR pairing and secure cloud sync must be implemented before other phones can be managed.", 14, false));
        info.addView(text("Privacy", 18, true));
        info.addView(text("This initial build does not access camera, gallery, location, SMS, call history, or payment apps.", 14, false));

        TextView footer = text("FamGuard · Version 0.1.2 · Local settings only", 12, false);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        footerParams.topMargin = dp(18);
        page.addView(footer, footerParams);

        setContentView(scroll);
    }

    private void updateSummary() {
        int limit = prefs.getInt("monthly_limit", 3000);
        budgetSummary.setText("Saved limit: ₹" + numberFormat.format(limit) + " / month");
    }
}
