package com.example.lotterappjava;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterappjava.databinding.ActivityMainBinding;

/**
 * The main activity of the application that serves as the entry point and hosts the navigation fragment.
 * This class follows the View component of the MVC design pattern, managing the overall window and 
 * navigation host for the application's fragments.
 *
 * Outstanding issues:
 * - None currently identified.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            // We removed the Toolbar and ActionBar setup to allow fragments to occupy the full screen.
            // Modern Android gesture navigation (sliding from the edge) will handle going back.
        }

        userController = new UserController();
        // User is created only after login or "Continue with this device" (see LoginFragment / AuthManager).
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}
