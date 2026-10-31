package com.nt.service_nexus_android.data.models;

import java.util.ArrayList;
import java.util.List;

public class DashboardResponse {
    private int count;
    private List<NearbyRequest> results;

    public DashboardResponse() {
        this.results = new ArrayList<>();
    }

    public int getCount() { return count; }
    public List<NearbyRequest> getResults() { return results != null ? results : new ArrayList<>(); }
    public void setResults(List<NearbyRequest> results) { this.results = results; }
}
