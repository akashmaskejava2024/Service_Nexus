package com.nt.service_nexus_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.nt.service_nexus_android.data.local.SessionManager;
import com.nt.service_nexus_android.data.models.Role;
import com.nt.service_nexus_android.data.models.TradeCategory;
import com.nt.service_nexus_android.data.models.User;
import com.nt.service_nexus_android.data.remote.NetworkModule;
import com.nt.service_nexus_android.data.repository.AuthRepository;
import com.nt.service_nexus_android.databinding.ActivityRegisterBinding;
import com.nt.service_nexus_android.ui.customer.CustomerMainActivity;
import com.nt.service_nexus_android.ui.worker.WorkerDashboardActivity;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;
    private boolean isWorker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);
        AuthRepository authRepository = new AuthRepository(NetworkModule.getApiService(sessionManager), sessionManager);
        viewModel = new AuthViewModel(authRepository);

        setupCategoryDropdown();
        setupToggleGroup();
        setupObservers();

        binding.btnRegister.setOnClickListener(v -> {
            String uname = binding.etUsername.getText() != null ? binding.etUsername.getText().toString() : "";
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString() : "";
            String pass = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";

            if (isWorker) {
                String catStr = binding.actvCategory.getText().toString();
                TradeCategory category = TradeCategory.OTHER;
                for (TradeCategory tc : TradeCategory.values()) {
                    if (tc.getDisplayName().equalsIgnoreCase(catStr)) {
                        category = tc;
                        break;
                    }
                }
                viewModel.registerWorker(uname, email, pass, category, 19.0760, 72.8777);
            } else {
                viewModel.registerCustomer(uname, email, pass);
            }
        });

        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void setupCategoryDropdown() {
        String[] categories = new String[TradeCategory.values().length];
        for (int i = 0; i < TradeCategory.values().length; i++) {
            categories[i] = TradeCategory.values()[i].getDisplayName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        binding.actvCategory.setAdapter(adapter);
    }

    private void setupToggleGroup() {
        binding.btnRoleCustomer.setOnClickListener(v -> {
            isWorker = false;
            binding.tilCategory.setVisibility(View.GONE);
        });

        binding.btnRoleWorker.setOnClickListener(v -> {
            isWorker = true;
            binding.tilCategory.setVisibility(View.VISIBLE);
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.btnRegister.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getAuthSuccessUser().observe(this, user -> {
            if (user != null) {
                Intent intent = user.getRole() == Role.WORKER ?
                        new Intent(this, WorkerDashboardActivity.class) :
                        new Intent(this, CustomerMainActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });
    }
}
