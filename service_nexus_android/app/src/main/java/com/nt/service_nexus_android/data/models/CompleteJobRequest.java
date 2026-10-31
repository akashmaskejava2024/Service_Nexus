package com.nt.service_nexus_android.data.models;

public class CompleteJobRequest {
    private int request_id;
    private String secret_token;

    public CompleteJobRequest(int request_id, String secret_token) {
        this.request_id = request_id;
        this.secret_token = secret_token;
    }

    public int getRequest_id() { return request_id; }
    public String getSecret_token() { return secret_token; }
}
