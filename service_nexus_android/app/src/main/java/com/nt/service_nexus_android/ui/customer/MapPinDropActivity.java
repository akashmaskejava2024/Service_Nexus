package com.nt.service_nexus_android.ui.customer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.nt.service_nexus_android.data.local.SessionManager;
import com.nt.service_nexus_android.data.remote.NetworkModule;
import com.nt.service_nexus_android.data.repository.AuthRepository;
import com.nt.service_nexus_android.data.repository.ServiceRequestRepository;
import com.nt.service_nexus_android.databinding.ActivityMapPinDropBinding;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class MapPinDropActivity extends AppCompatActivity {
    private ActivityMapPinDropBinding binding;
    private CustomerViewModel viewModel;
    private Marker mapMarker;
    private Uri selectedPhotoUri;

    private final ActivityResultLauncher<Intent> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedPhotoUri = result.getData().getData();
                    if (selectedPhotoUri != null) {
                        binding.ivSelectedPhoto.setVisibility(View.VISIBLE);
                        binding.ivSelectedPhoto.setImageURI(selectedPhotoUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapPinDropBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);
        ServiceRequestRepository repository = new ServiceRequestRepository(NetworkModule.getApiService(sessionManager), sessionManager);
        AuthRepository authRepository = new AuthRepository(NetworkModule.getApiService(sessionManager), sessionManager);
        viewModel = new CustomerViewModel(repository, authRepository);

        setupToolbar();
        setupMapView();
        setupObservers();

        binding.btnAttachPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            photoPickerLauncher.launch(intent);
        });

        binding.btnSubmitRequest.setOnClickListener(v -> {
            String desc = binding.etDescription.getText() != null ? binding.etDescription.getText().toString() : "";
            String photoUriStr = selectedPhotoUri != null ? selectedPhotoUri.toString() : null;
            viewModel.createServiceRequest(this, desc, photoUriStr);
        });
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupMapView() {
        binding.mapView.setMultiTouchControls(true);
        GeoPoint startPoint = new GeoPoint(viewModel.getSelectedLatitude(), viewModel.getSelectedLongitude());
        binding.mapView.getController().setZoom(15.0);
        binding.mapView.getController().setCenter(startPoint);

        mapMarker = new Marker(binding.mapView);
        mapMarker.setPosition(startPoint);
        mapMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapMarker.setTitle("Selected Location");
        binding.mapView.getOverlays().add(mapMarker);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                mapMarker.setPosition(p);
                binding.mapView.invalidate();
                viewModel.updateMapLocation(p.getLatitude(), p.getLongitude());
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) { return false; }
        });
        binding.mapView.getOverlays().add(0, mapEventsOverlay);
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.btnSubmitRequest.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getRequestCreatedSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Service request created successfully!", Toast.LENGTH_SHORT).show();
                viewModel.resetSuccessState();
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }
}
