package org.bahmni.module.bahmnicore.service;

import java.util.Map;

public interface SqlSearchParamStore {
    void initQueryParamStore();

    boolean isInitialized();

    Map<String, Object> getSearchableParameters();
}
