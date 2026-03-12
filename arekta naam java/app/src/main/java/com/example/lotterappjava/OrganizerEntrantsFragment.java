package com.example.lotterappjava;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterappjava.databinding.FragmentOrganizerEntrantsBinding;

import java.util.ArrayList;
import java.util.List;

public class OrganizerEntrantsFragment extends Fragment {

    private FragmentOrganizerEntrantsBinding binding;
    private String eventId;
    private EventController eventController;
    private NotificationController notificationController;
    private List<User> currentDisplayList = new ArrayList<>();
    private UserAdapter adapter;
    private String currentFilter = "all";
    private String organizerId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        eventController = new EventController();
        notificationController = new NotificationController();
        organizerId = DeviceIdManager.getDeviceId(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizerEntrantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFilters();

        binding.btnNotifyGroup.setOnClickListener(v -> showNotificationDialog());

        loadData();
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(currentDisplayList, false);
        binding.recyclerEntrants.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerEntrants.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.filterAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterUI();
            loadData();
        });

        binding.filterWaitlist.setOnClickListener(v -> {
            currentFilter = "waiting";
            updateFilterUI();
            loadData();
        });

        binding.filterSelected.setOnClickListener(v -> {
            currentFilter = "invited";
            updateFilterUI();
            loadData();
        });

        updateFilterUI();
    }

    private void updateFilterUI() {
        resetFilterStyle(binding.filterAll);
        resetFilterStyle(binding.filterWaitlist);
        resetFilterStyle(binding.filterSelected);

        TextView selected = null;
        String btnText = "Notify All";
        
        switch (currentFilter) {
            case "all":
                selected = binding.filterAll;
                btnText = "Notify All";
                break;
            case "waiting":
                selected = binding.filterWaitlist;
                btnText = "Notify WAITING";
                break;
            case "invited":
                selected = binding.filterSelected;
                btnText = "Notify SELECTED";
                break;
        }

        if (selected != null) {
            selected.setBackgroundResource(R.drawable.chip_background_selected);
            selected.setTextColor(Color.WHITE);
            binding.btnNotifyGroup.setText(btnText);
        }
    }

    private void resetFilterStyle(TextView textView) {
        textView.setBackgroundResource(R.drawable.chip_background);
        textView.setTextColor(Color.parseColor("#4B5563"));
    }

    private void loadData() {
        if (currentFilter.equals("all")) {
            eventController.getEntrantsForEvent(eventId, entrants -> {
                if (binding == null) return;
                currentDisplayList.clear();
                currentDisplayList.addAll(entrants);
                adapter.updateList(currentDisplayList);
            });
        } else {
            eventController.getEntrantsWithStatus(eventId, currentFilter, participants -> {
                if (binding == null) return;
                currentDisplayList.clear();
                for (EventController.Participant p : participants) {
                    currentDisplayList.add(p.getUser());
                }
                adapter.updateList(currentDisplayList);
            });
        }
    }

    private void showNotificationDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_send_notification, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText editMessage = dialogView.findViewById(R.id.edit_notification_message);
        Button btnSend = dialogView.findViewById(R.id.btn_send);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String message = editMessage.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            notificationController.sendGroupNotification(eventId, organizerId, currentFilter, message, new ArrayList<>(currentDisplayList));
            Toast.makeText(getContext(), "Notification sent to " + currentFilter + " group", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
