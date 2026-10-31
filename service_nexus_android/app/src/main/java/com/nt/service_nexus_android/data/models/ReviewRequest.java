package com.nt.service_nexus_android.data.models;

public class ReviewRequest {
    private int rating;
    private String comment;

    public ReviewRequest(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    public int getRating() { return rating; }
    public String getComment() { return comment; }
}
