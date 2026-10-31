package com.nt.service_nexus_android.data.models;

public class AuthResponse {
    private String access;
    private String refresh;
    private int user_id;
    private String username;
    private String user_type;

    public AuthResponse() {}

    public String getAccess() { return access; }
    public String getRefresh() { return refresh; }
    public int getUser_id() { return user_id; }
    public String getUsername() { return username; }
    public String getUser_type() { return user_type; }
}
