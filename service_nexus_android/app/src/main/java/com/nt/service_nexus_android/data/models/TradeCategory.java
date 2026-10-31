package com.nt.service_nexus_android.data.models;

public enum TradeCategory {
    PLUMBER("Plumber"),
    ELECTRICIAN("Electrician"),
    CARPENTER("Carpenter"),
    PAINTER("Painter"),
    LOCKSMITH("Locksmith"),
    OTHER("General Repair");

    private final String displayName;

    TradeCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
