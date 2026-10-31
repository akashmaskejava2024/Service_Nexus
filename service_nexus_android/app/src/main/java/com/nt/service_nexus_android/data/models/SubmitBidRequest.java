package com.nt.service_nexus_android.data.models;

public class SubmitBidRequest {
    private String amount;
    private String message;

    public SubmitBidRequest(String amount, String message) {
        this.amount = amount;
        this.message = message;
    }

    public String getAmount() { return amount; }
    public String getMessage() { return message; }
}
