package com.example.eventlottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.adapters.RegistrationAdapter;
import com.example.eventlottery.adapters.UserSearchAdapter;
import com.example.eventlottery.models.AppNotification;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.models.Registration;
import com.example.eventlottery.models.User;
import com.example.eventlottery.repositories.EventRepository;
import com.example.eventlottery.repositories.NotificationRepository;
import com.example.eventlottery.repositories.RegistrationRepository;
import com.example.eventlottery.repositories.UserRepository;
import com.example.eventlottery.utils.CsvExportHelper;
import com.example.eventlottery.utils.LotteryEngine;
import com.example.eventlottery.utils.QRCodeHelper;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrganizerEventManagementActivity extends AppCompatActivity {

    private SessionManager session;
    private EventRepository eventRepo;
    private RegistrationRepository regRepo;
    private UserRepository userRepo;
    private NotificationRepository notifRepo;
    private LotteryEngine lotteryEngine;

    private Event currentEvent;
    private String currentTab = Registration.STATUS_WAITING;

    private TextView tvEventStatusBadge;
    private TextView tvStatWaiting, tvStatSelected, tvStatAccepted, tvStatDeclined;
    private RecyclerView rvRegistrations;
    private TextView tvEmptyTab;
    private RegistrationAdapter registrationAdapter;
    private TabLayout tabLayout;
    private ChipGroup cgAudience;
    private TextInputEditText etNotifMsg;
    private MaterialButton btnSendNotification;
    private ImageView ivQrToolbar;
    private MaterialButton btnInviteEntrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_event_management);

        session      = new SessionManager(this);
        eventRepo    = new EventRepository();
        regRepo      = new RegistrationRepository();
        userRepo     = new UserRepository();
        notifRepo    = new NotificationRepository();
        lotteryEngine = new LotteryEngine();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        String eventId = getIntent().getStringExtra("event_id");
        if (eventId == null) { finish(); return; }

        bindViews();
        loadEvent(eventId);
    }

    private void bindViews() {
        tvEventStatusBadge = findViewById(R.id.tv_event_status_badge);
        tvStatWaiting   = findViewById(R.id.tv_stat_waiting);
        tvStatSelected  = findViewById(R.id.tv_stat_selected);
        tvStatAccepted  = findViewById(R.id.tv_stat_accepted);
        tvStatDeclined  = findViewById(R.id.tv_stat_declined);
        rvRegistrations = findViewById(R.id.rv_registrations);
        tvEmptyTab      = findViewById(R.id.tv_empty_tab);
        tabLayout       = findViewById(R.id.tab_layout);
        cgAudience      = findViewById(R.id.cg_audience);
        etNotifMsg      = findViewById(R.id.et_notification_msg);
        btnSendNotification = findViewById(R.id.btn_send_notification);
        ivQrToolbar     = findViewById(R.id.iv_qr_toolbar);
        btnInviteEntrant = findViewById(R.id.btn_invite_entrant);

        registrationAdapter = new RegistrationAdapter(true);
        registrationAdapter.setCancelListener(r -> cancelEntrant(r));
        rvRegistrations.setLayoutManager(new LinearLayoutManager(this));
        rvRegistrations.setAdapter(registrationAdapter);

        // Tabs
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_waiting));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_invited));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_enrolled));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_cancelled));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentTab = Registration.STATUS_WAITING; break;
                    case 1: currentTab = Registration.STATUS_SELECTED; break;
                    case 2: currentTab = Registration.STATUS_ACCEPTED; break;
                    case 3: currentTab = Registration.STATUS_CANCELLED; break;
                }
                if (currentEvent != null) loadTabData();
            }
            @Override public void onTabUnselected(TabLayout.Tab t) {}
            @Override public void onTabReselected(TabLayout.Tab t) {}
        });

        findViewById(R.id.btn_run_lottery).setOnClickListener(v -> showLotteryDialog());
        findViewById(R.id.btn_draw_replacement).setOnClickListener(v -> drawReplacement());
        findViewById(R.id.btn_export_csv).setOnClickListener(v -> exportCsv());
        ivQrToolbar.setOnClickListener(v -> showQrDialog());
        findViewById(R.id.btn_view_map).setOnClickListener(v -> openMap());
        findViewById(R.id.btn_co_organizers).setOnClickListener(v -> showCoOrganizerDialog());
        btnSendNotification.setOnClickListener(v -> sendBulkNotification());
        btnInviteEntrant.setOnClickListener(v -> showInviteEntrantDialog());
    }

    private void loadEvent(String eventId) {
        eventRepo.getEventById(eventId).addOnSuccessListener(doc -> {
            currentEvent = doc.toObject(Event.class);
            if (currentEvent == null) { finish(); return; }
            currentEvent.setId(doc.getId());

            if (getSupportActionBar() != null) getSupportActionBar().setTitle(currentEvent.getTitle());
            tvEventStatusBadge.setText(currentEvent.getStatus().toUpperCase());
            
            // Hide QR button if event is private
            if (currentEvent.isPrivate()) {
                ivQrToolbar.setVisibility(View.GONE);
                btnInviteEntrant.setVisibility(View.VISIBLE); // Show invite button for private events
            } else {
                ivQrToolbar.setVisibility(View.VISIBLE);
                btnInviteEntrant.setVisibility(View.GONE);
            }

            loadStats();
            loadTabData();
        });
    }

    private void loadStats() {
        regRepo.getRegistrationsForEvent(currentEvent.getId())
                .addOnSuccessListener(qs -> {
                    int waiting = 0, selected = 0, accepted = 0, declined = 0;
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Registration r = doc.toObject(Registration.class);
                        if (r == null) continue;
                        switch (r.getStatus()) {
                            case Registration.STATUS_WAITING:   waiting++;  break;
                            case Registration.STATUS_SELECTED:  selected++; break;
                            case Registration.STATUS_ACCEPTED:  accepted++; break;
                            case Registration.STATUS_DECLINED:  declined++; break;
                        }
                    }
                    tvStatWaiting.setText(String.valueOf(waiting));
                    tvStatSelected.setText(String.valueOf(selected));
                    tvStatAccepted.setText(String.valueOf(accepted));
                    tvStatDeclined.setText(String.valueOf(declined));
                });
    }

    private void loadTabData() {
        regRepo.getRegistrationsByStatus(currentEvent.getId(), currentTab)
                .addOnSuccessListener(qs -> {
                    List<Registration> list = new ArrayList<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Registration r = doc.toObject(Registration.class);
                        if (r != null) { r.setId(doc.getId()); list.add(r); }
                    }
                    registrationAdapter.setRegistrations(list);
                    tvEmptyTab.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    rvRegistrations.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }

    private void openMap() {
        Intent intent = new Intent(this, EntrantMapActivity.class);
        intent.putExtra("event_id", currentEvent.getId());
        startActivity(intent);
    }

    private void cancelEntrant(Registration r) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Entrant")
                .setMessage("Remove " + r.getUserName() + " from this event?")
                .setPositiveButton("Cancel Entrant", (d, w) -> {
                    regRepo.updateStatus(currentEvent.getId(), r.getUserId(),
                            Registration.STATUS_CANCELLED)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "Entrant cancelled", Toast.LENGTH_SHORT).show();
                                loadStats();
                                loadTabData();
                            });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showLotteryDialog() {
        final EditText etCount = new EditText(this);
        etCount.setHint(getString(R.string.lottery_sample_prompt));
        etCount.setText(String.valueOf(currentEvent.getCapacity()));
        etCount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etCount.setPadding(dp(20), dp(16), dp(20), dp(8));

        new AlertDialog.Builder(this)
                .setTitle(R.string.run_lottery)
                .setMessage("How many attendees to randomly select from the waiting list?")
                .setView(etCount)
                .setPositiveButton(R.string.run_lottery, (d, w) -> {
                    int count = safeInt(etCount.getText().toString(), currentEvent.getCapacity());
                    runLottery(count);
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void runLottery(int sampleSize) {
        Toast.makeText(this, R.string.lottery_running, Toast.LENGTH_SHORT).show();
        lotteryEngine.runLottery(currentEvent, sampleSize, new LotteryEngine.LotteryCallback() {
            @Override public void onSuccess(int selected, int notSelected) {
                eventRepo.updateEventStatus(currentEvent.getId(), Event.STATUS_DRAWN);
                currentEvent.setStatus(Event.STATUS_DRAWN);
                tvEventStatusBadge.setText(Event.STATUS_DRAWN.toUpperCase());
                Toast.makeText(OrganizerEventManagementActivity.this,
                        getString(R.string.lottery_success, selected, notSelected),
                        Toast.LENGTH_LONG).show();
                loadStats();
                loadTabData();
            }
            @Override public void onFailure(Exception e) {
                Toast.makeText(OrganizerEventManagementActivity.this,
                        R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawReplacement() {
        lotteryEngine.drawReplacement(currentEvent, new LotteryEngine.LotteryCallback() {
            @Override public void onSuccess(int selected, int notSelected) {
                if (selected == 0) {
                    Toast.makeText(OrganizerEventManagementActivity.this,
                            R.string.no_waiting_list, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OrganizerEventManagementActivity.this,
                            R.string.replacement_drawn, Toast.LENGTH_SHORT).show();
                    loadStats();
                    loadTabData();
                }
            }
            @Override public void onFailure(Exception e) {
                Toast.makeText(OrganizerEventManagementActivity.this,
                        R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCoOrganizerDialog() {
        final EditText etEmail = new EditText(this);
        etEmail.setHint("User Email");
        etEmail.setPadding(dp(20), dp(16), dp(20), dp(8));

        new AlertDialog.Builder(this)
                .setTitle("Invite Co-Organizer")
                .setMessage("Enter the email of the user you want to invite as a co-organizer.")
                .setView(etEmail)
                .setPositiveButton("Invite", (d, w) -> {
                    String email = etEmail.getText().toString().trim();
                    if (!TextUtils.isEmpty(email)) inviteCoOrganizer(email);
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void inviteCoOrganizer(String email) {
        userRepo.getUserByEmail(email).addOnSuccessListener(qs -> {
            if (qs.isEmpty()) {
                Toast.makeText(this, "User not found with this email", Toast.LENGTH_SHORT).show();
                return;
            }
            DocumentSnapshot doc = qs.getDocuments().get(0);
            User targetUser = doc.toObject(User.class);
            if (targetUser == null) return;
            targetUser.setId(doc.getId());

            String currentUserId = session.getUserId();
            userRepo.getUserById(currentUserId).addOnSuccessListener(myDoc -> {
                User me = myDoc.toObject(User.class);
                String myName = (me != null) ? me.getName() : "An organizer";

                AppNotification notif = new AppNotification(
                        targetUser.getId(),
                        currentEvent.getId(),
                        currentEvent.getTitle(),
                        AppNotification.TYPE_CO_ORGANIZER,
                        "Co-Organizer Invitation",
                        myName + " has invited you to be a co-organizer for the event \"" + currentEvent.getTitle() + "\".",
                        true
                );
                notif.setSenderName(myName);

                notifRepo.createNotification(notif).addOnSuccessListener(v -> {
                    Toast.makeText(this, "Invitation sent to " + targetUser.getName(), Toast.LENGTH_SHORT).show();
                });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error finding user", Toast.LENGTH_SHORT).show();
        });
    }

    private void showInviteEntrantDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_search_user, null);
        EditText etSearch = view.findViewById(R.id.et_search_query);
        ProgressBar progressBar = view.findViewById(R.id.search_progress);
        RecyclerView rvResults = view.findViewById(R.id.rv_search_results);
        TextView tvNoResults = view.findViewById(R.id.tv_no_results);

        UserSearchAdapter searchAdapter = new UserSearchAdapter(user -> {
            confirmInvitation(user);
        });
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(searchAdapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton(R.string.btn_close, null)
                .create();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    performUserSearch(query, progressBar, rvResults, tvNoResults, searchAdapter);
                } else {
                    rvResults.setVisibility(View.GONE);
                    tvNoResults.setVisibility(View.GONE);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        dialog.show();
    }

    private void performUserSearch(String query, ProgressBar pb, RecyclerView rv, TextView tvNone, UserSearchAdapter adapter) {
        pb.setVisibility(View.VISIBLE);
        
        Task<QuerySnapshot> t1 = userRepo.getUserByEmail(query);
        Task<QuerySnapshot> t2 = userRepo.getUserByEmailOriginal(query);
        Task<QuerySnapshot> t3 = userRepo.searchByPhone(query);
        Task<QuerySnapshot> t4 = userRepo.searchByName(query);
        Task<QuerySnapshot> t5 = userRepo.searchByNameOriginal(query);

        Tasks.whenAllComplete(t1, t2, t3, t4, t5).addOnCompleteListener(all -> {
            pb.setVisibility(View.GONE);
            Set<User> uniqueUsers = new HashSet<>();
            
            for (Task<?> t : all.getResult()) {
                if (t.isSuccessful()) {
                    QuerySnapshot qs = (QuerySnapshot) t.getResult();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        User u = doc.toObject(User.class);
                        if (u != null) { u.setId(doc.getId()); uniqueUsers.add(u); }
                    }
                }
            }

            List<User> list = new ArrayList<>(uniqueUsers);
            adapter.setUsers(list);
            rv.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
            tvNone.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void confirmInvitation(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Invite Entrant")
                .setMessage("Invite " + user.getName() + " to the waiting list for this event?")
                .setPositiveButton("Invite", (d, w) -> inviteUserToWaitingList(user))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void inviteUserToWaitingList(User user) {
        regRepo.checkExistingRegistration(currentEvent.getId(), user.getId()).addOnSuccessListener(qs -> {
            if (!qs.isEmpty()) {
                Toast.makeText(this, "User is already registered for this event", Toast.LENGTH_SHORT).show();
                return;
            }

            Registration reg = new Registration(currentEvent.getId(), user.getId(), user.getName(), user.getEmail());
            reg.setStatus(Registration.STATUS_INVITED); // Set status to INVITED for private events
            regRepo.createRegistration(reg).addOnSuccessListener(v -> {
                // For private invites, we don't increment waiting list count until they accept
                
                // Send notification to user with action required
                AppNotification notif = new AppNotification(
                        user.getId(), currentEvent.getId(), currentEvent.getTitle(),
                        AppNotification.TYPE_INVITATION, "Private Event Invitation",
                        "You've been invited to join \"" + currentEvent.getTitle() + "\".",
                        true
                );
                notifRepo.createNotification(notif);

                Toast.makeText(this, "Invitation sent to " + user.getName(), Toast.LENGTH_SHORT).show();
                loadStats();
                loadTabData();
            });
        });
    }

    private void sendBulkNotification() {
        String msg = etNotifMsg.getText() != null ? etNotifMsg.getText().toString().trim() : "";
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = cgAudience.getCheckedChipId();
        String audience;
        if (checkedId == R.id.chip_selected_entrants)   audience = "selected";
        else if (checkedId == R.id.chip_cancelled_entrants) audience = "cancelled";
        else audience = "all";

        String currentUserId = session.getUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "Session error - please log in again", Toast.LENGTH_SHORT).show();
            return;
        }
        btnSendNotification.setEnabled(false);

        userRepo.getUserById(currentUserId)
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    String orgName = user != null ? user.getName() : "Organizer";
                    lotteryEngine.sendBulkNotification(currentEvent, audience, msg, orgName,
                            new LotteryEngine.LotteryCallback() {
                                @Override public void onSuccess(int sent, int x) {
                                    btnSendNotification.setEnabled(true);
                                    etNotifMsg.setText("");
                                    Toast.makeText(OrganizerEventManagementActivity.this,
                                            "Notification sent to " + sent + " entrant(s)",
                                            Toast.LENGTH_SHORT).show();
                                }
                                @Override public void onFailure(Exception e) {
                                    btnSendNotification.setEnabled(true);
                                    Toast.makeText(OrganizerEventManagementActivity.this,
                                            R.string.error_generic, Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }

    private void exportCsv() {
        regRepo.getRegistrationsByStatus(currentEvent.getId(), Registration.STATUS_ACCEPTED)
                .addOnSuccessListener(qs -> {
                    List<Registration> accepted = new ArrayList<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Registration r = doc.toObject(Registration.class);
                        if (r != null) accepted.add(r);
                    }
                    CsvExportHelper.exportRegistrations(this, currentEvent.getTitle(), accepted);
                });
    }

    private void showQrDialog() {
        if (currentEvent == null || currentEvent.getQrCodeContent() == null) return;
        android.graphics.Bitmap qr = QRCodeHelper.generateQRCode(currentEvent.getQrCodeContent(), 600, 600);
        if (qr == null) return;

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_qr_code);
        android.widget.ImageView ivQr = dialog.findViewById(R.id.iv_qr);
        TextView tvTitle = dialog.findViewById(R.id.tv_qr_title);
        ivQr.setImageBitmap(qr);
        tvTitle.setText(currentEvent.getTitle());
        dialog.findViewById(R.id.btn_close_qr).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private int safeInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private int dp(int val) {
        return Math.round(val * getResources().getDisplayMetrics().density);
    }
}
