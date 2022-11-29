package org.openmrs.module.bahmniemrapi.encountertransaction.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.impl.BahmniVisitAttributeService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterSearchParameters;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationService;
import org.openmrs.module.emrapi.encounter.EncounterParameters;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.matcher.BaseEncounterMatcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniEncounterTransactionServiceImplTest {

    @Mock
    private BaseEncounterMatcher baseEncounterMatcher;

    @Mock
    private VisitService visitService;

    @Mock
    private LocationService locationService;

    @Mock
    private EncounterService encounterService;

    @Mock
    private PatientService patientService;

    @Mock
    private EncounterTransactionMapper encounterTransactionMapper;

    @Mock
    private BahmniVisitLocationService bahmniVisitLocationService;

    @Mock
    private BahmniVisitAttributeService bahmniVisitAttributeService;

    private BahmniEncounterTransactionService bahmniEncounterTransactionService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        bahmniEncounterTransactionService = new BahmniEncounterTransactionServiceImpl(encounterService,null,encounterTransactionMapper,null,null, null,visitService,patientService
                ,locationService,null,baseEncounterMatcher,bahmniVisitLocationService, bahmniVisitAttributeService);

    }

    @Test
    public void shouldReturnTheEncounterFromTheVisitThatIsOpenedInThatVisitLocation() throws Exception {

        Patient patient = new Patient();

        EncounterTransaction encounterTransaction = new EncounterTransaction();
        encounterTransaction.setEncounterUuid("encounter-uuid");

        Location loginLocation = new Location();
        loginLocation.setUuid("login-location-uuid");

        Location location = new Location();
        location.setUuid("visit-location-uuid");
        Visit visit = new Visit();
        visit.setLocation(location);
        visit.setUuid("visit-uuid");

        Encounter encounter = new Encounter();
        encounter.setLocation(location);
        encounter.setUuid("encounter-uuid");
        HashSet<Encounter> encounters = new HashSet<>();
        encounters.add(encounter);
        visit.setEncounters(encounters);

        BahmniEncounterSearchParameters encounterSearchParameters = new BahmniEncounterSearchParameters();
        encounterSearchParameters.setLocationUuid(loginLocation.getUuid());
        encounterSearchParameters.setPatientUuid("patient-uuid");
        encounterSearchParameters.setEncounterTypeUuids(Arrays.asList("encounter-type-uuid"));
        when(baseEncounterMatcher.findEncounter(any(Visit.class), any(EncounterParameters.class))).thenReturn(encounter);
        List<Visit> visits = Arrays.asList(visit);
        when(locationService.getLocationByUuid(loginLocation.getUuid())).thenReturn(loginLocation);
        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(patient);
        when(visitService.getActiveVisitsByPatient(any(Patient.class))).thenReturn(visits);
        when(encounterService.getEncounterByUuid(anyString())).thenReturn(null);
        when(bahmniVisitLocationService.getVisitLocationUuid(anyString())).thenReturn("visit-location-uuid");
        when(bahmniVisitLocationService.getMatchingVisitInLocation(visits, "login-location-uuid")).thenReturn(visit);
        when(encounterTransactionMapper.map(any(Encounter.class),anyBoolean())).thenReturn(encounterTransaction);
        bahmniEncounterTransactionService.find(encounterSearchParameters);
        ArgumentCaptor<Visit> argumentCaptor = ArgumentCaptor.forClass(Visit.class);
        ArgumentCaptor<EncounterParameters> argument = ArgumentCaptor.forClass(EncounterParameters.class);
        verify(baseEncounterMatcher).findEncounter(argumentCaptor.capture(), argument.capture());
        assertEquals(argumentCaptor.getValue().getUuid(), "visit-uuid");
    }

    @Test
    public void shouldReturnTheEncounterFromTheVisitThatIsOpenedInThatVisitLocationIfThereAreTwoVisitsInDiffLocations() throws Exception {
        Patient patient = new Patient();

        EncounterTransaction encounterTransaction = new EncounterTransaction();
        encounterTransaction.setEncounterUuid("encounter-uuid");

        Location loginLocation = new Location();
        loginLocation.setUuid("login-location-uuid");

        Location location = new Location();
        location.setUuid("visit-location-uuid");

        Visit visitOne = new Visit();
        visitOne.setLocation(location);
        visitOne.setUuid("visit-uuid-one");

        Location locationTwo = new Location();
        locationTwo.setUuid("visit-location-uuid-two");

        Visit visitTwo = new Visit();
        visitTwo.setUuid("visit-uuid-two");
        visitTwo.setLocation(locationTwo);

        Encounter encounter = new Encounter();
        encounter.setLocation(location);
        encounter.setUuid("encounter-uuid");
        HashSet<Encounter> encounters = new HashSet<>();
        encounters.add(encounter);
        visitTwo.setEncounters(encounters);

        BahmniEncounterSearchParameters encounterSearchParameters = new BahmniEncounterSearchParameters();
        encounterSearchParameters.setLocationUuid("login-location-uuid");
        encounterSearchParameters.setPatientUuid("patient-uuid");
        encounterSearchParameters.setEncounterTypeUuids(Arrays.asList("encounter-type-uuid"));
        when(locationService.getLocationByUuid(loginLocation.getUuid())).thenReturn(loginLocation);
        when(baseEncounterMatcher.findEncounter(any(Visit.class), any(EncounterParameters.class))).thenReturn(encounter);
        List<Visit> visits = Arrays.asList(visitOne, visitTwo);
        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(patient);
        when(visitService.getActiveVisitsByPatient(any(Patient.class))).thenReturn(visits);
        when(encounterService.getEncounterByUuid(anyString())).thenReturn(null);
        when(bahmniVisitLocationService.getVisitLocationUuid(anyString())).thenReturn("visit-location-uuid-two");
        when(bahmniVisitLocationService.getMatchingVisitInLocation(visits, "login-location-uuid")).thenReturn(visitTwo);

        bahmniEncounterTransactionService.find(encounterSearchParameters);
        ArgumentCaptor<Visit> argumentCaptor = ArgumentCaptor.forClass(Visit.class);
        ArgumentCaptor<EncounterParameters> argument = ArgumentCaptor.forClass(EncounterParameters.class);
        verify(baseEncounterMatcher).findEncounter(argumentCaptor.capture(), argument.capture());
        assertEquals(argumentCaptor.getValue().getUuid(), "visit-uuid-two");
    }

}
