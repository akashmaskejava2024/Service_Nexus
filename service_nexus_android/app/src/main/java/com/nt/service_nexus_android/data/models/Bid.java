package com.nt.service_nexus_android.data.models;

public class Bid {
    private int id;
    private String worker_username;
    private String worker_category;
    private String amount;
    private String message;
    private boolean is_accepted;
    private String created_at;

    public Bid() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getWorker_username() { return worker_username != null ? worker_username : "Worker"; }
    public void setWorker_username(String worker_username) { this.worker_username = worker_username; }

    public String getWorker_category() { return worker_category != null ? worker_category : "OTHER"; }
    public void setWorker_category(String worker_category) { this.worker_category = worker_category; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isIs_accepted() { return is_accepted; }
    public void setIs_accepted(boolean is_accepted) { this.is_accepted = is_accepted; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}
