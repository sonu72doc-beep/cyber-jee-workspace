package com.cyberjee.familycontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private EditText nameInput;
    private Spinner roleSpinner;
    private TextView profileStatus;
    private TextView deviceStatus;
    private LinearLayout devicesCard;
    private LinearLayout deviceRows;
    private ImageView qrImage;
    private TextView inviteStatus;
    private String currentInvitePayload = "";

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

    private Button button(String label, LinearLayout parent) {
        Button b = new Button(this);
        b.setText(label);
        parent.addView(b);
        return b;
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
        page.addView(text("Set up this phone and try QR invitations. Cloud syncing is not configured yet.", 14, false));

        LinearLayout setup = card(page);
        setup.addView(text("Set up your profile", 18, true));
        setup.addView(text("Profile details are saved only on this phone.", 14, false));

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

        button("Save profile", setup).setOnClickListener(v -> {
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

        devicesCard = card(page);
        devicesCard.addView(text("Family devices", 18, true));
        deviceStatus = text("", 14, false);
        devicesCard.addView(deviceStatus);
        deviceRows = new LinearLayout(this);
        deviceRows.setOrientation(LinearLayout.VERTICAL);
        devicesCard.addView(deviceRows);
        button("Scan a family QR", devicesCard).setOnClickListener(v -> startQrScan());
        button("Enter invite code manually", devicesCard).setOnClickListener(v -> showManualInviteEntry());
        button("Refresh device list", devicesCard).setOnClickListener(v -> updateDeviceStatus());
        button("Clear local device list", devicesCard).setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle("Clear saved demo devices?")
                .setMessage("This removes all device records saved on this phone only. It does not affect other phones.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear list", (dialog, which) -> {
                    prefs.edit().putString("family_devices", "[]").apply();
                    updateDeviceStatus();
                    Toast.makeText(this, "Local device list cleared", Toast.LENGTH_SHORT).show();
                })
                .show());
        updateDeviceStatus();

        LinearLayout invite = card(page);
        invite.addView(text("Invite a family device", 18, true));
        invite.addView(text("Generate a QR on this phone, then scan it from another FamGuard phone. This prototype stores records locally only; it does not sync or securely authorize devices across phones yet.", 14, false));
        button("Generate invite QR", invite).setOnClickListener(v -> generateInvite());
        qrImage = new ImageView(this);
        qrImage.setAdjustViewBounds(true);
        qrImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams qrParams = new LinearLayout.LayoutParams(
            dp(230), dp(230));
        qrParams.gravity = Gravity.CENTER_HORIZONTAL;
        qrImage.setVisibility(ImageView.GONE);
        invite.addView(qrImage, qrParams);
        inviteStatus = text("No invitation generated yet.", 13, false);
        invite.addView(inviteStatus);
        button("Copy invite code", invite).setOnClickListener(v -> {
            if (currentInvitePayload.isEmpty()) {
                Toast.makeText(this, "Generate an invite QR first", Toast.LENGTH_SHORT).show();
                return;
            }
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("FamGuard invite", currentInvitePayload));
            Toast.makeText(this, "Invite code copied. Send it only to your family member.", Toast.LENGTH_LONG).show();
        });

        LinearLayout privacy = card(page);
        privacy.addView(text("Privacy & permissions", 18, true));
        privacy.addView(text("Camera permission is used only when you choose to scan a QR. This build does not access payment apps, SMS, call history, or location. QR pairing is a demo and is not a security boundary.", 14, false));

        TextView footer = text("FamGuard · Version 0.4.0 · Local prototype", 12, false);
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
        profileStatus.setText(name.isEmpty() ? "Profile not saved yet." : "Saved profile: " + name + " · " + role);
    }

    private void generateInvite() {
        String name = prefs.getString("profile_name", "").trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Save your profile first", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = UUID.randomUUID().toString();
        String encodedName = Base64.encodeToString(name.getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        String payload = "FAMGUARD|1|" + token + "|" + encodedName;
        currentInvitePayload = payload;
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, 640, 640);
            Bitmap bitmap = new BarcodeEncoder().createBitmap(matrix);
            qrImage.setImageBitmap(bitmap);
            qrImage.setVisibility(ImageView.VISIBLE);
            inviteStatus.setText("Invite created for " + name + ". Token: " + token.substring(0, 8) + "…\nFor testing only: this QR is not a secure cloud invitation.");
        } catch (Exception e) {
            Toast.makeText(this, "Could not generate QR. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    private void startQrScan() {
        new IntentIntegrator(this)
            .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            .setPrompt("Scan a FamGuard invite QR")
            .setBeepEnabled(false)
            .setOrientationLocked(true)
            .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String contents = result.getContents();
            if (contents == null) {
                Toast.makeText(this, "QR scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                saveScannedInvite(contents);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void showManualInviteEntry() {
        EditText input = new EditText(this);
        input.setHint("Paste the complete FamGuard invite code");
        input.setMinLines(2);
        input.setGravity(Gravity.TOP | Gravity.START);
        new AlertDialog.Builder(this)
            .setTitle("Enter invite code")
            .setMessage("Use this if the camera scanner is unavailable. On the other phone, generate an invite and tap Copy invite code. This still saves a local demo record only.")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add invite", (dialog, which) -> {
                String code = input.getText().toString().trim();
                if (code.isEmpty()) {
                    Toast.makeText(this, "Paste an invite code first", Toast.LENGTH_SHORT).show();
                } else {
                    saveScannedInvite(code);
                }
            })
            .show();
    }

    private void saveScannedInvite(String contents) {
        String[] parts = contents.split("\\|", 4);
        if (parts.length != 4 || !"FAMGUARD".equals(parts[0]) || !"1".equals(parts[1])) {
            Toast.makeText(this, "This is not a supported FamGuard invite QR", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            String name = new String(Base64.decode(parts[3], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING), StandardCharsets.UTF_8);
            if (parts[2].trim().isEmpty() || name.trim().isEmpty()) throw new IllegalArgumentException("Empty invite");
            // Invite tokens are UUIDs generated by this prototype. Validate their format before saving.
            UUID.fromString(parts[2]);
            JSONArray devices = new JSONArray(prefs.getString("family_devices", "[]"));
            for (int i = 0; i < devices.length(); i++) {
                if (parts[2].equals(devices.getJSONObject(i).optString("token"))) {
                    Toast.makeText(this, "This invite is already saved on this phone", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            if (devices.length() >= 10) {
                Toast.makeText(this, "This prototype supports up to 10 saved demo devices", Toast.LENGTH_LONG).show();
                return;
            }
            JSONObject device = new JSONObject();
            device.put("name", name);
            device.put("token", parts[2]);
            device.put("status", "Demo QR scanned · local only");
            devices.put(device);
            prefs.edit().putString("family_devices", devices.toString()).apply();
            updateDeviceStatus();
            new AlertDialog.Builder(this)
                .setTitle("Invite QR saved")
                .setMessage(name + " has been added to this phone's local demo list. The other phone is not notified, and no cloud connection has been made.")
                .setPositiveButton("OK", null)
                .show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not read this invite QR", Toast.LENGTH_LONG).show();
        }
    }

    private void removeDevice(String token) {
        try {
            JSONArray devices = new JSONArray(prefs.getString("family_devices", "[]"));
            JSONArray remaining = new JSONArray();
            for (int i = 0; i < devices.length(); i++) {
                JSONObject item = devices.getJSONObject(i);
                if (!token.equals(item.optString("token"))) remaining.put(item);
            }
            prefs.edit().putString("family_devices", remaining.toString()).apply();
            updateDeviceStatus();
            Toast.makeText(this, "Demo device removed from this phone", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not update the local device list", Toast.LENGTH_LONG).show();
        }
    }

    private void updateDeviceStatus() {
        if (deviceStatus == null) return;
        deviceRows.removeAllViews();
        try {
            JSONArray devices = new JSONArray(prefs.getString("family_devices", "[]"));
            if (devices.length() == 0) {
                deviceStatus.setText("No demo devices saved yet. Generate an invite on one phone and scan it from another.");
                return;
            }
            deviceStatus.setText("Saved on this phone only · " + devices.length() + "/10 demo devices");
            for (int i = 0; i < devices.length(); i++) {
                JSONObject device = devices.getJSONObject(i);
                String name = device.optString("name", "Family device");
                String token = device.optString("token", "");
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(dp(10), dp(8), dp(10), dp(8));
                row.setBackgroundColor(0xFFF0F4FA);
                TextView details = text(name + "\n" + device.optString("status", "Local demo"), 14, true);
                row.addView(details);
                Button remove = new Button(this);
                remove.setText("Remove " + name);
                remove.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Remove demo device?")
                    .setMessage("This only removes the saved record from this phone.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Remove", (dialog, which) -> removeDevice(token))
                    .show());
                row.addView(remove);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.topMargin = dp(8);
                deviceRows.addView(row, params);
            }
        } catch (Exception e) {
            deviceStatus.setText("Device list could not be read. You can try scanning again.");
        }
    }
}
