package com.nt.service_nexus_android.data.repository;

public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
}
