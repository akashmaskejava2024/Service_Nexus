package com.nt.service_nexus_android.data.repository;

import android.content.Context;
import android.net.Uri;
import com.nt.service_nexus_android.data.local.SessionManager;
import com.nt.service_nexus_android.data.models.*;
import com.nt.service_nexus_android.data.remote.ApiService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceRequestRepository {
    private final ApiService apiService;
    private final SessionManager sessionManager;

    public ServiceRequestRepository(ApiService apiService, SessionManager sessionManager) {
        this.apiService = apiService;
        this.sessionManager = sessionManager;
    }

    public void createServiceRequest(Context context, String description, String photoUriString, double latitude, double longitude, RepositoryCallback<ServiceRequest> callback) {
        MediaType textType = MediaType.parse("text/plain");
        RequestBody descPart = RequestBody.create(description, textType);
        RequestBody latPart = RequestBody.create(String.valueOf(latitude), textType);
        RequestBody lngPart = RequestBody.create(String.valueOf(longitude), textType);

        MultipartBody.Part photoPart = null;
        if (photoUriString != null && !photoUriString.trim().isEmpty()) {
            File file = getFileFromUri(context, photoUriString);
            if (file != null && file.exists()) {
                RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
                photoPart = MultipartBody.Part.createFormData("photo", file.getName(), reqFile);
            }
        }

        apiService.createServiceRequest(descPart, latPart, lngPart, photoPart).enqueue(new Callback<ServiceRequest>() {
            @Override
            public void onResponse(Call<ServiceRequest> call, Response<ServiceRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ServiceRequest req = response.body();
                    String curUser = sessionManager.getUser() != null ? sessionManager.getUser().getUsername() : "Customer";
                    req.setCustomer_username(curUser);
                    if (photoUriString != null) req.setPhoto(photoUriString);
                    callback.onSuccess(req);
                } else {
                    String msg = "Failed to create service request";
                    try { if (response.errorBody() != null) msg = response.errorBody().string(); } catch (Exception ignored) {}
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<ServiceRequest> call, Throwable t) {
                callback.onError(t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Network error");
            }
        });
    }

    private File getFileFromUri(Context context, String uriStr) {
        try {
            Uri uri = Uri.parse(uriStr);
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                File f = new File(uri.getPath() != null ? uri.getPath() : "");
                if (f.exists()) return f;
            }

            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = new File(context.getCacheDir(), "temp_upload_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.close();
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    public void getCustomerRequests(RepositoryCallback<List<ServiceRequest>> callback) {
        apiService.getMyRequests().enqueue(new Callback<List<ServiceRequest>>() {
            @Override
            public void onResponse(Call<List<ServiceRequest>> call, Response<List<ServiceRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String curUser = sessionManager.getUser() != null ? sessionManager.getUser().getUsername() : "Customer";
                    List<ServiceRequest> formattedList = new ArrayList<>();
                    for (ServiceRequest req : response.body()) {
                        String photoUrl = req.getPhoto();
                        if (photoUrl != null && !photoUrl.trim().isEmpty() && !photoUrl.startsWith("http")) {
                            photoUrl = "http://10.0.2.2:8000" + (photoUrl.startsWith("/") ? "" : "/") + photoUrl;
                        }
                        req.setPhoto(photoUrl);

                        String uname = req.getCustomer_username();
                        if (uname == null || uname.trim().isEmpty() || "Customer".equals(uname)) {
                            req.setCustomer_username(curUser);
                        }
                        formattedList.add(req);
                    }
                    callback.onSuccess(formattedList);
                } else {
                    String msg = "Failed to fetch customer requests";
                    try { if (response.errorBody() != null) msg = response.errorBody().string(); } catch (Exception ignored) {}
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<List<ServiceRequest>> call, Throwable t) {
                callback.onError(t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Network error");
            }
        });
    }

    public void acceptBid(int bidId, RepositoryCallback<AcceptBidResponse> callback) {
        apiService.acceptBid(bidId).enqueue(new Callback<AcceptBidResponse>() {
            @Override
            public void onResponse(Call<AcceptBidResponse> call, Response<AcceptBidResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String msg = "Failed to accept bid";
                    try { if (response.errorBody() != null) msg = response.errorBody().string(); } catch (Exception ignored) {}
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<AcceptBidResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Network error");
            }
        });
    }

    public void getWorkerDashboard(RepositoryCallback<DashboardResponse> callback) {
        apiService.getWorkerDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse db = response.body();
                    List<NearbyRequest> formatted = new ArrayList<>();
                    for (NearbyRequest item : db.getResults()) {
                        String photoUrl = item.getPhoto();
                        if (photoUrl != null && !photoUrl.trim().isEmpty() && !photoUrl.startsWith("http")) {
                            photoUrl = "http://10.0.2.2:8000" + (photoUrl.startsWith("/") ? "" : "/") + photoUrl;
                        }
                        item.setPhoto(photoUrl);
                        formatted.add(item);
                    }
                    db.setResults(formatted);
                    callback.onSuccess(db);
                } else {
                    String msg = "Failed to fetch worker dashboard";
                    try { if (response.errorBody() != null) msg = response.errorBody().string(); } catch (Exception ignored) {}
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Network error");
            }
        });
    }

    public void submitBid(int requestId, String amount, String message, RepositoryCallback<Bid> callback) {
        SubmitBidRequest req = new SubmitBidRequest(amount, message);
        apiService.submitBid(requestId, req).enqueue(new Callback<Bid>() {
            @Override
            public void onResponse(Call<Bid> call, Response<Bid> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Bid bid = response.body();
                    String curWorker = sessionManager.getUser() != null ? sessionManager.getUser().getUsername() : "Worker";
                    bid.setWorker_username(curWorker);
                    callback.onSuccess(bid);
                } else {
                    String msg = "Failed to submit bid";
                    try { if (response.errorBody() != null) msg = response.errorBody().string(); } catch (Exception ignored) {}
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<Bid> call, Throwable t) {
                callback.onError(t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Network error");
            }
        });
    }

    public void getQRCode(int requestId, RepositoryCallback<QRResponse> callback) {
        apiService.getQRCode(requestId).enqueue(new Callback<QRResponse>() {
            @Override
            public void onResponse(Call<QRResponse> call, Response<QRResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String msg = "Failed to fetch QR code";
                    try { if (response.errorBody() != null) msg = response.errorBody().string(); } catch (Exception ignored) {}
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<QRResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Network error");
            }
        });
    }

    public void completeJob(int requestId, String secretToken, RepositoryCallback<CompleteJobResponse> callback) {
        CompleteJobRequest req = new CompleteJobRequest(requestId, secretToken);
        apiService.completeJob(req).enqueue(new Callback<CompleteJobResponse>() {
            @Override
            public void onResponse(Call<CompleteJobResponse> call, Response<CompleteJobResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String msg = "Failed to complete job";
                    try { if (response.errorBody() != null) msg = response.errorBody().string(); } catch (Exception ignored) {}
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<CompleteJobResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Network error");
            }
        });
    }

    public void submitReview(int requestId, int rating, String comment, RepositoryCallback<ReviewResponse> callback) {
        ReviewRequest req = new ReviewRequest(rating, comment);
        apiService.submitReview(requestId, req).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String msg = "Failed to submit review";
                    try { if (response.errorBody() != null) msg = response.errorBody().string(); } catch (Exception ignored) {}
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "Network error");
            }
        });
    }
}
