package com.nt.service_nexus_android.data.models;

public class RegisterWorkerRequest {
    private String username;
    private String email;
    private String password;
    private String password2;
    private String category;
    private double latitude;
    private double longitude;

    public RegisterWorkerRequest(String username, String email, String password, String password2, String category, double latitude, double longitude) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.password2 = password2;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPassword2() { return password2; }
    public String getCategory() { return category; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
