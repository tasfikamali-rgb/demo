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

/**
 * Fragment that displays a list of entrants for a specific event.
 * It provides filtering options (All, Waiting, Selected) and allows the organizer
 * to send group notifications to the current list of users.
 * This class follows the View component of the MVC design pattern.
 *
 * Outstanding issues:
 * - Large lists of entrants may need pagination for performance.
 */
public class OrganizerEntrantsFragment extends Fragment {

    private FragmentOrganizerEntrantsBinding binding;
    private String eventId;
    private EventController eventController;
    private NotificationController notificationController;
    private List<User> currentDisplayList = new ArrayList<>();
    private UserAdapter adapter;
    private String currentFilter = "all";
    private String organizerId;

    /**
     * Retrieves the event ID from arguments and initializes controllers.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state.
     */
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

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizerEntrantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes UI components, sets up filters, and loads entrant data.
     *
     * @param view The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous state.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFilters();

        binding.btnNotifyGroup.setOnClickListener(v -> showNotificationDialog());

        loadData();
    }

    /**
     * Configures the RecyclerView with a UserAdapter.
     */
    private void setupRecyclerView() {
        adapter = new UserAdapter(currentDisplayList, false);
        binding.recyclerEntrants.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerEntrants.setAdapter(adapter);
    }

    /**
     * Sets up click listeners for the filter chips.
     */
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

    /**
     * Updates the UI styling of the filter chips and the notification button text based on the current filter.
     */
    private void updateFilterUI() {
        resetFilterStyle(binding.filterAll);
        resetFilterStyle(binding.filterWaitlist);
        resetFilterStyle(binding.filterSelected);

        TextView selected = null;
        String btnText = getString(R.string.notify_all);
        
        switch (currentFilter) {
            case "all":
                selected = binding.filterAll;
                btnText = getString(R.string.notify_all);
                break;
            case "waiting":
                selected = binding.filterWaitlist;
                btnText = getString(R.string.notify_waiting);
                break;
            case "invited":
                selected = binding.filterSelected;
                btnText = getString(R.string.notify_selected);
                break;
        }

        if (selected != null) {
            selected.setBackgroundResource(R.drawable.chip_background_selected);
            selected.setTextColor(Color.WHITE);
            binding.btnNotifyGroup.setText(btnText);
        }
    }

    /**
     * Resets a TextView's background and text color to the default unselected state.
     *
     * @param textView The TextView to reset.
     */
    private void resetFilterStyle(TextView textView) {
        textView.setBackgroundResource(R.drawable.chip_background);
        textView.setTextColor(Color.parseColor("#4B5563"));
    }

    /**
     * Fetches entrant data from the EventController based on the current filter.
     */
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

    /**
     * Displays a dialog to the organizer to enter a notification message for the current group.
     */
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
                Toast.makeText(getContext(), getString(R.string.please_enter_a_message), Toast.LENGTH_SHORT).show();
                return;
            }

            notificationController.sendGroupNotification(eventId, organizerId, currentFilter, message, new ArrayList<>(currentDisplayList));
            String statusMsg = String.format(getString(R.string.notification_sent_to), currentFilter);
            Toast.makeText(getContext(), statusMsg, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Cleans up the binding when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
