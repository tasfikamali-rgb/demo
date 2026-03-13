package com.example.lotterappjava;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterappjava.databinding.FragmentAdminHomeBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment representing the administrator's home screen.
 * This class follows the View component of the MVC design pattern, providing
 * the user interface for managing users, events, images, and system logs.
 *
 * Outstanding issues:
 * - None currently identified.
 */
public class AdminHomeFragment extends Fragment {

    private static final String TAG = "AdminHomeFragment";
    private FragmentAdminHomeBinding binding;
    private UserController userController;
    private EventController eventController;
    private NotificationController notificationController;
    private final AuthManager authManager = new AuthManager();

    private UserAdapter userAdapter;
    private EventAdapter eventAdapter;
    private ImageAdapter imageAdapter;
    private NotificationAdapter notificationAdapter;

    private List<User> userList = new ArrayList<>();
    private List<Event> eventList = new ArrayList<>();
    private List<String> imageUrlList = new ArrayList<>();
    private List<Notification> notificationList = new ArrayList<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        userController = new UserController();
        eventController = new EventController();
        notificationController = new NotificationController();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAdapters();
        setupSearch();

        binding.adminTopNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            binding.searchViewAdmin.setQuery("", false);

            if (itemId == R.id.nav_admin_users) {
                binding.searchViewAdmin.setVisibility(View.VISIBLE);
                loadUsers();
                return true;
            } else if (itemId == R.id.nav_admin_events) {
                binding.searchViewAdmin.setVisibility(View.VISIBLE);
                loadEvents();
                return true;
            } else if (itemId == R.id.nav_admin_images) {
                binding.searchViewAdmin.setVisibility(View.GONE);
                loadImages();
                return true;
            } else if (itemId == R.id.nav_admin_logs) {
                binding.searchViewAdmin.setVisibility(View.GONE);
                loadNotifications();
                return true;
            }
            return false;
        });

        binding.btnLogoutAdmin.setOnClickListener(v -> logout());


        binding.adminTopNav.setSelectedItemId(R.id.nav_admin_users);
    }

    /**
     * Handles the logout process for the administrator.
     */
    private void logout() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to sign out from Admin panel?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    authManager.signOut();
                    NavOptions options = new NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .build();
                    NavHostFragment.findNavController(AdminHomeFragment.this)
                            .navigate(R.id.loginFragment, null, options);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Initializes the adapters for users, events, images, and notifications.
     */
    private void setupAdapters() {
        userAdapter = new UserAdapter(userList, true);
        userAdapter.setOnDeleteClickListener(position -> {
            User userToDelete = userList.get(position);
            userController.deleteUser(userToDelete.getDeviceId(), aVoid -> {
                Toast.makeText(getContext(), R.string.user_deleted, Toast.LENGTH_SHORT).show();
                loadUsers();
            });
        });

        eventAdapter = new EventAdapter(eventList, true);
        eventAdapter.setOnDeleteClickListener(position -> {
            Event eventToDelete = eventList.get(position);
            deleteEvent(eventToDelete);
        });

        imageAdapter = new ImageAdapter(imageUrlList);
        imageAdapter.setOnDeleteClickListener(position -> {
            String imageUrlToDelete = imageUrlList.get(position);
            deleteImage(imageUrlToDelete);
        });

        notificationAdapter = new NotificationAdapter(notificationList);
    }

    /**
     * Configures the search view to filter the current list displayed in the recycler view.
     */
    private void setupSearch() {
        binding.searchViewAdmin.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                RecyclerView.Adapter<?> currentAdapter = binding.recyclerAdminContent.getAdapter();

                if (currentAdapter instanceof UserAdapter) {
                    ((UserAdapter) currentAdapter).getFilter().filter(newText);
                } else if (currentAdapter instanceof EventAdapter) {
                    ((EventAdapter) currentAdapter).getFilter().filter(newText);
                }
                return true;
            }
        });
    }

    /**
     * Loads all users from the database and updates the recycler view.
     */
    private void loadUsers() {
        userController.getAllUsers(users -> {
            userList.clear();
            userList.addAll(users);
            userAdapter.updateList(new ArrayList<>(userList));
            binding.recyclerAdminContent.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerAdminContent.setAdapter(userAdapter);
        });
    }

    /**
     * Loads all events from the database and updates the recycler view.
     */
    private void loadEvents() {
        eventController.getAllEvents(events -> {
            eventList.clear();
            eventList.addAll(events);
            eventAdapter.updateList(new ArrayList<>(eventList));
            binding.recyclerAdminContent.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerAdminContent.setAdapter(eventAdapter);
        });
    }

    /**
     * Loads all image URLs from the database and updates the recycler view with a grid layout.
     */
    private void loadImages() {
        Log.d(TAG, "Loading all images for admin...");
        eventController.getAllImageUrls(imageUrls -> {
            if (binding == null) return;
            
            Log.d(TAG, "Fetched " + imageUrls.size() + " images.");
            imageUrlList.clear();
            imageUrlList.addAll(imageUrls);

            // Correctly update the adapter with the new list
            imageAdapter.updateList(new ArrayList<>(imageUrlList));

            binding.recyclerAdminContent.setLayoutManager(new GridLayoutManager(getContext(), 2));
            binding.recyclerAdminContent.setAdapter(imageAdapter);
            
            if (imageUrls.isEmpty()) {
                Toast.makeText(getContext(), "No images found in database", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Loaded " + imageUrls.size() + " images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads all administrator notification logs and updates the recycler view.
     */
    private void loadNotifications() {
        notificationController.getAdminLogs(notifications -> {
            notificationList.clear();
            notificationList.addAll(notifications);
            binding.recyclerAdminContent.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerAdminContent.setAdapter(notificationAdapter);
            notificationAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Deletes a specific event, including its poster from Firebase Storage if it exists.
     * @param event The event object to delete.
     */
    private void deleteEvent(Event event) {
        if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(event.getPosterUrl());
            photoRef.delete().addOnSuccessListener(aVoid -> {
                deleteEventFromFirestore(event.getEventId());
            }).addOnFailureListener(exception -> {
                deleteEventFromFirestore(event.getEventId());
            });
        } else {
            deleteEventFromFirestore(event.getEventId());
        }
    }

    /**
     * Internal method to delete an event record from Firestore.
     * @param eventId The ID of the event to delete.
     */
    private void deleteEventFromFirestore(String eventId) {
        eventController.deleteEvent(eventId, success -> {
            if (success) {
                Toast.makeText(getContext(), R.string.event_deleted, Toast.LENGTH_SHORT).show();
                loadEvents();
            } else {
                Toast.makeText(getContext(), R.string.failed_to_delete_event, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Deletes a specific image from Firebase Storage and updates database references.
     * @param imageUrl The URL of the image to delete.
     */
    private void deleteImage(String imageUrl) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to delete this image from the database?")
            .setPositiveButton("Yes", (dialog, which) -> {
                try {
                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    photoRef.delete().addOnCompleteListener(task -> {
                        eventController.deleteEventImage(imageUrl, success -> {
                            if (success) {
                                Toast.makeText(getContext(), R.string.image_deleted, Toast.LENGTH_SHORT).show();
                                loadImages();
                            } else {
                                Toast.makeText(getContext(), R.string.failed_to_update_event, Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                } catch (Exception e) {
                    eventController.deleteEventImage(imageUrl, success -> loadImages());
                }
            })
            .setNegativeButton("No", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
