package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.lang3.LocaleUtils;
import org.bahmni.module.bahmnicore.service.BahmniDiagnosisService;
import org.bahmni.module.bahmnicore.service.TsConceptSearchService;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.util.LocaleUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TsConceptSearchServiceImpl implements TsConceptSearchService {
    private BahmniDiagnosisService bahmniDiagnosisService;

    private EmrConceptService emrConceptService;


    @Autowired
    public TsConceptSearchServiceImpl(BahmniDiagnosisService bahmniDiagnosisService, EmrConceptService emrConceptService) {
        this.bahmniDiagnosisService = bahmniDiagnosisService;
        this.emrConceptService = emrConceptService;
    }

    @Override
    public List<SimpleObject> getConcepts(String query, Integer limit, String locale) {
        boolean externalTerminologyServerLookupNeeded = bahmniDiagnosisService.isExternalTerminologyServerLookupNeeded();
        if (externalTerminologyServerLookupNeeded) {
            return new ArrayList<>();
        } else {
            return getDiagnosisConcepts(query, limit, locale);
        }
    }

    private List<SimpleObject> getDiagnosisConcepts(String query, Integer limit, String locale) {
        Collection<Concept> diagnosisSets = bahmniDiagnosisService.getDiagnosisSets();
        List<ConceptSource> conceptSources = bahmniDiagnosisService.getConceptSourcesForDiagnosisSearch();
        Locale searchLocale = getSearchLocale(locale);
        List<ConceptSearchResult> conceptSearchResults =
                emrConceptService.conceptSearch(query, LocaleUtility.getDefaultLocale(), null, diagnosisSets, conceptSources, limit);
        if(!LocaleUtility.getDefaultLocale().equals(searchLocale)) {
            conceptSearchResults.addAll(emrConceptService.conceptSearch(query, searchLocale, null, diagnosisSets, conceptSources, limit));
        }

        ConceptSource conceptSource = conceptSources.isEmpty() ? null : conceptSources.get(0);
        return createListResponse(conceptSearchResults, conceptSource, searchLocale);
    }

    private List<SimpleObject> createListResponse(List<ConceptSearchResult> resultList,
                                                  ConceptSource conceptSource, Locale searchLocale) {
        List<SimpleObject> allDiagnoses = new ArrayList<>();

        for (ConceptSearchResult diagnosis : resultList) {
            SimpleObject diagnosisObject = new SimpleObject();
            ConceptName conceptName = diagnosis.getConcept().getName(searchLocale);
            if (conceptName == null) {
                conceptName = diagnosis.getConcept().getName();
            }
            diagnosisObject.add("conceptName", conceptName.getName());
            diagnosisObject.add("conceptUuid", diagnosis.getConcept().getUuid());
            if (diagnosis.getConceptName() != null) {
                diagnosisObject.add("matchedName", diagnosis.getConceptName().getName());
            }
            ConceptReferenceTerm term = getConceptReferenceTermByConceptSource(diagnosis.getConcept(), conceptSource);
            if (term != null) {
                diagnosisObject.add("code", term.getCode());
            }
            allDiagnoses.add(diagnosisObject);
        }
        return allDiagnoses;
    }

    private ConceptReferenceTerm getConceptReferenceTermByConceptSource(Concept concept, ConceptSource conceptSource) {
        Collection<ConceptMap> conceptMappings = concept.getConceptMappings();
        if (conceptMappings != null && conceptSource != null) {
            for (ConceptMap cm : conceptMappings) {
                ConceptReferenceTerm term = cm.getConceptReferenceTerm();
                if (conceptSource.equals(term.getConceptSource())) {
                    return term;
                }
            }
        }
        return null;
    }

    private Locale getSearchLocale(String localeStr) {
        if (localeStr == null) {
            return Context.getLocale();
        }
        Locale locale;
        try {
            locale = LocaleUtils.toLocale(localeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(localeErrorMessage("emrapi.conceptSearch.invalidLocale", localeStr));
        }
        if (allowedLocale(locale)) {
            return locale;
        } else {
            throw new IllegalArgumentException(localeErrorMessage("emrapi.conceptSearch.unsupportedLocale", localeStr));
        }
    }

    private boolean allowedLocale(Locale locale) {
        Set<Locale> allowedLocales = new HashSet<>(Context.getAdministrationService().getAllowedLocales());
        return allowedLocales.contains(locale);
    }

    private String localeErrorMessage(String msgKey, String localeStr) {
        return Context.getMessageSourceService().getMessage(msgKey, new Object[]{localeStr}, Context.getLocale());
    }

    @Override
    public void onStartup() {

    }

    @Override
    public void onShutdown() {

    }
}
