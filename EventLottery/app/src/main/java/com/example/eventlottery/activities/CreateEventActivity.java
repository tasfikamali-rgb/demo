package com.example.eventlottery.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.repositories.EventRepository;
import com.example.eventlottery.repositories.UserRepository;
import com.example.eventlottery.utils.DateUtils;
import com.example.eventlottery.utils.QRCodeHelper;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity {

    private SessionManager session;
    private EventRepository eventRepo;
    private UserRepository userRepo;

    private TextInputLayout tilTitle, tilDescription, tilLocation, tilCapacity;
    private TextInputEditText etTitle, etDescription, etLocation, etTags;
    private TextInputEditText etEventStart, etEventEnd, etRegOpen, etRegClose;
    private TextInputEditText etCapacity, etMaxWl, etPrice;
    private TextView tvPosterName;
    private MaterialButton btnUploadPoster;
    private SwitchMaterial swPrivate, swGeolocation;
    private MaterialButton btnSave;

    private String base64Poster = null;
    private String eventId = null;
    private Event existingEvent = null;

    private final Calendar calEventStart = Calendar.getInstance();
    private final Calendar calEventEnd   = Calendar.getInstance();
    private final Calendar calRegOpen    = Calendar.getInstance();
    private final Calendar calRegClose   = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    processImage(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        session   = new SessionManager(this);
        eventRepo = new EventRepository();
        userRepo  = new UserRepository();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        bindViews();
        setupDatePickers();

        eventId = getIntent().getStringExtra("event_id");
        if (eventId != null) {
            setTitle("Edit Event");
            btnSave.setText("Save Changes");
            loadExistingEvent();
        } else {
            setTitle("Create Event");
            btnSave.setText("Create Event");
        }

        btnUploadPoster.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void loadExistingEvent() {
        eventRepo.getEventById(eventId).addOnSuccessListener(doc -> {
            existingEvent = doc.toObject(Event.class);
            if (existingEvent != null) {
                existingEvent.setId(doc.getId());
                populateFields();
            }
        });
    }

    private void populateFields() {
        etTitle.setText(existingEvent.getTitle());
        etDescription.setText(existingEvent.getDescription());
        etLocation.setText(existingEvent.getLocation());
        if (existingEvent.getTags() != null) {
            etTags.setText(TextUtils.join(", ", existingEvent.getTags()));
        }
        
        calEventStart.setTimeInMillis(existingEvent.getEventStartDate());
        etEventStart.setText(sdf.format(calEventStart.getTime()));
        
        calEventEnd.setTimeInMillis(existingEvent.getEventEndDate());
        etEventEnd.setText(sdf.format(calEventEnd.getTime()));
        
        calRegOpen.setTimeInMillis(existingEvent.getRegistrationOpenDate());
        etRegOpen.setText(sdf.format(calRegOpen.getTime()));
        
        calRegClose.setTimeInMillis(existingEvent.getRegistrationCloseDate());
        etRegClose.setText(sdf.format(calRegClose.getTime()));
        
        etCapacity.setText(String.valueOf(existingEvent.getCapacity()));
        etMaxWl.setText(String.valueOf(existingEvent.getMaxWaitingList()));
        etPrice.setText(String.valueOf(existingEvent.getPrice()));
        
        swPrivate.setChecked(existingEvent.isPrivate());
        swGeolocation.setChecked(existingEvent.isRequireGeolocation());
        
        if (existingEvent.getPosterUrl() != null && !existingEvent.getPosterUrl().isEmpty()) {
            base64Poster = existingEvent.getPosterUrl();
            tvPosterName.setText("Current poster kept");
        }
    }

    private void processImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            
            // Resize image to keep Firestore document size under 1MB limit
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] bytes = baos.toByteArray();
            
            base64Poster = Base64.encodeToString(bytes, Base64.DEFAULT);
            tvPosterName.setText("New image selected");
        } catch (Exception e) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindViews() {
        tilTitle       = findViewById(R.id.til_title);
        tilDescription = findViewById(R.id.til_description);
        tilLocation    = findViewById(R.id.til_location);
        tilCapacity    = findViewById(R.id.til_capacity);

        etTitle       = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etLocation    = findViewById(R.id.et_location);
        etTags        = findViewById(R.id.et_tags);
        etEventStart  = findViewById(R.id.et_event_start);
        etEventEnd    = findViewById(R.id.et_event_end);
        etRegOpen     = findViewById(R.id.et_reg_open);
        etRegClose    = findViewById(R.id.et_reg_close);
        etCapacity    = findViewById(R.id.et_capacity);
        etMaxWl       = findViewById(R.id.et_max_wl);
        etPrice       = findViewById(R.id.et_price);
        
        btnUploadPoster = findViewById(R.id.btn_upload_poster);
        tvPosterName    = findViewById(R.id.tv_poster_name);
        
        swPrivate     = findViewById(R.id.sw_private);
        swGeolocation = findViewById(R.id.sw_geolocation);
        btnSave       = findViewById(R.id.btn_create_event);
    }

    private void setupDatePickers() {
        etEventStart.setOnClickListener(v -> pickDateTime(calEventStart, etEventStart));
        etEventEnd.setOnClickListener(v -> pickDateTime(calEventEnd, etEventEnd));
        etRegOpen.setOnClickListener(v -> pickDateTime(calRegOpen, etRegOpen));
        etRegClose.setOnClickListener(v -> pickDateTime(calRegClose, etRegClose));
    }

    private void pickDateTime(Calendar cal, TextInputEditText target) {
        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    cal.set(year, month, day);
                    new TimePickerDialog(this,
                            (tp, hour, minute) -> {
                                cal.set(Calendar.HOUR_OF_DAY, hour);
                                cal.set(Calendar.MINUTE, minute);
                                target.setText(sdf.format(cal.getTime()));
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE), false).show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void validateAndSave() {
        String title = getText(etTitle);
        String desc  = getText(etDescription);
        String loc   = getText(etLocation);
        String cap   = getText(etCapacity);

        boolean valid = true;
        if (TextUtils.isEmpty(title)) { tilTitle.setError(getString(R.string.error_title_required)); valid = false; }
        else tilTitle.setError(null);
        if (TextUtils.isEmpty(loc))   { tilLocation.setError(getString(R.string.error_location_required)); valid = false; }
        else tilLocation.setError(null);
        if (TextUtils.isEmpty(cap))   { tilCapacity.setError(getString(R.string.error_capacity_required)); valid = false; }
        else tilCapacity.setError(null);
        if (getText(etEventStart).isEmpty() || getText(etEventEnd).isEmpty() ||
            getText(etRegOpen).isEmpty()    || getText(etRegClose).isEmpty()) {
            Toast.makeText(this, R.string.error_dates_required, Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if (!valid) return;

        int capacity   = safeInt(cap, 10);
        int maxWl      = safeInt(getText(etMaxWl), 0);
        double price   = safeDouble(getText(etPrice), 0);
        boolean priv   = swPrivate.isChecked();
        boolean geo    = swGeolocation.isChecked();

        String currentUserId = session.getUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "Session error — please log in again", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving…");

        if (eventId != null) {
            updateEvent(title, desc, loc, capacity, maxWl, price, priv, geo);
        } else {
            createNewEvent(title, desc, loc, currentUserId, capacity, maxWl, price, priv, geo);
        }
    }

    private void updateEvent(String title, String desc, String loc, int capacity, int maxWl, double price, boolean priv, boolean geo) {
        existingEvent.setTitle(title);
        existingEvent.setDescription(desc);
        existingEvent.setLocation(loc);
        existingEvent.setEventStartDate(calEventStart.getTimeInMillis());
        existingEvent.setEventEndDate(calEventEnd.getTimeInMillis());
        existingEvent.setRegistrationOpenDate(calRegOpen.getTimeInMillis());
        existingEvent.setRegistrationCloseDate(calRegClose.getTimeInMillis());
        existingEvent.setCapacity(capacity);
        existingEvent.setMaxWaitingList(maxWl);
        existingEvent.setPrice(price);
        existingEvent.setPrivate(priv);
        existingEvent.setRequireGeolocation(geo);
        existingEvent.setPosterUrl(base64Poster);

        String tagsStr = getText(etTags);
        if (!tagsStr.isEmpty()) {
            existingEvent.setTags(Arrays.asList(tagsStr.split(",\\s*")));
        } else {
            existingEvent.setTags(Arrays.asList());
        }

        eventRepo.updateEvent(existingEvent).addOnSuccessListener(v -> {
            Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            btnSave.setEnabled(true);
            btnSave.setText("Save Changes");
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
        });
    }

    private void createNewEvent(String title, String desc, String loc, String currentUserId, int capacity, int maxWl, double price, boolean priv, boolean geo) {
        userRepo.getUserById(currentUserId).addOnSuccessListener(doc -> {
            com.example.eventlottery.models.User user =
                    doc.toObject(com.example.eventlottery.models.User.class);
            String orgName = user != null ? user.getName() : "Organizer";

            Event event = new Event(title, desc, loc,
                    currentUserId, orgName,
                    calEventStart.getTimeInMillis(), calEventEnd.getTimeInMillis(),
                    calRegOpen.getTimeInMillis(), calRegClose.getTimeInMillis(),
                    capacity, maxWl, price,
                    base64Poster,
                    priv, geo);

            String tagsStr = getText(etTags);
            if (!tagsStr.isEmpty()) {
                event.setTags(Arrays.asList(tagsStr.split(",\\s*")));
            }

            eventRepo.createEvent(event).addOnSuccessListener(ref -> {
                String newEventId = ref.getId();
                String deepLink = QRCodeHelper.buildEventDeepLink(newEventId);
                eventRepo.setQrCodeContent(newEventId, deepLink)
                        .addOnSuccessListener(v -> {
                            Toast.makeText(this, R.string.event_created_success, Toast.LENGTH_SHORT).show();
                            finish();
                        });
            }).addOnFailureListener(e -> {
                btnSave.setEnabled(true);
                btnSave.setText(R.string.create_event);
                Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            });
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private int safeInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private double safeDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }
}
