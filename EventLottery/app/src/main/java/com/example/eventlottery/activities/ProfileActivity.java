package com.example.eventlottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.eventlottery.R;
import com.example.eventlottery.models.User;
import com.example.eventlottery.repositories.UserRepository;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager session;
    private UserRepository userRepo;
    private User currentUser;

    private TextView tvAvatar, tvDisplayName;
    private LinearLayout llRoleBadges;
    private TextInputEditText etName, etEmail, etPhone;
    private SwitchMaterial swNotifications;
    private MaterialButton btnSave, btnSignOut, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session  = new SessionManager(this);
        userRepo = new UserRepository();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvAvatar      = findViewById(R.id.tv_avatar);
        tvDisplayName = findViewById(R.id.tv_display_name);
        llRoleBadges  = findViewById(R.id.ll_role_badges);
        etName        = findViewById(R.id.et_name);
        etEmail       = findViewById(R.id.et_email);
        etPhone       = findViewById(R.id.et_phone);
        swNotifications = findViewById(R.id.sw_notifications);
        btnSave       = findViewById(R.id.btn_save);
        btnSignOut    = findViewById(R.id.btn_sign_out);
        btnDelete     = findViewById(R.id.btn_delete_profile);

        loadUser();

        btnSave.setOnClickListener(v -> saveUser());
        btnSignOut.setOnClickListener(v -> confirmSignOut());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadUser() {
        String userId = session.getUserId();
        if (userId == null) return;
        userRepo.getUserById(userId).addOnSuccessListener(doc -> {
            currentUser = doc.toObject(User.class);
            if (currentUser == null) return;
            currentUser.setId(doc.getId());
            populateUI();
        });
    }

    private void populateUI() {
        tvAvatar.setText(currentUser.getInitials());
        tvDisplayName.setText(currentUser.getName());
        etName.setText(currentUser.getName());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhone());
        swNotifications.setChecked(currentUser.isPushNotificationsEnabled());

        // Role badges
        llRoleBadges.removeAllViews();
        if (currentUser.getRoles() != null) {
            for (String role : currentUser.getRoles()) {
                TextView badge = new TextView(this);
                badge.setText(capitalize(role));
                badge.setTextSize(11f);
                badge.setTextColor(0xFFFFFFFF);
                int pad = dp(8);
                badge.setPadding(pad, dp(3), pad, dp(3));
                badge.setBackgroundResource(getBadgeRes(role));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd(dp(4));
                badge.setLayoutParams(lp);
                llRoleBadges.addView(badge);
            }
        }
    }

    private void saveUser() {
        // Guard: Firestore load may not have completed yet
        if (currentUser == null) {
            Toast.makeText(this, "Profile still loading, please wait", Toast.LENGTH_SHORT).show();
            return;
        }
        String name  = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setName(name);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);
        currentUser.setPushNotificationsEnabled(swNotifications.isChecked());

        btnSave.setEnabled(false);
        userRepo.updateUser(currentUser.getId(), currentUser)
                .addOnSuccessListener(v -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    tvAvatar.setText(currentUser.getInitials());
                    tvDisplayName.setText(currentUser.getName());
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmSignOut() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage(R.string.sign_out_confirm)
                .setPositiveButton("Sign Out", (d, w) -> {
                    session.clearSession();
                    startActivity(new Intent(this, LoginActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_profile)
                .setMessage(R.string.delete_profile_confirm)
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    userRepo.deleteUser(currentUser.getId())
                            .addOnSuccessListener(v -> {
                                session.clearSession();
                                startActivity(new Intent(this, LoginActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private int getBadgeRes(String role) {
        switch (role) {
            case "organizer": return R.drawable.bg_badge_accent;
            case "admin":     return R.drawable.bg_badge_red;
            default:          return R.drawable.bg_badge_green;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private int dp(int val) {
        return Math.round(val * getResources().getDisplayMetrics().density);
    }
}
