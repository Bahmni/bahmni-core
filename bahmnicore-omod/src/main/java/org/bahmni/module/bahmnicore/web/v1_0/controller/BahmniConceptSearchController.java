package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.lang3.LocaleUtils;
import org.bahmni.module.bahmnicore.service.BahmniDiagnosisService;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.util.LocaleUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.springframework.web.bind.annotation.ValueConstants.DEFAULT_NONE;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmni/terminologies")
public class BahmniConceptSearchController extends BaseRestController {

    private BahmniDiagnosisService bahmniDiagnosisService;

    private EmrConceptService emrService;

    private TerminologyLookupService terminologyLookupService;

    @Autowired
    public BahmniConceptSearchController(BahmniDiagnosisService bahmniDiagnosisService, EmrConceptService emrService, TerminologyLookupService terminologyLookupService) {
        this.bahmniDiagnosisService = bahmniDiagnosisService;
        this.emrService = emrService;
        this.terminologyLookupService = terminologyLookupService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "concepts")
    @ResponseBody
    public Object search(@RequestParam("term") String query, @RequestParam Integer limit,
                         @RequestParam(required = false, defaultValue = DEFAULT_NONE) String locale) throws Exception {
        boolean externalTerminologyServerLookupNeeded = bahmniDiagnosisService.isExternalTerminologyServerLookupNeeded();
        if(externalTerminologyServerLookupNeeded) {
            return terminologyLookupService.getResponseList(query, limit, locale);
        } else {
            return getDiagnosisConcepts(query, limit, locale);
        }
    }

    private List<SimpleObject> getDiagnosisConcepts(String query, Integer limit, String locale) {
        Collection<Concept> diagnosisSets = bahmniDiagnosisService.getDiagnosisSets();
        List<ConceptSource> conceptSources = bahmniDiagnosisService.getConceptSourcesForDiagnosisSearch();
        Locale searchLocale = getSearchLocale(locale);
        List<ConceptSearchResult> conceptSearchResults =
                emrService.conceptSearch(query, LocaleUtility.getDefaultLocale(), null, diagnosisSets, conceptSources, limit);
        ConceptSource conceptSource = conceptSources.isEmpty() ? null: conceptSources.get(0);
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
            if(diagnosis.getConceptName()!=null) {
                diagnosisObject.add("matchedName", diagnosis.getConceptName().getName());
            }
            ConceptReferenceTerm term = getConceptReferenceTermByConceptSource(diagnosis.getConcept(), conceptSource);
            if(term != null) {
                diagnosisObject.add("code", term.getCode());
            }
            allDiagnoses.add(diagnosisObject);
        }
        return allDiagnoses;
    }

    private ConceptReferenceTerm getConceptReferenceTermByConceptSource(Concept concept, ConceptSource conceptSource) {
        Collection<ConceptMap> conceptMappings = concept.getConceptMappings();
        if(conceptMappings != null && conceptSource != null) {
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
        }  catch (IllegalArgumentException e) {
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
        return Context.getMessageSourceService().getMessage(msgKey, new Object[] { localeStr }, Context.getLocale());
    }

}
