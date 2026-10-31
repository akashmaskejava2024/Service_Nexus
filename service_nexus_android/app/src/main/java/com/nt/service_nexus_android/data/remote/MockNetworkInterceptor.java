package com.nt.service_nexus_android.data.remote;

import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.nt.service_nexus_android.data.models.*;
import java.io.IOException;
import java.util.*;
import okhttp3.*;

public class MockNetworkInterceptor implements Interceptor {
    private final Gson gson = new Gson();
    private final MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");

    private final List<ServiceRequest> mockRequests = new ArrayList<>();
    private final List<Bid> mockBids = new ArrayList<>();

    public MockNetworkInterceptor() {
        seedInitialMockData();
    }

    private void seedInitialMockData() {
        int req1Id = 7;
        String req1Token = "550e8400-e29b-41d4-a716-446655440000";

        Bid bid1 = new Bid();
        bid1.setId(3);
        bid1.setWorker_username("bob_plumber");
        bid1.setWorker_category("PLUMBER");
        bid1.setAmount("850.00");
        bid1.setMessage("Can fix in 2 hours with all plumbing tools.");
        bid1.setIs_accepted(false);
        bid1.setCreated_at("2026-10-27T07:30:00Z");
        mockBids.add(bid1);

        ServiceRequest req1 = new ServiceRequest();
        req1.setId(req1Id);
        req1.setDescription("Main bathroom faucet leaking heavily near washer joint.");
        req1.setPhoto("https://picsum.photos/seed/leak1/400/300");
        req1.setLatitude(19.0780);
        req1.setLongitude(72.8790);
        req1.setStatus("PENDING");
        req1.setSecret_token(req1Token);
        req1.setCreated_at("2026-10-27T07:00:00Z");
        req1.setCustomer_username("alice");
        req1.getBids().add(bid1);

        ServiceRequest req2 = new ServiceRequest();
        req2.setId(8);
        req2.setDescription("Sparking kitchen switchboard. Needs quick electrical inspection.");
        req2.setPhoto(null);
        req2.setLatitude(19.0850);
        req2.setLongitude(72.8820);
        req2.setStatus("PENDING");
        req2.setSecret_token(UUID.randomUUID().toString());
        req2.setCreated_at("2026-10-27T08:15:00Z");
        req2.setCustomer_username("alice");

        mockRequests.add(req1);
        mockRequests.add(req2);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        try {
            return chain.proceed(request);
        } catch (Exception e) {
            return handleMockRequest(request);
        }
    }

    private Response handleMockRequest(Request request) {
        String path = request.url().encodedPath();
        String method = request.method();

        if (path.endsWith("/api/auth/login/") && "POST".equalsIgnoreCase(method)) {
            AuthResponse auth = new AuthResponse();
            return makeResponse(request, 200, auth);
        }

        if (path.endsWith("/api/requests/my-requests/") && "GET".equalsIgnoreCase(method)) {
            return makeResponse(request, 200, mockRequests);
        }

        if (path.endsWith("/api/worker/dashboard/") && "GET".equalsIgnoreCase(method)) {
            DashboardResponse db = new DashboardResponse();
            List<NearbyRequest> results = new ArrayList<>();
            for (ServiceRequest req : mockRequests) {
                NearbyRequest nr = new NearbyRequest();
                nr.setId(req.getId());
                nr.setDescription(req.getDescription());
                nr.setPhoto(req.getPhoto());
                nr.setLatitude(req.getLatitude());
                nr.setLongitude(req.getLongitude());
                nr.setStatus(req.getStatus());
                nr.setCreated_at(req.getCreated_at());
                nr.setCustomer_username(req.getCustomer_username());
                nr.setBids_count(req.getBids().size());
                nr.setDistance_km(calculateHaversineDistance(19.0760, 72.8777, req.getLatitude(), req.getLongitude()));
                results.add(nr);
            }
            db.setResults(results);
            return makeResponse(request, 200, db);
        }

        Map<String, String> errMap = new HashMap<>();
        errMap.put("detail", "Mock response");
        return makeResponse(request, 200, errMap);
    }

    private Response makeResponse(Request request, int code, Object data) {
        String json = gson.toJson(data);
        ResponseBody body = ResponseBody.create(json, jsonMediaType);
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(code >= 200 && code < 300 ? "OK" : "Error")
                .body(body)
                .build();
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(r * c * 100.0) / 100.0;
    }
}
