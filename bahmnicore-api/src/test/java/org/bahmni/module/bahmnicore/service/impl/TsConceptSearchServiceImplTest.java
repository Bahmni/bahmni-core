package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.service.BahmniDiagnosisService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSearchResult;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.util.LocaleUtility;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;


import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore("javax.management.*")
public class TsConceptSearchServiceImplTest {


    @Mock
    BahmniDiagnosisService bahmniDiagnosisService;
    @Mock
    EmrConceptService emrConceptService;
    @InjectMocks
    TsConceptSearchServiceImpl tsConceptSearchService;
    @Mock
    private UserContext userContext;
    @Mock
    private AdministrationService administrationService;

    String searchTerm = "Malaria";
    int searchLimit = 20;
    String locale = LocaleUtility.getDefaultLocale().toString();
    List<Locale> locales =  Collections.singletonList(LocaleUtility.getDefaultLocale());
    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
        Locale defaultLocale = new Locale("en", "GB");
        when(Context.getLocale()).thenReturn(defaultLocale);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getAllowedLocales()).thenReturn(locales);
    }

    @Test
    public void shouldReturnEmptyListWhenExternalTerminologyServerLookUpIsEnabled() {
        when(bahmniDiagnosisService.isExternalTerminologyServerLookupNeeded()).thenReturn(true);
        List<SimpleObject> diagnosisList = tsConceptSearchService.getConcepts(searchTerm, searchLimit, locale);
        assertNotNull(diagnosisList);
        assertEquals(0, diagnosisList.size());
    }
    @Test
    public void shouldReturnListFromEmrConceptServiceWhenExternalTerminologyServerLookUpIsNotEnabled() {
        Concept malariaConcept = new Concept();
        ConceptName malariaConceptName = new ConceptName(searchTerm, LocaleUtility.getDefaultLocale());
        String malariaConceptUuid = "uuid1";
        malariaConcept.setUuid(malariaConceptUuid);
        malariaConcept.setFullySpecifiedName(malariaConceptName);
        malariaConcept.setPreferredName(malariaConceptName);
        ConceptSearchResult conceptSearchResult = new ConceptSearchResult(searchTerm, malariaConcept, malariaConceptName);

        when(emrConceptService.conceptSearch(searchTerm, LocaleUtility.getDefaultLocale(), null, Collections.EMPTY_LIST, Collections.EMPTY_LIST, searchLimit)).thenReturn(Collections.singletonList(conceptSearchResult));
        when(bahmniDiagnosisService.isExternalTerminologyServerLookupNeeded()).thenReturn(false);
        List<SimpleObject> diagnosisList = tsConceptSearchService.getConcepts(searchTerm, searchLimit, locale);
        assertNotNull(diagnosisList);
        assertEquals(1, diagnosisList.size());
        assertEquals(diagnosisList.get(0).get("conceptName"), searchTerm);
        assertEquals(diagnosisList.get(0).get("conceptUuid"), malariaConceptUuid);
    }

}