package com.nt.service_nexus_android.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.nt.service_nexus_android.data.models.User;

public class SessionManager {
    private static final String PREF_NAME = "service_nexus_session";
    private static final String KEY_ACCESS_TOKEN = "jwt_access_token";
    private static final String KEY_REFRESH_TOKEN = "jwt_refresh_token";
    private static final String KEY_USER = "user_json";
    private static final String KEY_LAT = "last_latitude";
    private static final String KEY_LNG = "last_longitude";

    public static final double DEFAULT_LAT = 19.0760;
    public static final double DEFAULT_LNG = 72.8777;

    private final SharedPreferences prefs;
    private final Gson gson;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveTokens(String access, String refresh) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, access)
                .putString(KEY_REFRESH_TOKEN, refresh)
                .apply();
    }

    public void updateAccessToken(String access) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, access).apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void saveUser(User user) {
        String userJson = gson.toJson(user);
        prefs.edit().putString(KEY_USER, userJson).apply();
        if (user != null) {
            saveLastLocation(user.getLatitude(), user.getLongitude());
        }
    }

    public User getUser() {
        String userJson = prefs.getString(KEY_USER, null);
        if (userJson == null) return null;
        try {
            return gson.fromJson(userJson, User.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void saveLastLocation(double lat, double lng) {
        prefs.edit()
                .putFloat(KEY_LAT, (float) lat)
                .putFloat(KEY_LNG, (float) lng)
                .apply();
    }

    public double getLastLatitude() {
        User user = getUser();
        if (user != null && user.getLatitude() != 0.0) return user.getLatitude();
        double lat = prefs.getFloat(KEY_LAT, (float) DEFAULT_LAT);
        return lat == 0.0 ? DEFAULT_LAT : lat;
    }

    public double getLastLongitude() {
        User user = getUser();
        if (user != null && user.getLongitude() != 0.0) return user.getLongitude();
        double lng = prefs.getFloat(KEY_LNG, (float) DEFAULT_LNG);
        return lng == 0.0 ? DEFAULT_LNG : lng;
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null && getUser() != null;
    }
}
