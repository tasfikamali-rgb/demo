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

public class NotificationFragment extends Fragment {

    private static final String TAG = "NotificationFragment";
    private FragmentNotificationsBinding binding;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private NotificationController notificationController;
    private String userId;
    private ListenerRegistration registration;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationController = new NotificationController();
        
        // Consistent ID resolution: Use Firebase UID if signed in, otherwise Device ID
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null) {
            userId = fbUser.getUid();
        } else {
            userId = DeviceIdManager.getDeviceId(requireContext());
        }
        Log.d(TAG, "NotificationFragment initialized for userId: " + userId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        adapter = new NotificationAdapter(notificationList);
        binding.recyclerNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerNotifications.setAdapter(adapter);

        startNotificationListener();
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (registration != null) registration.remove();
        binding = null;
    }
}
