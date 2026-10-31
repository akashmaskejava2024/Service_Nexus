package com.nt.service_nexus_android.data.remote;

import androidx.annotation.NonNull;
import com.nt.service_nexus_android.data.local.SessionManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String token = sessionManager.getAccessToken();

        if (token != null && !token.trim().isEmpty()) {
            Request authenticatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(authenticatedRequest);
        } else {
            return chain.proceed(originalRequest);
        }
    }
}
