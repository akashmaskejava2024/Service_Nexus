package com.nt.service_nexus_android.data.models;

public class RegisterCustomerRequest {
    private String username;
    private String email;
    private String password;
    private String password2;

    public RegisterCustomerRequest(String username, String email, String password, String password2) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.password2 = password2;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPassword2() { return password2; }
}
