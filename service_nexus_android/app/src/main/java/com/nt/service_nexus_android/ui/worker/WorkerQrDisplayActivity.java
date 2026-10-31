package com.nt.service_nexus_android.ui.worker;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nt.service_nexus_android.data.local.SessionManager;
import com.nt.service_nexus_android.data.remote.NetworkModule;
import com.nt.service_nexus_android.data.repository.ServiceRequestRepository;
import com.nt.service_nexus_android.databinding.ActivityWorkerQrDisplayBinding;

public class WorkerQrDisplayActivity extends AppCompatActivity {
    private ActivityWorkerQrDisplayBinding binding;
    private WorkerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkerQrDisplayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int requestId = getIntent().getIntExtra("request_id", -1);

        SessionManager sessionManager = new SessionManager(this);
        ServiceRequestRepository repository = new ServiceRequestRepository(NetworkModule.getApiService(sessionManager), sessionManager);
        viewModel = new WorkerViewModel(repository);

        setupObservers();

        if (requestId != -1) {
            viewModel.fetchQrCode(requestId);
        } else {
            Toast.makeText(this, "Invalid request ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.btnClose.setOnClickListener(v -> finish());
    }

    private void setupObservers() {
        viewModel.getQrCodeData().observe(this, qrData -> {
            if (qrData != null && qrData.getSecret_token() != null) {
                Bitmap bitmap = generateQrCodeBitmap(qrData.getSecret_token());
                if (bitmap != null) {
                    binding.ivQrCode.setImageBitmap(bitmap);
                }
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap generateQrCodeBitmap(String text) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            return null;
        }
    }
}
