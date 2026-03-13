package com.example.lotterappjava;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterappjava.databinding.FragmentNotificationsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying a list of notifications for the current user.
 * This class follows the View component of the MVC design pattern, interacting with the
 * NotificationController to listen for real-time updates.
 *
 * Outstanding issues:
 * - None currently identified.
 */
public class NotificationFragment extends Fragment {

    private static final String TAG = "NotificationFragment";
    private FragmentNotificationsBinding binding;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private NotificationController notificationController;
    private String userId;
    private ListenerRegistration registration;

    /**
     * Initializes the controller and retrieves the current user's unique identifier.
     * 
     * @param savedInstanceState If the fragment is being re-created from a previous saved state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationController = new NotificationController();
        
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null) {
            userId = fbUser.getUid();
        } else {
            userId = DeviceIdManager.getDeviceId(requireContext());
        }
        Log.d(TAG, "NotificationFragment initialized for userId: " + userId);
    }

    /**
     * Inflates the layout for this fragment using View Binding.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous state.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up the RecyclerView and starts listening for notification updates.
     *
     * @param view The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous state.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        adapter = new NotificationAdapter(notificationList);
        binding.recyclerNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerNotifications.setAdapter(adapter);

        startNotificationListener();
    }

    /**
     * Establishes a real-time listener for notifications associated with the current user.
     * Updates the UI dynamically as notifications are received or changed.
     */
    private void startNotificationListener() {
        if (registration != null) registration.remove();
        
        Log.d(TAG, "Starting real-time listener for userId: " + userId);
        registration = notificationController.listenForUserNotifications(userId, notifications -> {
            if (binding == null) return;
            
            Log.d(TAG, "Received " + notifications.size() + " notifications for user");
            notificationList.clear();
            notificationList.addAll(notifications);
            adapter.notifyDataSetChanged();

            if (notificationList.isEmpty()) {
                binding.textNoNotifications.setVisibility(View.VISIBLE);
                binding.recyclerNotifications.setVisibility(View.GONE);
            } else {
                binding.textNoNotifications.setVisibility(View.GONE);
                binding.recyclerNotifications.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Cleans up the listener and bindings when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (registration != null) registration.remove();
        binding = null;
    }
}
