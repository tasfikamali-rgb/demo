package com.example.eventlottery.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.adapters.ProfileAdapter;
import com.example.eventlottery.models.User;
import com.example.eventlottery.repositories.UserRepository;
import com.example.eventlottery.utils.QRCodeHelper;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private SessionManager session;
    private UserRepository userRepo;
    private ProfileAdapter profileAdapter;
    private RecyclerView rvProfiles;
    private TextView tvNoProfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session  = new SessionManager(this);
        userRepo = new UserRepository();

        // Handle QR deep link when app was not running
        Uri data = getIntent().getData();
        if (data != null && "eventlottery".equals(data.getScheme())) {
            String eventId = QRCodeHelper.extractEventId(data.toString());
            if (session.isLoggedIn() && eventId != null) {
                openEventDetail(eventId);
                return;
            }
        }

        // Already logged in → route to the correct screen
        if (session.isLoggedIn()) {
            navigateToRole(session.getActiveRole());
            return;
        }

        setContentView(R.layout.activity_login);
        rvProfiles   = findViewById(R.id.rv_profiles);
        tvNoProfiles = findViewById(R.id.tv_no_profiles);
        MaterialButton btnCreate = findViewById(R.id.btn_create_profile);

        profileAdapter = new ProfileAdapter(this::onProfileSelected);
        rvProfiles.setLayoutManager(new LinearLayoutManager(this));
        rvProfiles.setAdapter(profileAdapter);

        btnCreate.setOnClickListener(v -> showCreateProfileDialog());
        loadProfiles();
    }

    private void loadProfiles() {
        userRepo.getAllUsers().addOnSuccessListener(qs -> {
            List<User> profiles = new ArrayList<>();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                User u = doc.toObject(User.class);
                if (u != null) {
                    u.setId(doc.getId());
                    profiles.add(u);
                }
            }
            profileAdapter.setProfiles(profiles);
            tvNoProfiles.setVisibility(profileAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        }).addOnFailureListener(e ->
            Toast.makeText(this, "Failed to load profiles", Toast.LENGTH_SHORT).show()
        );
    }

    private void onProfileSelected(User user) {
        session.saveUserId(user.getId());
        // Route to the highest-privilege role the user has
        String role = "entrant";
        if (user.hasRole("admin"))          role = "admin";
        else if (user.hasRole("organizer")) role = "organizer";
        session.saveActiveRole(role);
        navigateToRole(role);
    }

    private void navigateToRole(String role) {
        Intent intent;
        switch (role) {
            case "admin":     intent = new Intent(this, AdminMainActivity.class);    break;
            case "organizer": intent = new Intent(this, OrganizerMainActivity.class); break;
            default:          intent = new Intent(this, EntrantMainActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void openEventDetail(String eventId) {
        String role = session.getActiveRole();
        Intent main;
        switch (role) {
            case "admin":     main = new Intent(this, AdminMainActivity.class);    break;
            case "organizer": main = new Intent(this, OrganizerMainActivity.class); break;
            default:          main = new Intent(this, EntrantMainActivity.class);
        }
        main.putExtra("open_event_id", eventId);
        startActivity(main);
        finish();
    }

    private void showCreateProfileDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_profile, null);

        TextInputLayout tilName  = dialogView.findViewById(R.id.til_name);
        TextInputLayout tilEmail = dialogView.findViewById(R.id.til_email);
        TextInputEditText etName  = dialogView.findViewById(R.id.et_name);
        TextInputEditText etEmail = dialogView.findViewById(R.id.et_email);
        TextInputEditText etPhone = dialogView.findViewById(R.id.et_phone);
        CheckBox cbEntrant   = dialogView.findViewById(R.id.cb_entrant);
        CheckBox cbOrganizer = dialogView.findViewById(R.id.cb_organizer);
        CheckBox cbAdmin     = dialogView.findViewById(R.id.cb_admin);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnCreate = dialogView.findViewById(R.id.btn_create);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String name  = etName.getText()  != null ? etName.getText().toString().trim()  : "";
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

            tilName.setError(null);
            tilEmail.setError(null);

            if (TextUtils.isEmpty(name)) {
                tilName.setError(getString(R.string.error_name_required)); return;
            }
            if (TextUtils.isEmpty(email)) {
                tilEmail.setError(getString(R.string.error_email_required)); return;
            }
            List<String> roles = new ArrayList<>();
            if (cbEntrant.isChecked())   roles.add("entrant");
            if (cbOrganizer.isChecked()) roles.add("organizer");
            if (cbAdmin.isChecked()) {
                roles.add("admin");
                // US 03.09.01: Admin is also organizer and entrant by default
                if (!roles.contains("organizer")) roles.add("organizer");
                if (!roles.contains("entrant"))   roles.add("entrant");
            }
            if (roles.isEmpty()) {
                Toast.makeText(this, R.string.error_role_required, Toast.LENGTH_SHORT).show();
                return;
            }

            btnCreate.setEnabled(false);
            btnCreate.setText("Creating…");

            User newUser = new User(name, email, phone, roles, session.getDeviceId());

            userRepo.addUser(newUser).addOnSuccessListener(docRef -> {
                String newId = docRef.getId();
                newUser.setId(newId);
                dialog.dismiss();
                onProfileSelected(newUser);
            }).addOnFailureListener(e -> {
                btnCreate.setEnabled(true);
                btnCreate.setText(R.string.create_profile);
                Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            });
        });

        dialog.show();
    }
}
