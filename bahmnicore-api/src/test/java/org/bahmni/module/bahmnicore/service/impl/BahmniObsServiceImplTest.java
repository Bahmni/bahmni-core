package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.module.bahmnicore.dao.VisitDao;
import org.bahmni.module.bahmnicore.dao.impl.ObsDaoImpl;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.bahmni.test.builder.ConceptBuilder;
import org.bahmni.test.builder.VisitBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.OMRSObsToBahmniObsMapper;
import org.openmrs.module.emrapi.encounter.matcher.ObservationTypeMatcher;
import org.openmrs.util.LocaleUtility;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LocaleUtility.class)
public class BahmniObsServiceImplTest {

    private BahmniObsService bahmniObsService;

    private String personUUID = "12345";

    @Mock
    private ObsDao obsDao;
    @Mock
    private VisitDao visitDao;
    @Mock
    private ObservationTypeMatcher observationTypeMatcher;
    @Mock
    private VisitService visitService;
    @Mock
    private ConceptService conceptService;
    @Mock
    private BahmniProgramWorkflowService bahmniProgramWorkflowService;
    @Mock
    private ObsService obsService;
    @Mock
    private OMRSObsToBahmniObsMapper omrsObsToBahmniObsMapper;

    @Before
    public void setUp() {
        initMocks(this);

        mockStatic(LocaleUtility.class);
        when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);
        when(observationTypeMatcher.getObservationType(any(Obs.class))).thenReturn(ObservationTypeMatcher.ObservationType.OBSERVATION);
        bahmniObsService = new BahmniObsServiceImpl(obsDao, omrsObsToBahmniObsMapper, visitService, conceptService, visitDao, bahmniProgramWorkflowService, obsService);
    }

    @Test
    public void shouldGetPersonObs() throws Exception {
        bahmniObsService.getObsForPerson(personUUID);
        verify(obsDao).getNumericObsByPerson(personUUID);
    }

    @Test
    public void shouldGetNumericConcepts() throws Exception {
        bahmniObsService.getNumericConceptsForPerson(personUUID);
        verify(obsDao).getNumericConceptsForPerson(personUUID);
    }

    @Test
    public void shouldGetObsByPatientUuidConceptNameAndNumberOfVisits() throws Exception {
        Concept bloodPressureConcept = new ConceptBuilder().withName("Blood Pressure").build();
        Integer numberOfVisits = 3;
        bahmniObsService.observationsFor(personUUID, asList(bloodPressureConcept), numberOfVisits, null, false, null, null, null);
        verify(obsDao).getObsByPatientAndVisit(personUUID, asList("Blood Pressure"),
                visitDao.getVisitIdsFor(personUUID, numberOfVisits), Integer.MAX_VALUE, ObsDaoImpl.OrderBy.DESC, null, false, null, null, null);
    }

    @Test
    public void shouldGetInitialObservations() throws Exception {
        Concept weightConcept = new ConceptBuilder().withName("Weight").build();
        Integer limit = 1;
        VisitBuilder visitBuilder = new VisitBuilder();
        Visit visit = visitBuilder.withUUID("visitId").withEncounter(new Encounter(1)).withPerson(new Person()).build();
        List<String> obsIgnoreList = new ArrayList<>();
        bahmniObsService.getInitialObsByVisit(visit, asList(weightConcept), obsIgnoreList, true);
        verify(obsDao).getObsByPatientAndVisit(visit.getPatient().getUuid(), asList("Weight"),
                asList(visit.getVisitId()), limit, ObsDaoImpl.OrderBy.ASC, obsIgnoreList, true, null, null, null);
    }

    @Test
    public void shouldGetAllObsForOrder() throws Exception {
        bahmniObsService.getObservationsForOrder("orderUuid");
        verify(obsDao, times(1)).getObsForOrder("orderUuid");
    }

    @Test
    public void shouldGetObsForPatientProgram() {
        Collection<Encounter> encounters = asList(new Encounter(), new Encounter());
        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(any(String.class))).thenReturn(encounters);
        Concept bloodPressureConcept = new ConceptBuilder().withName("Blood Pressure").build();
        Integer numberOfVisits = 3;

        bahmniObsService.observationsFor(personUUID, bloodPressureConcept, bloodPressureConcept, numberOfVisits, null, null, "patientProgramUuid");
        verify(obsDao).getObsFor(personUUID, bloodPressureConcept, bloodPressureConcept, visitDao.getVisitIdsFor(personUUID, numberOfVisits), encounters, null, null);
        verify(bahmniProgramWorkflowService).getEncountersByPatientProgramUuid("patientProgramUuid");
    }

    @Test
    public void shouldMakeACallToGetObservationsForEncounterAndConcepts() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        String encounterUuid = "encounterUuid";

        bahmniObsService.getObservationsForEncounter(encounterUuid, conceptNames);

        verify(obsDao, times(1)).getObsForConceptsByEncounter(encounterUuid, conceptNames);
    }

    @Test
    public void shouldReturnEmptyObservationListIfProgramDoesNotHaveEncounters() {
        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(any(String.class))).thenReturn(EMPTY_LIST);
        Concept bloodPressureConcept = new ConceptBuilder().withName("Blood Pressure").build();

        Collection<BahmniObservation> observations = bahmniObsService.observationsFor(personUUID, bloodPressureConcept, bloodPressureConcept, 3, null, null, "patientProgramUuid");

        verify(obsDao, times(0)).getObsFor(anyString(), any(Concept.class), any(Concept.class), any(List.class), any(Collection.class), any(Date.class), any(Date.class));
        assertThat(observations.size(), is(equalTo(0)));
    }

    @Test
    public void shouldCallObsServiceWithEmptyListOfEncountersWhenProgramUuidIsNull() {
        Concept bloodPressureConcept = new ConceptBuilder().withName("Blood Pressure").build();

        int numberOfVisits = 3;
        bahmniObsService.observationsFor(personUUID, bloodPressureConcept, bloodPressureConcept, numberOfVisits, null, null, null);

        verify(obsDao).getObsFor(personUUID, bloodPressureConcept, bloodPressureConcept, visitDao.getVisitIdsFor(personUUID, numberOfVisits), new ArrayList<Encounter>(), null, null);
    }

    @Test
    public void shouldGetObsbyPatientProgramUuid() throws Exception {
        String patientProgramUuid = "patientProgramUuid";
        ArrayList<String> conceptNames = new ArrayList<>();
        List<Obs> obs = new ArrayList<>();
        conceptNames.add("Paracetamol");
        Collection<Concept> names = new ArrayList<Concept>() {{add(null);}};

        bahmniObsService.getObservationsForPatientProgram(patientProgramUuid, conceptNames, null);

        verify(obsDao).getObsByPatientProgramUuidAndConceptNames(patientProgramUuid, asList("Paracetamol"),  null, ObsDaoImpl.OrderBy.DESC, null, null);
        verify(omrsObsToBahmniObsMapper, times(1)).map(obs, names);
    }

    @Test
    public void shouldGetLatestObsbyPatientProgramUuid() throws Exception {
        String patientProgramUuid = "patientProgramUuid";
        List<String> conceptNames = new ArrayList<>();
        conceptNames.add("Paracetamol");
        List<Obs> obs = new ArrayList<>();
        Collection<Concept> names = new ArrayList<Concept>() {{add(null);}};

        bahmniObsService.getLatestObservationsForPatientProgram(patientProgramUuid, conceptNames, null);

        verify(obsDao).getObsByPatientProgramUuidAndConceptNames(patientProgramUuid, asList("Paracetamol"),  null, ObsDaoImpl.OrderBy.DESC, null, null);
        verify(omrsObsToBahmniObsMapper, times(1)).map(obs, names);
    }

    @Test
    public void shouldGetInitialObsbyPatientProgramUuid() throws Exception {
        String patientProgramUuid = "patientProgramUuid";
        List<String> conceptNames = new ArrayList<>();
        conceptNames.add("Paracetamol");
        List<Obs> obs = new ArrayList<>();
        Collection<Concept> names = new ArrayList<Concept>() {{add(null);}};

        bahmniObsService.getInitialObservationsForPatientProgram(patientProgramUuid, conceptNames, null);

        verify(obsDao).getObsByPatientProgramUuidAndConceptNames(patientProgramUuid, asList("Paracetamol"), 1, ObsDaoImpl.OrderBy.ASC, null, null);
        verify(omrsObsToBahmniObsMapper, times(1)).map(obs, names);
    }

    @Test
    public void shouldGetBahmniObservationByObservationUuid() throws Exception {
        String observationUuid = "observationUuid";
        Obs obs = new Obs();
        BahmniObservation expectedBahmniObservation = new BahmniObservation();
        when(obsService.getObsByUuid(observationUuid)).thenReturn(obs);
        when(omrsObsToBahmniObsMapper.map(obs, null)).thenReturn(expectedBahmniObservation);

        BahmniObservation actualBahmniObservation = bahmniObsService.getBahmniObservationByUuid(observationUuid);

        verify(obsService, times(1)).getObsByUuid(observationUuid);
        verify(omrsObsToBahmniObsMapper, times(1)).map(obs, null);
        assertNotNull(actualBahmniObservation);
        assertEquals(expectedBahmniObservation, actualBahmniObservation);
    }

    @Test
    public void shouldCallGetObsForFormBuilderFormsWithEncountersAndVisits() {
        String patientUuid = "patient-uuid";
        String patientProgramUuid = "patient-program-uuid";
        int numberOfVisits = 2;
        List<Integer> visitIds = asList(100, 101);
        List<String> formNames = singletonList("First Aid Form");
        List<Encounter> encounters = singletonList(mock(Encounter.class));

        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(patientProgramUuid))
                .thenReturn(encounters);
        when(visitDao.getVisitIdsFor(patientUuid, numberOfVisits)).thenReturn(visitIds);
        when(obsDao.getObsForFormBuilderForms(patientUuid, formNames, visitIds, encounters, null, null))
                .thenReturn(EMPTY_LIST);

        bahmniObsService.getObsForFormBuilderForms(patientUuid, formNames, numberOfVisits, null, null, patientProgramUuid);

        verify(bahmniProgramWorkflowService).getEncountersByPatientProgramUuid(patientProgramUuid);
        verify(visitDao).getVisitIdsFor(patientUuid, numberOfVisits);
        verify(obsDao).getObsForFormBuilderForms(patientUuid, formNames, visitIds, encounters, null, null);
    }

    @Test
    public void shouldReturnBahmniObservationWhenGetObsForFormBuilderFormsCalled() {
        String patientUuid = "patient-uuid";
        String patientProgramUuid = "patient-program-uuid";
        int numberOfVisits = 2;
        List<Integer> visitIds = asList(100, 101);
        List<String> formNames = singletonList("First Aid Form");
        List<Encounter> encounters = singletonList(mock(Encounter.class));
        Obs observation = mock(Obs.class);
        BahmniObservation bahmniObservation = mock(BahmniObservation.class);

        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(patientProgramUuid))
                .thenReturn(encounters);
        when(visitDao.getVisitIdsFor(patientUuid, numberOfVisits)).thenReturn(visitIds);
        when(obsDao.getObsForFormBuilderForms(patientUuid, formNames, visitIds, encounters, null, null))
                .thenReturn(singletonList(observation));
        when(omrsObsToBahmniObsMapper.map(observation, null)).thenReturn(bahmniObservation);

        Collection<BahmniObservation> bahmniObservations = bahmniObsService.getObsForFormBuilderForms(patientUuid,
                formNames, numberOfVisits, null, null, patientProgramUuid);

        assertEquals(1, bahmniObservations.size());
        assertEquals(bahmniObservation, bahmniObservations.iterator().next());
    }
}
