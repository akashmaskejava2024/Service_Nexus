package com.nt.service_nexus_android.data.remote;

import com.nt.service_nexus_android.data.local.SessionManager;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkModule {
    private static final String BASE_URL = "http://10.0.2.2:8000/";
    private static volatile ApiService apiServiceInstance;

    public static ApiService getApiService(SessionManager sessionManager) {
        if (apiServiceInstance == null) {
            synchronized (NetworkModule.class) {
                if (apiServiceInstance == null) {
                    apiServiceInstance = buildApiService(sessionManager);
                }
            }
        }
        return apiServiceInstance;
    }

    private static ApiService buildApiService(SessionManager sessionManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(sessionManager))
                .addInterceptor(new MockNetworkInterceptor())
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(ApiService.class);
    }
}
