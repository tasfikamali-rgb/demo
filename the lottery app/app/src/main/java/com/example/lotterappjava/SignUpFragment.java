package com.example.lotterappjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterappjava.databinding.FragmentSignUpBinding;

/**
 * A Fragment that handles user registration.
 * This class follows the View component of the MVC design pattern, providing
 * the user interface for creating a new account with a specific role.
 *
 * Outstanding issues:
 * - Admin accounts cannot be created via this screen, which is intentional for security.
 */
public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;
    private final AuthManager authManager = new AuthManager();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide admin option from sign up - admins cannot sign up themselves
        binding.signupRoleAdmin.setVisibility(View.GONE);

        binding.signupCreateBtn.setOnClickListener(v -> attemptSignUp());
        binding.signupLoginLink.setOnClickListener(v ->
                NavHostFragment.findNavController(SignUpFragment.this).navigate(R.id.action_signUpFragment_to_loginFragment));
    }

    /**
     * Attempts to create a new user account with the provided details.
     */
    private void attemptSignUp() {
        String name = binding.signupName.getText() != null ? binding.signupName.getText().toString().trim() : "";
        String email = binding.signupEmail.getText() != null ? binding.signupEmail.getText().toString().trim() : "";
        String password = binding.signupPassword.getText() != null ? binding.signupPassword.getText().toString() : "";
        String role = getSelectedRole();

        binding.signupError.setVisibility(View.GONE);

        if (name.isEmpty()) {
            binding.signupError.setText(getString(R.string.error_name_required));
            binding.signupError.setVisibility(View.VISIBLE);
            return;
        }
        if (email.isEmpty() || password.isEmpty()) {
            binding.signupError.setText(getString(R.string.error_email_password_required));
            binding.signupError.setVisibility(View.VISIBLE);
            return;
        }
        if (password.length() < 6) {
            binding.signupError.setText(getString(R.string.error_password_length));
            binding.signupError.setVisibility(View.VISIBLE);
            return;
        }
        
        // Final safety check: no one can sign up as admin through this screen
        if (User.ROLE_ADMIN.equals(role)) {
            binding.signupError.setText("Admin accounts cannot be created via sign up.");
            binding.signupError.setVisibility(View.VISIBLE);
            return;
        }

        setLoading(true);
        authManager.signUpWithEmail(requireContext(), name, email, password, role, new AuthManager.AuthResultListener() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);
                if (user != null && user.getRole() != null) {
                    navigateToHomeByRole(user.getRole());
                } else {
                    binding.signupError.setVisibility(View.VISIBLE);
                    binding.signupError.setText(getString(R.string.error_unknown));
                }
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                binding.signupError.setText(message);
                binding.signupError.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Determines the role selected by the user in the sign-up form.
     * @return The selected role string.
     */
    private String getSelectedRole() {
        int checkedId = binding.signupRoleGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.signup_role_organizer) {
            return User.ROLE_ORGANIZER;
        }
        // Default to entrant. Admin is hidden.
        return User.ROLE_ENTRANT;
    }

    /**
     * Navigates to the corresponding home screen based on the user's role.
     * @param role The role of the user.
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
        NavHostFragment.findNavController(SignUpFragment.this).navigate(destId, null, options);
    }

    /**
     * Controls the visibility of the loading progress bar and button states.
     * @param loading Boolean indicating if a sign-up attempt is in progress.
     */
    private void setLoading(boolean loading) {
        binding.signupProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.signupCreateBtn.setEnabled(!loading);
        binding.signupLoginLink.setClickable(!loading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
