package com.nt.service_nexus_android.data.models;

public class User {
    private int id;
    private String username;
    private String email;
    private Role role;
    private TradeCategory tradeCategory;
    private double latitude;
    private double longitude;

    public User() {}

    public User(int id, String username, Role role) {
        this(id, username, "", role, null, 19.0760, 72.8777);
    }

    public User(int id, String username, String email, Role role, TradeCategory tradeCategory, double latitude, double longitude) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.tradeCategory = tradeCategory;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public TradeCategory getTradeCategory() { return tradeCategory; }
    public void setTradeCategory(TradeCategory tradeCategory) { this.tradeCategory = tradeCategory; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
