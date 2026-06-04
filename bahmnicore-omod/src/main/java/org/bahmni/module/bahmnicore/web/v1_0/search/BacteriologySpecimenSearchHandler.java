package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.apache.commons.collections.CollectionUtils;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bacteriology.api.BacteriologyService;
import org.openmrs.module.bacteriology.api.encounter.domain.Specimen;
import org.openmrs.module.bacteriology.api.encounter.domain.Specimens;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

@Component
public class BacteriologySpecimenSearchHandler implements SearchHandler {

    private final String BACTERIOLOGY_CONCEPT_SET = "BACTERIOLOGY CONCEPT SET";
    private final String QUERY_INFORMATION = "Allows you to toString specimens based on the pateint program enrollment.";

    private BahmniProgramWorkflowService bahmniProgramWorkflowService;
    private ConceptService conceptService;
    private ObsService obsService;

    @Autowired
    public BacteriologySpecimenSearchHandler(@Qualifier("bahmniProgramWorkflowService")
                                                     BahmniProgramWorkflowService bahmniProgramWorkflowService,
                                             ConceptService conceptService,
                                             ObsService obsService) {
        this.bahmniProgramWorkflowService = bahmniProgramWorkflowService;
        this.conceptService = conceptService;
        this.obsService = obsService;
    }

    @Override
    public SearchConfig getSearchConfig() {
        SearchQuery searchQuery = new SearchQuery.Builder(QUERY_INFORMATION).withRequiredParameters("patientProgramUuid").build();
        return new SearchConfig("byPatientProgram", RestConstants.VERSION_1 + "/specimen", asList("1.10.* - 2.*"), searchQuery);
    }

    @Override
    public PageableResult search(RequestContext requestContext) throws ResponseException {

        BacteriologyService bacteriologyService = Context.getService(BacteriologyService.class);
        String patientProgramUuid = requestContext.getParameter("patientProgramUuid");
        Collection<Encounter> encounters = bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(patientProgramUuid);
        if (CollectionUtils.isEmpty(encounters)) {
            return new EmptySearchResult();
        }
        List<Encounter> encountersAsList = new ArrayList<>(encounters);

        Concept bacteriologyConceptSet = conceptService.getConceptByName(BACTERIOLOGY_CONCEPT_SET);
        List<Concept> concepts = Arrays.asList(bacteriologyConceptSet);
        List<Obs> observations = obsService.getObservations(null, encountersAsList, concepts, null, null, null, null, null, null, null, null, false);

        Specimens sortedSpecimens = bacteriologyService.getSpecimens(observations).sortByDateCollected();

        return new NeedsPaging<Specimen>(sortedSpecimens, requestContext);
    }

}
