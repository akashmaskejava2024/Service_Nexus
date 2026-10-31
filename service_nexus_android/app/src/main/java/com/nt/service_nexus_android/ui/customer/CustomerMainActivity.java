package com.nt.service_nexus_android.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.nt.service_nexus_android.data.local.SessionManager;
import com.nt.service_nexus_android.data.remote.NetworkModule;
import com.nt.service_nexus_android.data.repository.AuthRepository;
import com.nt.service_nexus_android.data.repository.ServiceRequestRepository;
import com.nt.service_nexus_android.databinding.ActivityCustomerMainBinding;
import com.nt.service_nexus_android.ui.auth.LoginActivity;

public class CustomerMainActivity extends AppCompatActivity {
    private ActivityCustomerMainBinding binding;
    private CustomerViewModel viewModel;
    private RequestAdapter adapter;
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            viewModel.loadCustomerRequests();
            pollHandler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);
        ServiceRequestRepository repository = new ServiceRequestRepository(NetworkModule.getApiService(sessionManager), sessionManager);
        AuthRepository authRepository = new AuthRepository(NetworkModule.getApiService(sessionManager), sessionManager);
        viewModel = new CustomerViewModel(repository, authRepository);

        setupRecyclerView();
        setupObservers();

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadCustomerRequests());

        binding.fabNewRequest.setOnClickListener(v -> {
            startActivity(new Intent(CustomerMainActivity.this, MapPinDropActivity.class));
        });

        binding.btnLogout.setOnClickListener(v -> {
            authRepository.logout();
            Intent intent = new Intent(CustomerMainActivity.this, LoginActivity.class);
            startActivity(intent);
            finishAffinity();
        });
    }

    private void setupRecyclerView() {
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestAdapter(new RequestAdapter.OnRequestActionListener() {
            @Override
            public void onAcceptBid(int bidId) {
                viewModel.acceptBid(bidId);
            }

            @Override
            public void onScanQr(int requestId) {
                Intent intent = new Intent(CustomerMainActivity.this, QrScannerActivity.class);
                intent.putExtra("request_id", requestId);
                startActivity(intent);
            }
        });
        binding.rvRequests.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        viewModel.getRequests().observe(this, requests -> {
            if (requests == null || requests.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.rvRequests.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.rvRequests.setVisibility(View.VISIBLE);
                adapter.setRequests(requests);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadCustomerRequests();
        pollHandler.postDelayed(pollRunnable, 5000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollHandler.removeCallbacks(pollRunnable);
    }
}
