package com.nt.service_nexus_android.data.remote;

import com.nt.service_nexus_android.data.models.*;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ── Auth ───────────────────────────────────────────────────────────────

    @POST("api/auth/register/customer/")
    Call<AuthResponse> registerCustomer(@Body RegisterCustomerRequest body);

    @POST("api/auth/register/worker/")
    Call<AuthResponse> registerWorker(@Body RegisterWorkerRequest body);

    @POST("api/auth/login/")
    Call<AuthResponse> login(@Body LoginRequest body);

    // ── Customer ───────────────────────────────────────────────────────────

    @Multipart
    @POST("api/requests/")
    Call<ServiceRequest> createServiceRequest(
            @Part("description") RequestBody description,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part MultipartBody.Part photo
    );

    @GET("api/requests/my-requests/")
    Call<List<ServiceRequest>> getMyRequests();

    @POST("api/bids/{bid_id}/accept/")
    Call<AcceptBidResponse> acceptBid(@Path("bid_id") int bidId);

    // ── Worker ─────────────────────────────────────────────────────────────

    @GET("api/worker/dashboard/")
    Call<DashboardResponse> getWorkerDashboard();

    @POST("api/requests/{request_id}/bid/")
    Call<Bid> submitBid(
            @Path("request_id") int requestId,
            @Body SubmitBidRequest body
    );

    // ── Jobs & Reviews ──────────────────────────────────────────────────────

    @GET("api/jobs/{request_id}/qr/")
    Call<QRResponse> getQRCode(@Path("request_id") int requestId);

    @POST("api/jobs/complete/")
    Call<CompleteJobResponse> completeJob(@Body CompleteJobRequest body);

    @POST("api/jobs/{request_id}/review/")
    Call<ReviewResponse> submitReview(
            @Path("request_id") int requestId,
            @Body ReviewRequest body
    );
}
