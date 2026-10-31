package com.nt.service_nexus_android.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.nt.service_nexus_android.data.models.Role;
import com.nt.service_nexus_android.data.models.TradeCategory;
import com.nt.service_nexus_android.data.models.User;
import com.nt.service_nexus_android.data.repository.AuthRepository;
import com.nt.service_nexus_android.data.repository.RepositoryCallback;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<User> authSuccessUser = new MutableLiveData<>(null);

    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<User> getAuthSuccessUser() { return authSuccessUser; }

    public void login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Please enter both username and password");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        authRepository.login(username.trim(), password.trim(), new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                isLoading.setValue(false);
                authSuccessUser.setValue(user);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void registerCustomer(String username, String email, String password) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            errorMessage.setValue("Username and password are required");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        authRepository.registerCustomer(username.trim(), email.trim(), password.trim(), new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                isLoading.setValue(false);
                authSuccessUser.setValue(user);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void registerWorker(String username, String email, String password, TradeCategory category, double lat, double lng) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            errorMessage.setValue("Username and password are required");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        authRepository.registerWorker(username.trim(), email.trim(), password.trim(), category, lat, lng, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                isLoading.setValue(false);
                authSuccessUser.setValue(user);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}
