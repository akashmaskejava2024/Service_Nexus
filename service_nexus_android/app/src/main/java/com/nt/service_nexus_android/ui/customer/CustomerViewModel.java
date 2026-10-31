package com.nt.service_nexus_android.ui.customer;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.nt.service_nexus_android.data.models.AcceptBidResponse;
import com.nt.service_nexus_android.data.models.CompleteJobResponse;
import com.nt.service_nexus_android.data.models.ReviewResponse;
import com.nt.service_nexus_android.data.models.ServiceRequest;
import com.nt.service_nexus_android.data.repository.AuthRepository;
import com.nt.service_nexus_android.data.repository.RepositoryCallback;
import com.nt.service_nexus_android.data.repository.ServiceRequestRepository;
import java.util.ArrayList;
import java.util.List;

public class CustomerViewModel extends ViewModel {
    private final ServiceRequestRepository repository;
    private final AuthRepository authRepository;

    private final MutableLiveData<List<ServiceRequest>> requests = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> requestCreatedSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> jobCompletedSuccess = new MutableLiveData<>(false);

    private double selectedLatitude = 19.0760;
    private double selectedLongitude = 72.8777;

    public CustomerViewModel(ServiceRequestRepository repository, AuthRepository authRepository) {
        this.repository = repository;
        this.authRepository = authRepository;
        this.selectedLatitude = authRepository.getLastLatitude();
        this.selectedLongitude = authRepository.getLastLongitude();
        loadCustomerRequests();
    }

    public LiveData<List<ServiceRequest>> getRequests() { return requests; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getRequestCreatedSuccess() { return requestCreatedSuccess; }
    public LiveData<Boolean> getJobCompletedSuccess() { return jobCompletedSuccess; }

    public double getSelectedLatitude() { return selectedLatitude; }
    public double getSelectedLongitude() { return selectedLongitude; }

    public void updateMapLocation(double lat, double lng) {
        this.selectedLatitude = lat;
        this.selectedLongitude = lng;
        authRepository.saveLastLocation(lat, lng);
    }

    public void loadCustomerRequests() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.getCustomerRequests(new RepositoryCallback<List<ServiceRequest>>() {
            @Override
            public void onSuccess(List<ServiceRequest> result) {
                isLoading.setValue(false);
                requests.setValue(result);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void createServiceRequest(Context context, String description, String photoUri) {
        if (description == null || description.trim().isEmpty()) {
            errorMessage.setValue("Please enter problem description");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.createServiceRequest(context, description.trim(), photoUri, selectedLatitude, selectedLongitude, new RepositoryCallback<ServiceRequest>() {
            @Override
            public void onSuccess(ServiceRequest result) {
                isLoading.setValue(false);
                requestCreatedSuccess.setValue(true);
                loadCustomerRequests();
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void acceptBid(int bidId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.acceptBid(bidId, new RepositoryCallback<AcceptBidResponse>() {
            @Override
            public void onSuccess(AcceptBidResponse result) {
                isLoading.setValue(false);
                loadCustomerRequests();
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void completeJob(int requestId, String secretToken) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.completeJob(requestId, secretToken, new RepositoryCallback<CompleteJobResponse>() {
            @Override
            public void onSuccess(CompleteJobResponse result) {
                isLoading.setValue(false);
                jobCompletedSuccess.setValue(true);
                loadCustomerRequests();
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void submitReview(int requestId, int rating, String comment) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.submitReview(requestId, rating, comment, new RepositoryCallback<ReviewResponse>() {
            @Override
            public void onSuccess(ReviewResponse result) {
                isLoading.setValue(false);
                loadCustomerRequests();
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void resetSuccessState() {
        requestCreatedSuccess.setValue(false);
        jobCompletedSuccess.setValue(false);
    }
}
