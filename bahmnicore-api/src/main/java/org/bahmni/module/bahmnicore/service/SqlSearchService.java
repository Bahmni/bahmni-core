package org.bahmni.module.bahmnicore.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

public interface SqlSearchService {

    @Transactional(readOnly = true)
    @Authorized
    public List<SimpleObject> search(String sqlQuery, Map<String, String[]> params);

}
