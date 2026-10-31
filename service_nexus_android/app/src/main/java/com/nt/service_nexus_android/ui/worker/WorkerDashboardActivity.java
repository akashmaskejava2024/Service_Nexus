package com.nt.service_nexus_android.ui.worker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.nt.service_nexus_android.data.local.SessionManager;
import com.nt.service_nexus_android.data.models.NearbyRequest;
import com.nt.service_nexus_android.data.remote.NetworkModule;
import com.nt.service_nexus_android.data.repository.AuthRepository;
import com.nt.service_nexus_android.data.repository.ServiceRequestRepository;
import com.nt.service_nexus_android.databinding.ActivityWorkerDashboardBinding;
import com.nt.service_nexus_android.ui.auth.LoginActivity;
import java.util.ArrayList;
import java.util.List;

public class WorkerDashboardActivity extends AppCompatActivity {
    private ActivityWorkerDashboardBinding binding;
    private WorkerViewModel viewModel;
    private RadarAdapter adapter;

    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            viewModel.loadRadarRequests();
            pollHandler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);
        ServiceRequestRepository repository = new ServiceRequestRepository(NetworkModule.getApiService(sessionManager), sessionManager);
        AuthRepository authRepository = new AuthRepository(NetworkModule.getApiService(sessionManager), sessionManager);
        viewModel = new WorkerViewModel(repository);

        setupRecyclerView();
        setupObservers();

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadRadarRequests());

        binding.btnLogout.setOnClickListener(v -> {
            authRepository.logout();
            Intent intent = new Intent(WorkerDashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finishAffinity();
        });
    }

    private void setupRecyclerView() {
        binding.rvRadar.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RadarAdapter(new RadarAdapter.OnRadarActionListener() {
            @Override
            public void onPlaceBid(NearbyRequest request) {
                BiddingBottomSheetDialog dialog = new BiddingBottomSheetDialog((amount, message) -> {
                    viewModel.submitBid(request.getId(), amount, message);
                });
                dialog.show(getSupportFragmentManager(), "BiddingDialog");
            }

            @Override
            public void onShowQr(NearbyRequest request) {
                Intent intent = new Intent(WorkerDashboardActivity.this, WorkerQrDisplayActivity.class);
                intent.putExtra("request_id", request.getId());
                startActivity(intent);
            }
        });
        binding.rvRadar.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        viewModel.getNearbyRequests().observe(this, requests -> {
            List<NearbyRequest> combined = new ArrayList<>();
            if (viewModel.getAcceptedJobs().getValue() != null) {
                combined.addAll(viewModel.getAcceptedJobs().getValue());
            }
            if (requests != null) {
                combined.addAll(requests);
            }
            adapter.setRequests(combined);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getBidSubmittedSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Bid submitted successfully!", Toast.LENGTH_SHORT).show();
                viewModel.resetSuccessState();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadRadarRequests();
        pollHandler.postDelayed(pollRunnable, 5000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollHandler.removeCallbacks(pollRunnable);
    }
}
