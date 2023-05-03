package org.bahmni.module.bahmnicore.service;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;

public interface TsConceptSearchService extends OpenmrsService {
     List<SimpleObject> getConcepts(String query, Integer limit, String locale);
}
