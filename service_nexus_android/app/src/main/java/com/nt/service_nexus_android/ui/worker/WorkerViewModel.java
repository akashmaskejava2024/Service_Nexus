package com.nt.service_nexus_android.ui.worker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.nt.service_nexus_android.data.models.Bid;
import com.nt.service_nexus_android.data.models.DashboardResponse;
import com.nt.service_nexus_android.data.models.NearbyRequest;
import com.nt.service_nexus_android.data.models.QRResponse;
import com.nt.service_nexus_android.data.repository.RepositoryCallback;
import com.nt.service_nexus_android.data.repository.ServiceRequestRepository;
import java.util.ArrayList;
import java.util.List;

public class WorkerViewModel extends ViewModel {
    private final ServiceRequestRepository repository;

    private final MutableLiveData<List<NearbyRequest>> nearbyRequests = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<NearbyRequest>> acceptedJobs = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> bidSubmittedSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<QRResponse> qrCodeData = new MutableLiveData<>(null);

    public WorkerViewModel(ServiceRequestRepository repository) {
        this.repository = repository;
        loadRadarRequests();
    }

    public LiveData<List<NearbyRequest>> getNearbyRequests() { return nearbyRequests; }
    public LiveData<List<NearbyRequest>> getAcceptedJobs() { return acceptedJobs; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getBidSubmittedSuccess() { return bidSubmittedSuccess; }
    public LiveData<QRResponse> getQrCodeData() { return qrCodeData; }

    public void loadRadarRequests() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.getWorkerDashboard(new RepositoryCallback<DashboardResponse>() {
            @Override
            public void onSuccess(DashboardResponse dashboard) {
                isLoading.setValue(false);
                List<NearbyRequest> openNearby = new ArrayList<>();
                List<NearbyRequest> wonJobs = new ArrayList<>();

                for (NearbyRequest item : dashboard.getResults()) {
                    if ("PENDING".equalsIgnoreCase(item.getStatus())) {
                        openNearby.add(item);
                    } else {
                        wonJobs.add(item);
                    }
                }

                nearbyRequests.setValue(openNearby);
                acceptedJobs.setValue(wonJobs);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void submitBid(int requestId, String amount, String message) {
        if (amount == null || amount.trim().isEmpty()) {
            errorMessage.setValue("Please enter bid amount");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.submitBid(requestId, amount.trim(), message.trim(), new RepositoryCallback<Bid>() {
            @Override
            public void onSuccess(Bid result) {
                isLoading.setValue(false);
                bidSubmittedSuccess.setValue(true);
                loadRadarRequests();
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void fetchQrCode(int requestId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.getQRCode(requestId, new RepositoryCallback<QRResponse>() {
            @Override
            public void onSuccess(QRResponse result) {
                isLoading.setValue(false);
                qrCodeData.setValue(result);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void resetSuccessState() {
        bidSubmittedSuccess.setValue(false);
    }
}
