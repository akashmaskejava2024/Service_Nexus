package com.nt.service_nexus_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.nt.service_nexus_android.data.local.SessionManager;
import com.nt.service_nexus_android.data.models.Role;
import com.nt.service_nexus_android.data.models.User;
import com.nt.service_nexus_android.data.remote.NetworkModule;
import com.nt.service_nexus_android.data.repository.AuthRepository;
import com.nt.service_nexus_android.databinding.ActivityLoginBinding;
import com.nt.service_nexus_android.ui.customer.CustomerMainActivity;
import com.nt.service_nexus_android.ui.worker.WorkerDashboardActivity;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);
        AuthRepository authRepository = new AuthRepository(NetworkModule.getApiService(sessionManager), sessionManager);
        viewModel = new AuthViewModel(authRepository);

        setupObservers();

        binding.btnLogin.setOnClickListener(v -> {
            String uname = binding.etUsername.getText() != null ? binding.etUsername.getText().toString() : "";
            String pass = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
            viewModel.login(uname, pass);
        });

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.btnLogin.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getAuthSuccessUser().observe(this, user -> {
            if (user != null) {
                navigateToUserHome(user);
            }
        });
    }

    private void navigateToUserHome(User user) {
        Intent intent;
        if (user.getRole() == Role.WORKER) {
            intent = new Intent(this, WorkerDashboardActivity.class);
        } else {
            intent = new Intent(this, CustomerMainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
