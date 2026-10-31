package com.nt.service_nexus_android.data.models;

public class QRResponse {
    private int request_id;
    private String secret_token;
    private String qr_code_url;

    public QRResponse() {}

    public int getRequest_id() { return request_id; }
    public String getSecret_token() { return secret_token; }
    public String getQr_code_url() { return qr_code_url; }
}
