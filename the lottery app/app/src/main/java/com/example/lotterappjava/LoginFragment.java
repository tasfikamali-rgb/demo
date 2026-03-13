package com.example.lotterappjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterappjava.databinding.FragmentLoginBinding;

/**
 * A Fragment that handles user login and authentication.
 * This class follows the View component of the MVC design pattern, providing
 * the user interface for email/password login or device-based identification.
 *
 * Outstanding issues:
 * - Error messages could be more user-friendly and localized.
 */
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private final AuthManager authManager = new AuthManager();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If already signed in, go to the appropriate home
        authManager.getCurrentUserIfSignedIn(requireContext(), user -> {
            if (user != null && user.getRole() != null) {
                navigateToHomeByRole(user.getRole());
                return;
            }
            setupListeners();
        });
    }

    /**
     * Initializes click listeners for login and sign-up buttons.
     */
    private void setupListeners() {
        binding.loginSignInBtn.setOnClickListener(v -> attemptSignIn());
        binding.loginSignUpLink.setOnClickListener(v ->
                NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_loginFragment_to_signUpFragment));
        binding.loginContinueDeviceBtn.setOnClickListener(v -> attemptContinueWithDevice());
    }

    /**
     * Attempts to sign in with the provided email and password.
     */
    private void attemptSignIn() {
        String email = binding.loginEmail.getText() != null ? binding.loginEmail.getText().toString().trim() : "";
        String password = binding.loginPassword.getText() != null ? binding.loginPassword.getText().toString() : "";

        binding.loginError.setVisibility(View.GONE);
        if (email.isEmpty() || password.isEmpty()) {
            binding.loginError.setText(getString(R.string.error_email_password_required));
            binding.loginError.setVisibility(View.VISIBLE);
            return;
        }

        setLoading(true);
        authManager.signInWithEmail(requireContext(), email, password, new AuthManager.AuthResultListener() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);
                if (user != null && user.getRole() != null) {
                    navigateToHomeByRole(user.getRole());
                } else {
                    binding.loginError.setText(getString(R.string.error_unknown));
                    binding.loginError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                binding.loginError.setText(message);
                binding.loginError.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Attempts to continue as an entrant using only the device ID.
     */
    private void attemptContinueWithDevice() {
        binding.loginError.setVisibility(View.GONE);
        setLoading(true);
        authManager.continueWithDevice(requireContext(), new AuthManager.AuthResultListener() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);
                if (user != null) {
                    navigateToHomeByRole(User.ROLE_ENTRANT);
                } else {
                    binding.loginError.setText(getString(R.string.error_unknown));
                    binding.loginError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                binding.loginError.setText(message);
                binding.loginError.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Navigates to the corresponding home screen based on the user's role.
     *
     * @param role The role of the user (entrant, organizer, or admin).
     */
    private void navigateToHomeByRole(String role) {
        int destId;
        switch (role != null ? role : "") {
            case User.ROLE_ORGANIZER:
                destId = R.id.organizerHomeFragment;
                break;
            case User.ROLE_ADMIN:
                destId = R.id.adminHomeFragment;
                break;
            default:
                destId = R.id.entrantHomeFragment;
                break;
        }
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .build();
        NavHostFragment.findNavController(LoginFragment.this).navigate(destId, null, options);
    }

    /**
     * Controls the visibility of the loading progress bar and button states.
     *
     * @param loading Boolean indicating if a login attempt is in progress.
     */
    private void setLoading(boolean loading) {
        binding.loginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.loginSignInBtn.setEnabled(!loading);
        binding.loginContinueDeviceBtn.setEnabled(!loading);
        binding.loginSignUpLink.setClickable(!loading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
