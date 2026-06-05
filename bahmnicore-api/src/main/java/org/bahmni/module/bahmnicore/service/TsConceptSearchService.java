package org.bahmni.module.bahmnicore.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;

public interface TsConceptSearchService extends OpenmrsService {
     @Authorized(value = {"Get Concepts"})
     List<SimpleObject> getConcepts(String query, Integer limit, String locale);
}
