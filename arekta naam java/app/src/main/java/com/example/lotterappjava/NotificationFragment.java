package com.example.lotterappjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterappjava.databinding.FragmentNotificationsBinding;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private NotificationController notificationController;
    private String userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationController = new NotificationController();
        userId = DeviceIdManager.getDeviceId(requireContext());
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

        fetchNotifications();
    }

    private void fetchNotifications() {
        notificationController.getNotificationsForUser(userId, notifications -> {
            if (binding == null) return;
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
        binding = null;
    }
}
