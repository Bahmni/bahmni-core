package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.apache.commons.collections.CollectionUtils;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.bahmni.module.bahmnicore.web.v1_0.LocaleResolver;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.LocaleUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.bahmni.module.bahmnicore.web.v1_0.LocaleResolver.identifyLocale;

@Component
public class VisitFormsSearchHandler implements SearchHandler {
    @Autowired
    private EpisodeService episodeService;
    private final String ALL_OBSERVATION_TEMPLATES = "All Observation Templates";
    private final String QUERY_INFORMATION = "Allows you to search All Observation Templates by patientUuid";

    @Override
    public SearchConfig getSearchConfig() {
        SearchQuery searchQuery = new SearchQuery.Builder(QUERY_INFORMATION).withRequiredParameters("patient", "numberOfVisits").withOptionalParameters("conceptNames").build();
        return new SearchConfig("byPatientUuid", RestConstants.VERSION_1 + "/obs", asList("1.10.* - 2.*"), searchQuery);
    }

    @Override
    public PageableResult search(RequestContext context) throws ResponseException {

        String patientUuid = context.getRequest().getParameter("patient");
        String patientProgramUuid = context.getRequest().getParameter("patientProgramUuid");
        int numberOfVisits = Integer.parseInt(context.getRequest().getParameter("numberOfVisits"));
        String[] conceptNames = context.getRequest().getParameterValues("conceptNames");
        Locale searchLocale = identifyLocale(context.getRequest().getSession().getAttribute("locale").toString());

        Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
        if (patient == null) {
            throw new InvalidSearchException("Patient does not exist.");
        }
        List<String> conceptNamesList = new ArrayList<>();
        if (conceptNames == null) {
            List<Concept> concepts = Context.getConceptService().getConceptsByName(ALL_OBSERVATION_TEMPLATES, Locale.ENGLISH, false);
            if(!concepts.isEmpty()){
                for (Concept concept : concepts) {
                    conceptNamesList = getConcepts(concept.getSetMembers(), searchLocale);
                }
            }
        } else {
            conceptNamesList = asList(conceptNames);
        }

        List<Encounter> encounterList;
        if (patientProgramUuid != null) {
            encounterList = getEncountersWithinProgram(patientProgramUuid);
        } else {
            encounterList = getEncountersFor(numberOfVisits, patient);
        }

        List<Obs> finalObsList = getObservations(patient, conceptNamesList, encounterList, searchLocale);

        return new NeedsPaging<Obs>(finalObsList, context);
    }

    private List<Obs> getObservations(Patient patient, List<String> conceptNames, List<Encounter> encounterList, Locale searchLocale) {
        List<Obs> finalObsList = new ArrayList<>();
        if (CollectionUtils.isEmpty(encounterList)) {
            return finalObsList;
        }

        List<Obs> initialObsList = Context.getObsService().getObservations(Collections.singletonList(patient.getPerson()), encounterList, null, null, null, null, null, null, null, null, null, false);

        for (Obs obs : initialObsList) {
            String name = null;
            if(obs.getConcept()!= null){
                name = getConceptName(obs.getConcept(), searchLocale);
            }
            if (conceptNames.contains(name)) {
                finalObsList.add(obs);
            }
        }
        return finalObsList;
    }

    private List<Encounter> getEncountersFor(int numberOfVisits, Patient patient) {
        List<Encounter> encounterList;
        List<Visit> listOfVisitsNeeded = listOfVisitsNeeded(numberOfVisits, patient);
        encounterList = Context.getEncounterService().getEncounters(patient, null, null, null, null, null, null, null, listOfVisitsNeeded, false);
        return encounterList;
    }

    private List<Encounter> getEncountersWithinProgram(String patientProgramUuid) {
        List<Encounter> encounterList = new ArrayList<>();
        PatientProgram patientProgram = Context.getService(BahmniProgramWorkflowService.class).getPatientProgramByUuid(patientProgramUuid);
        Episode episode = episodeService.getEpisodeForPatientProgram(patientProgram);
        if (episode != null) {
            encounterList = new ArrayList<>(episode.getEncounters());
        }
        return encounterList;
    }

    private List<String> getConcepts(List<Concept> concepts, Locale searchLocale) {
        List<String> conceptNames = new ArrayList<>();
        if (concepts == null)
            return conceptNames;
        for (Concept concept : concepts) {
            conceptNames.add(getConceptName(concept, searchLocale));
        }
        return conceptNames;
    }

    private String getConceptName(Concept concept, Locale searchLocale) {
        if(concept.getFullySpecifiedName(searchLocale) != null)
            return concept.getFullySpecifiedName(searchLocale).getName();
        else
            return concept.getFullySpecifiedName(LocaleUtility.getDefaultLocale()).getName();
    }

    private List<Visit> listOfVisitsNeeded(int numberOfVisits, Patient patient) {
        List<Visit> visitsByPatient = Context.getVisitService().getVisitsByPatient(patient);
        int subsetVisits = numberOfVisits;
        if (visitsByPatient.size() < numberOfVisits) {
            subsetVisits = visitsByPatient.size();
        }
        return visitsByPatient.subList(0, subsetVisits);
    }
}
