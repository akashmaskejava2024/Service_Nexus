package com.nt.service_nexus_android.data.repository;

import com.nt.service_nexus_android.data.local.SessionManager;
import com.nt.service_nexus_android.data.models.*;
import com.nt.service_nexus_android.data.remote.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final ApiService apiService;
    private final SessionManager sessionManager;

    public AuthRepository(ApiService apiService, SessionManager sessionManager) {
        this.apiService = apiService;
        this.sessionManager = sessionManager;
    }

    public void login(String username, String password, RepositoryCallback<User> callback) {
        apiService.login(new LoginRequest(username, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    sessionManager.saveTokens(auth.getAccess(), auth.getRefresh());

                    Role role = "WORKER".equalsIgnoreCase(auth.getUser_type()) ? Role.WORKER : Role.CUSTOMER;
                    User user = new User(auth.getUser_id(), auth.getUsername(), role);
                    sessionManager.saveUser(user);
                    callback.onSuccess(user);
                } else {
                    String msg = "Login failed. Check credentials.";
                    try {
                        if (response.errorBody() != null) msg = response.errorBody().string();
                    } catch (Exception ignored) {}
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Network error");
            }
        });
    }

    public void registerCustomer(String username, String email, String password, RepositoryCallback<User> callback) {
        RegisterCustomerRequest req = new RegisterCustomerRequest(username, email, password, password);
        apiService.registerCustomer(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    sessionManager.saveTokens(auth.getAccess(), auth.getRefresh());

                    User user = new User(auth.getUser_id(), auth.getUsername(), email, Role.CUSTOMER, null, 19.0760, 72.8777);
                    sessionManager.saveUser(user);
                    callback.onSuccess(user);
                } else {
                    String msg = "Customer registration failed.";
                    try {
                        if (response.errorBody() != null) msg = response.errorBody().string();
                    } catch (Exception ignored) {}
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Network error");
            }
        });
    }

    public void registerWorker(String username, String email, String password, TradeCategory category, double latitude, double longitude, RepositoryCallback<User> callback) {
        RegisterWorkerRequest req = new RegisterWorkerRequest(username, email, password, password, category.name(), latitude, longitude);
        apiService.registerWorker(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    sessionManager.saveTokens(auth.getAccess(), auth.getRefresh());

                    User user = new User(auth.getUser_id(), auth.getUsername(), email, Role.WORKER, category, latitude, longitude);
                    sessionManager.saveUser(user);
                    callback.onSuccess(user);
                } else {
                    String msg = "Worker registration failed.";
                    try {
                        if (response.errorBody() != null) msg = response.errorBody().string();
                    } catch (Exception ignored) {}
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Network error");
            }
        });
    }

    public User getCurrentUser() {
        return sessionManager.getUser();
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public void logout() {
        sessionManager.clearSession();
    }

    public void saveLastLocation(double lat, double lng) {
        sessionManager.saveLastLocation(lat, lng);
    }

    public double getLastLatitude() {
        return sessionManager.getLastLatitude();
    }

    public double getLastLongitude() {
        return sessionManager.getLastLongitude();
    }
}
