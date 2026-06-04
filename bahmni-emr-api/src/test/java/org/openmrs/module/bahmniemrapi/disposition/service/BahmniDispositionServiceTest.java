package org.openmrs.module.bahmniemrapi.disposition.service;

import org.bahmni.test.builder.ConceptBuilder;
import org.bahmni.test.builder.EncounterBuilder;
import org.bahmni.test.builder.ObsBuilder;
import org.bahmni.test.builder.VisitBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.disposition.contract.BahmniDisposition;
import org.openmrs.module.bahmniemrapi.disposition.mapper.BahmniDispositionMapper;
import org.openmrs.module.emrapi.encounter.DispositionMapper;
import org.openmrs.module.emrapi.encounter.EncounterProviderMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.matcher.ObservationTypeMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class BahmniDispositionServiceTest {

    @Mock
    private VisitService visitService;

    @Mock
    private DispositionMapper dispositionMapper;

    @Mock
    private ObservationTypeMatcher observationTypeMatcher;

    private Visit visit;

    private BahmniDispositionService bahmniDispositionService;

    @Mock
    private EncounterProviderMapper encounterProviderMapper;

    @Mock
    private BahmniDispositionMapper bahmniDispositionMapper;


    private Obs height = null;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Concept heightConcept = new ConceptBuilder().withName("HEIGHT").build();
        height = new ObsBuilder().withConcept(heightConcept).withValue(150.9).build();

        Set<Obs> allObs = new HashSet<>();
        allObs.add(height);

        Encounter encounter = new EncounterBuilder().build();
        encounter.setObs(allObs);

        visit = new VisitBuilder().withEncounter(encounter).build();

        bahmniDispositionService = new BahmniDispositionServiceImpl(visitService, dispositionMapper, observationTypeMatcher,
                encounterProviderMapper, bahmniDispositionMapper);

    }

    @Test
    public void shouldReturnEmptyDispositionListWhenVisitNotAvailable() {
        when(visitService.getVisitByUuid("visitUuid")).thenReturn(null);
        List<BahmniDisposition> actualDispositions = bahmniDispositionService.getDispositionByVisitUuid("visitUuid");

        Assert.assertNotNull(actualDispositions);
        assertEquals(0, actualDispositions.size());
    }

    @Test
    public void shouldReturnDispositionsWhenVisitIsValid() {

        Set<EncounterTransaction.Provider> eTProvider = new HashSet<>();
        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setName("Sample");
        provider.setUuid("uuid");
        eTProvider.add(provider);

        EncounterTransaction.Disposition eTDisposition = new EncounterTransaction.Disposition();
        eTDisposition.setCode("1234")
                .setExistingObs("a26a8c32-6fc1-4f5e-8a96-f5f5b05b87d")
                .setVoided(false)
                .setVoidReason(null)
                .setDispositionDateTime(new Date());

        eTDisposition.setConceptName("Absconding");
        eTDisposition.setAdditionalObs(new ArrayList<EncounterTransaction.Observation>());

        BahmniDisposition bahmniDisposition = new BahmniDisposition();
        bahmniDisposition.setCode("1234");

        when(visitService.getVisitByUuid("visitUuid")).thenReturn(visit);
        when(encounterProviderMapper.convert(new HashSet<EncounterProvider>())).thenReturn(eTProvider);
        when(observationTypeMatcher.getObservationType(height)).thenReturn(ObservationTypeMatcher.ObservationType.DISPOSITION);
        when(dispositionMapper.getDisposition(height)).thenReturn(eTDisposition);
        when(bahmniDispositionMapper.map(eTDisposition, eTProvider, null ,new Locale("en"))).thenReturn(bahmniDisposition);

        List<BahmniDisposition> actualDispositions = bahmniDispositionService.getDispositionByVisitUuid("visitUuid");

        assertEquals(1, actualDispositions.size());
        assertEquals(bahmniDisposition, actualDispositions.get(0));

    }

    @Test
    public void shouldReturnEmptyDispositionListWhenNoneOfObservationsAreDispositions() {
        Set<EncounterTransaction.Provider> eTProvider = new HashSet<>();
        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setName("Sample");
        provider.setUuid("uuid");
        eTProvider.add(provider);

        when(visitService.getVisitByUuid("visitUuid")).thenReturn(visit);
        when(encounterProviderMapper.convert(new HashSet<EncounterProvider>())).thenReturn(eTProvider);
        when(observationTypeMatcher.getObservationType(height)).thenReturn(ObservationTypeMatcher.ObservationType.DIAGNOSIS);

        List<BahmniDisposition> actualDispositions = bahmniDispositionService.getDispositionByVisitUuid("visitUuid");

        assertEquals(0, actualDispositions.size());
    }

    @Test
    public void shouldReturnEmptyDispositionListWhenObservationsAreVoided() {
        Set<EncounterTransaction.Provider> eTProvider = new HashSet<>();
        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setName("Sample");
        provider.setUuid("uuid");
        eTProvider.add(provider);

        when(visitService.getVisitByUuid("visitUuid")).thenReturn(visit);
        when(encounterProviderMapper.convert(new HashSet<EncounterProvider>())).thenReturn(eTProvider);
        when(observationTypeMatcher.getObservationType(height)).thenReturn(ObservationTypeMatcher.ObservationType.DISPOSITION);
        when(dispositionMapper.getDisposition(height)).thenReturn(null);


        List<BahmniDisposition> actualDispositions = bahmniDispositionService.getDispositionByVisitUuid("visitUuid", new Locale("en"));

        assertEquals(0, actualDispositions.size());
    }

    @Test
    public void shouldReturnDispositionForMultipleVisits() {
        Set<EncounterTransaction.Provider> eTProvider = new HashSet<>();
        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setName("Sample");
        provider.setUuid("uuid");
        eTProvider.add(provider);

        EncounterTransaction.Disposition eTDisposition = new EncounterTransaction.Disposition();
        eTDisposition.setCode("1234")
                .setExistingObs("a26a8c32-6fc1-4f5e-8a96-f5f5b05b87d")
                .setVoided(false)
                .setVoidReason(null)
                .setDispositionDateTime(new Date());

        eTDisposition.setConceptName("Absconding");
        eTDisposition.setAdditionalObs(new ArrayList<EncounterTransaction.Observation>());

        BahmniDisposition bahmniDisposition = new BahmniDisposition();
        bahmniDisposition.setCode("1234");


        when(encounterProviderMapper.convert(new HashSet<EncounterProvider>())).thenReturn(eTProvider);
        when(observationTypeMatcher.getObservationType(height)).thenReturn(ObservationTypeMatcher.ObservationType.DISPOSITION);
        when(dispositionMapper.getDisposition(height)).thenReturn(eTDisposition);
        when(bahmniDispositionMapper.map(eTDisposition, eTProvider, null , new Locale("en"))).thenReturn(bahmniDisposition);

        List<BahmniDisposition> actualDispositions = bahmniDispositionService.getDispositionByVisits(Arrays.asList(visit));

        assertEquals(1, actualDispositions.size());
        assertEquals(bahmniDisposition, actualDispositions.get(0));
    }
}
