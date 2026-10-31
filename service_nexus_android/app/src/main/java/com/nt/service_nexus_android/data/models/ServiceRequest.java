package com.nt.service_nexus_android.data.models;

import java.util.ArrayList;
import java.util.List;

public class ServiceRequest {
    private int id;
    private String description;
    private String photo;
    private double latitude;
    private double longitude;
    private String status;
    private String secret_token;
    private String created_at;
    private String customer_username;
    private List<Bid> bids;

    public ServiceRequest() {
        this.status = "PENDING";
        this.secret_token = "";
        this.created_at = "";
        this.customer_username = "Customer";
        this.bids = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getStatus() { return status != null ? status : "PENDING"; }
    public void setStatus(String status) { this.status = status; }

    public String getSecret_token() { return secret_token; }
    public void setSecret_token(String secret_token) { this.secret_token = secret_token; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getCustomer_username() { return customer_username != null ? customer_username : "Customer"; }
    public void setCustomer_username(String customer_username) { this.customer_username = customer_username; }

    public List<Bid> getBids() { return bids != null ? bids : new ArrayList<>(); }
    public void setBids(List<Bid> bids) { this.bids = bids; }
}
