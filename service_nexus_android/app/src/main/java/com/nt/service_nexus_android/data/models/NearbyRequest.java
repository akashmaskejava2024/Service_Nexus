package com.nt.service_nexus_android.data.models;

public class NearbyRequest {
    private int id;
    private String description;
    private String photo;
    private double latitude;
    private double longitude;
    private String status;
    private String created_at;
    private String customer_username;
    private int bids_count;
    private double distance_km;

    public NearbyRequest() {}

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

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getCustomer_username() { return customer_username != null ? customer_username : "Customer"; }
    public void setCustomer_username(String customer_username) { this.customer_username = customer_username; }

    public int getBids_count() { return bids_count; }
    public void setBids_count(int bids_count) { this.bids_count = bids_count; }

    public double getDistance_km() { return distance_km; }
    public void setDistance_km(double distance_km) { this.distance_km = distance_km; }
}
