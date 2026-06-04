package org.bahmni.module.bahmnicore.matcher;

import org.apache.commons.lang3.time.DateUtils;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.api.impl.EncounterServiceImpl;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.EncounterTypeIdentifier;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationService;
import org.openmrs.module.emrapi.encounter.EncounterParameters;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore( {"javax.management.*"} )
public class EncounterSessionMatcherTest {
    @Mock
    private AdministrationService administrationService;
    @Mock
    private EncounterTypeIdentifier encounterTypeIdentifier;
    @Mock
    private EncounterServiceImpl encounterService;
    private Set<Provider> providers;
    private Set<EncounterProvider> encounterProviders;
    private User creator;
    @Mock
    private UserContext userContext;
    private EncounterType encounterType;
    @Mock
    private Encounter encounter;
    private Person person;
    private Patient patient;
    private Visit visit;
    private EncounterSessionMatcher encounterSessionMatcher;
    private Location location;
    @Mock
    private BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Mock
    private BahmniVisitLocationService bahmniVisitLocationService;

    @Mock
    private EpisodeService episodeService;

    @Before
    public void setUp(){
        initMocks(this);
        encounterSessionMatcher = new EncounterSessionMatcher(administrationService, encounterTypeIdentifier, encounterService, bahmniProgramWorkflowService, episodeService, bahmniVisitLocationService);
        visit = new Visit();
        visit.setId(3);

        providers = new HashSet<>();
        Provider provider = new Provider();
        provider.setId(1234);
        provider.setProviderId(1234);
        providers.add(provider);

        encounterProviders = new HashSet<>();
        EncounterProvider encounterProvider = new EncounterProvider();
        encounterProvider.setProvider(provider);
        encounterProviders.add(encounterProvider);

        encounterType = new EncounterType("Test", "Test");

        location = new Location();
        location.setUuid("location-uuid");

        creator = new User(person);
        creator.setId(1234);

        encounter = mock(Encounter.class);

        Encounter encounterOne = new Encounter();
        encounterOne.setLocation(location);
        encounterOne.setCreator(creator);
        encounterOne.setEncounterDatetime(new Date());

        person = new Person();
        person.setId(1234);
        provider.setPerson(person);
        location = new Location();
        location.setUuid("location");

        patient = new Patient();
        patient.setId(1111);
        patient.setUuid("patient_uuid");

        userContext = mock(UserContext.class);

        when(administrationService.getGlobalProperty("bahmni.encountersession.duration")).thenReturn("60");
        when(administrationService.getGlobalProperty("bahmni.encounterType.default")).thenReturn("Consultation");
        when(encounter.getCreator()).thenReturn(creator);
        when(encounter.getEncounterDatetime()).thenReturn(new Date());

        PowerMockito.mockStatic(Context.class);
        BDDMockito.given(Context.getUserContext()).willReturn(userContext);

        when(userContext.getAuthenticatedUser()).thenReturn(creator);

        when(encounterService.getEncounters(any(Patient.class), eq(null),
                any(Date.class), any(Date.class), any(Collection.class),
                any(Collection.class), any(Collection.class), eq(null),
                any(Collection.class), eq(false))).thenReturn(Arrays.asList(encounterOne));
        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(null)).thenReturn(Collections.<Encounter>emptyList());
    }

    @Test
    public void shouldReturnEncounterOfDefaultTypeIfEncounterParameterDoesNotHaveEncounterTypeAndLocationIsNotSet(){
        visit.addEncounter(encounter);
        EncounterType defaultEncounterType = new EncounterType();
        when(encounter.getEncounterType()).thenReturn(defaultEncounterType);
        when(encounter.getDateChanged()).thenReturn(new Date());
        when(encounter.getLocation()).thenReturn(null);
        when(encounter.getEncounterProviders()).thenReturn(encounterProviders);
        when(encounter.getCreator()).thenReturn(creator);
        when(encounterTypeIdentifier.getDefaultEncounterType()).thenReturn(defaultEncounterType);
        when(bahmniVisitLocationService.getVisitLocation(any(String.class))).thenReturn(location);


        Encounter encounterReturned = encounterSessionMatcher.findEncounter(visit, getEncounterParameters(providers, location, null));

        assertNotNull(encounterReturned);
        assertTrue(encounter.getEncounterType().equals(defaultEncounterType));
    }

    @Test
    public void shouldGetEncounter() throws ParseException {
        EncounterParameters encounterParameters = getEncounterParameters(providers, location);
        Date encounterDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-05-21 13:05:00");
        encounterParameters.setEncounterDateTime(encounterDate);
        encounterParameters.setLocation(new Location(1));

        when(bahmniVisitLocationService.getVisitLocation(any(String.class))).thenReturn(location);
        Encounter encounterReturned = encounterSessionMatcher.findEncounter(visit, encounterParameters);

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        ArgumentCaptor<Location> locationArgumentCaptor = ArgumentCaptor.forClass(Location.class);
        ArgumentCaptor<Date> dateArgumentCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Collection> collectionArgumentCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(encounterService).getEncounters(patientArgumentCaptor.capture(), locationArgumentCaptor.capture(), dateArgumentCaptor.capture(), dateArgumentCaptor.capture(), collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(),collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(), eq(false));
        assertEquals(encounterDate, dateArgumentCaptor.getAllValues().get(1));
        assertEquals(DateUtils.addMinutes(encounterDate, -60), dateArgumentCaptor.getAllValues().get(0));
        assertNotNull(encounterReturned);
    }

    @Test
    public void shouldReturnNullWhenNewlyCreatedVisitIsPassedEncounter(){
        EncounterParameters encounterParameters = getEncounterParameters(providers, location);
        encounterParameters.setEncounterDateTime(DateUtils.addDays(new Date(), -10));

        Encounter encounterReturned = encounterSessionMatcher.findEncounter(new Visit(), encounterParameters);
        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        ArgumentCaptor<Location> locationArgumentCaptor = ArgumentCaptor.forClass(Location.class);
        ArgumentCaptor<Date> dateArgumentCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Collection> collectionArgumentCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(encounterService, times(0)).getEncounters(patientArgumentCaptor.capture(), locationArgumentCaptor.capture(), dateArgumentCaptor.capture(), dateArgumentCaptor.capture(), collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(),collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(), eq(false));
        assertNull(encounterReturned);
    }

    @Test
    public void shouldGetEncounterFromSameDay(){
        EncounterParameters encounterParameters = getEncounterParameters(providers, location);
        Date encounterDateTime = DateUtils.addMinutes(DateUtils.truncate(new Date(), Calendar.DATE), 15);
        encounterParameters.setEncounterDateTime(encounterDateTime);

        when(bahmniVisitLocationService.getVisitLocation(any(String.class))).thenReturn(location);
        Encounter encounterReturned = encounterSessionMatcher.findEncounter(visit, encounterParameters);
        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        ArgumentCaptor<Location> locationArgumentCaptor = ArgumentCaptor.forClass(Location.class);
        ArgumentCaptor<Date> dateArgumentCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Collection> collectionArgumentCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(encounterService).getEncounters(patientArgumentCaptor.capture(), locationArgumentCaptor.capture(), dateArgumentCaptor.capture(), dateArgumentCaptor.capture(), collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(), eq(false));
        assertEquals(DateUtils.truncate(encounterParameters.getEncounterDateTime(), Calendar.DATE), dateArgumentCaptor.getAllValues().get(0));
        assertEquals(encounterParameters.getEncounterDateTime(), dateArgumentCaptor.getAllValues().get(1));
        assertNotNull(encounterReturned);
    }

    @Test
    public void shouldGetRetrospectiveEncounter(){
        EncounterParameters encounterParameters = getEncounterParameters(providers, location);
        encounterParameters.setEncounterDateTime(DateUtils.truncate(new Date(), Calendar.DATE));

        when(bahmniVisitLocationService.getVisitLocation(any(String.class))).thenReturn(location);
        Encounter encounterReturned = encounterSessionMatcher.findEncounter(visit, encounterParameters);
        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        ArgumentCaptor<Location> locationArgumentCaptor = ArgumentCaptor.forClass(Location.class);
        ArgumentCaptor<Date> dateArgumentCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Collection> collectionArgumentCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(encounterService).getEncounters(patientArgumentCaptor.capture(), locationArgumentCaptor.capture(), dateArgumentCaptor.capture(), dateArgumentCaptor.capture(), collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(), collectionArgumentCaptor.capture(), eq(false));
        assertEquals(dateArgumentCaptor.getAllValues().get(0), dateArgumentCaptor.getAllValues().get(1));
        assertEquals(encounterParameters.getEncounterDateTime(), dateArgumentCaptor.getAllValues().get(0));
        assertNotNull(encounterReturned);
    }

    @Test
    public void shouldMatchEncounterBasedOnUserWhenNoProviderIsSupplied(){
        EncounterParameters encounterParameters = getEncounterParameters(null, location);
        encounterParameters.setEncounterDateTime(DateUtils.truncate(new Date(), Calendar.DATE));


        Encounter e1 = new Encounter();
        User creator1 = new User(1);
        e1.setCreator(creator1);
        e1.setLocation(location);

        Encounter e2 = new Encounter();
        User creator2 = new User(2);
        e2.setCreator(creator2);
        e2.setLocation(location);

        when(userContext.getAuthenticatedUser()).thenReturn(creator1);
        when(encounterService.getEncounters(any(Patient.class), eq(null), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), eq(null), eq(null), eq(null), eq(false))).thenReturn(Arrays.asList(e1, e2));
        when(bahmniVisitLocationService.getVisitLocation(any(String.class))).thenReturn(location);

        Encounter encounterReturned = encounterSessionMatcher.findEncounter(null, encounterParameters);
        assertNotNull(encounterReturned);
        assertEquals(encounterReturned.getCreator(), creator1);
    }

    @Test
    public void shouldNotReturnExistingEncounterIfItDoesNotMatchPatientProgram() {
        EncounterParameters encounterParameters = getEncounterParameters(null, location);
        HashMap<String, Object> context = new HashMap<>();
        String patientProgramUuid = "94393942-dc4d-11e5-b5d2-0a1d41d68578";
        context.put("patientProgramUuid", patientProgramUuid);
        encounterParameters.setContext(context);

        encounterParameters.setEncounterDateTime(DateUtils.truncate(new Date(), Calendar.DATE));

        Encounter e1 = new Encounter();
        User creator1 = new User(1);
        e1.setCreator(creator1);

        when(userContext.getAuthenticatedUser()).thenReturn(creator1);
        when(encounterService.getEncounters(any(Patient.class), any(Location.class), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), eq(false)))
                .thenReturn(Arrays.asList(e1));
        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(patientProgramUuid)).thenReturn(Collections.<Encounter>emptyList());

        Encounter encounterReturned = encounterSessionMatcher.findEncounter(null, encounterParameters);
        assertNull(encounterReturned);
    }

    @Test
    public void shouldThrowExceptionWhenMultipleEncountersAreMatched() throws Exception {
        EncounterParameters encounterParameters = getEncounterParameters(null, location);
        encounterParameters.setEncounterDateTime(DateUtils.truncate(new Date(), Calendar.DATE));

        Encounter e1 = new Encounter();
        User creator1 = new User(1);
        e1.setCreator(creator1);
        e1.setLocation(location);

        Encounter e2 = new Encounter();
        e2.setCreator(creator1);
        e2.setLocation(location);

        when(userContext.getAuthenticatedUser()).thenReturn(creator1);
        when(encounterService.getEncounters(any(Patient.class), any(Location.class), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), eq(false))).thenReturn(Arrays.asList(e1, e2));
        when(bahmniVisitLocationService.getVisitLocation(any(String.class))).thenReturn(location);

        try {
            encounterSessionMatcher.findEncounter(null, encounterParameters);
            assertFalse("should not have matched encounter", false);
        }catch (RuntimeException e){
           assertEquals("More than one encounter matches the criteria", e.getMessage());
        }
    }

    @Test
    public void shouldReturnNullIfProgramUuidIsNotSpecifiedAndOnlyEncountersRelatedToProgramsAreOpen(){
        EncounterParameters encounterParameters = getEncounterParameters(null, location);
        HashMap<String, Object> context = new HashMap<>();
        context.put("patientProgramUuid", null);
        encounterParameters.setContext(context);

        encounterParameters.setEncounterDateTime(DateUtils.truncate(new Date(), Calendar.DATE));

        Encounter e1 = new Encounter();
        User creator1 = new User(1);
        e1.setCreator(creator1);
        List<Encounter> encounters = new ArrayList<>();
        encounters.add(e1);

        when(userContext.getAuthenticatedUser()).thenReturn(creator1);
        when(encounterService.getEncounters(any(Patient.class), eq(null), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), eq(null), eq(null), eq(null), eq(false)))
                .thenReturn(encounters);
        when(episodeService.getEpisodeForEncounter(e1)).thenReturn(new Episode());

        Encounter encounterReturned = encounterSessionMatcher.findEncounter(null, encounterParameters);

        verify(episodeService, times(1)).getEpisodeForEncounter(e1);
        assertThat(encounterReturned, is(nullValue()));
    }

    @Test
    public void shouldRemoveAllEncountersAssociatedWithEpisodes(){
        EncounterParameters encounterParameters = getEncounterParameters(null, location);
        HashMap<String, Object> context = new HashMap<>();
        context.put("patientProgramUuid", null);
        encounterParameters.setContext(context);

        encounterParameters.setEncounterDateTime(DateUtils.truncate(new Date(), Calendar.DATE));

        Encounter e1 = new Encounter();
        User creator1 = new User(1);
        e1.setCreator(creator1);
        e1.setLocation(location);
        Encounter e2 = new Encounter();
        e2.setCreator(creator1);
        e2.setLocation(location);
        List<Encounter> encounters = new ArrayList<>();
        encounters.add(e1);
        encounters.add(e2);

        when(bahmniVisitLocationService.getVisitLocation(any(String.class))).thenReturn(location);
        when(userContext.getAuthenticatedUser()).thenReturn(creator1);
        when(encounterService.getEncounters(any(Patient.class), eq(null), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), eq(null), eq(null), eq(null), eq(false)))
                .thenReturn(encounters);
        when(episodeService.getEpisodeForEncounter(e1)).thenReturn(new Episode());

        Encounter encounterReturned = encounterSessionMatcher.findEncounter(null, encounterParameters);

        verify(episodeService, times(1)).getEpisodeForEncounter(e1);
        verify(episodeService, times(1)).getEpisodeForEncounter(e2);
        assertThat(encounterReturned, is(equalTo(e2)));
    }

    @Test
    public void shouldReturnTheEncountersPresentInCurrentVisitLocation() {
        EncounterParameters encounterParameters = getEncounterParameters(null, location);
        Location loginLocation = new Location();
        loginLocation.setUuid("login-location");
        Location encounterLocation = new Location();
        encounterLocation.setUuid("encounter-location");

        encounterParameters.setContext(null);

        encounterParameters.setEncounterDateTime(DateUtils.truncate(new Date(), Calendar.DATE));
        encounterParameters.setLocation(loginLocation);

        Encounter e1 = new Encounter();
        User creator1 = new User(1);
        e1.setCreator(creator1);
        e1.setLocation(encounterLocation);

        Location otherLocation = new Location();
        otherLocation.setUuid("other-location-uuid");
        Encounter e2 = new Encounter();
        e2.setCreator(creator1);
        e2.setLocation(otherLocation);


        when(userContext.getAuthenticatedUser()).thenReturn(creator1);
        when(encounterService.getEncounters(any(Patient.class), eq(null), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), eq(null), eq(null), eq(null), eq(false)))
                .thenReturn(Arrays.asList(e1));
        when(bahmniVisitLocationService.getVisitLocation(loginLocation.getUuid())).thenReturn(loginLocation);
        when(bahmniVisitLocationService.getVisitLocation(encounterLocation.getUuid())).thenReturn(loginLocation);
        when(bahmniVisitLocationService.getVisitLocation(otherLocation.getUuid())).thenReturn(otherLocation);

        Encounter encounterReturned = encounterSessionMatcher.findEncounter(null, encounterParameters);
        assertNotNull(encounterReturned);
    }

    @Test
    public void shouldReturnNullIfThereIsNoEncounterInCurrentVisitLocation() {
        EncounterParameters encounterParameters = getEncounterParameters(null, location);
        Location loginLocation = new Location();
        loginLocation.setUuid("login-location");
        Location encounterLocation = new Location();
        encounterLocation.setUuid("encounter-location");

        encounterParameters.setContext(null);

        encounterParameters.setEncounterDateTime(DateUtils.truncate(new Date(), Calendar.DATE));
        encounterParameters.setLocation(loginLocation);

        Encounter e1 = new Encounter();
        User creator1 = new User(1);
        e1.setCreator(creator1);
        e1.setLocation(encounterLocation);


        when(userContext.getAuthenticatedUser()).thenReturn(creator1);
        when(encounterService.getEncounters(any(Patient.class), any(Location.class), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), eq(false)))
                .thenReturn(Arrays.asList(e1));
        when(bahmniVisitLocationService.getVisitLocation(loginLocation.getUuid())).thenReturn(loginLocation);
        when(bahmniVisitLocationService.getVisitLocation(encounterLocation.getUuid())).thenReturn(encounterLocation);

        Encounter encounterReturned = encounterSessionMatcher.findEncounter(null, encounterParameters);
        assertNull(encounterReturned);
    }

    @Test
    public void shouldNotReturnEncouterIfItsLocationIsNull() {
        EncounterParameters encounterParameters = getEncounterParameters(null, location);
        Location loginLocation = new Location();
        loginLocation.setUuid("login-location");

        encounterParameters.setContext(null);

        encounterParameters.setEncounterDateTime(DateUtils.truncate(new Date(), Calendar.DATE));
        encounterParameters.setLocation(loginLocation);

        Encounter e1 = new Encounter();
        User creator1 = new User(1);
        e1.setCreator(creator1);
        e1.setLocation(null);


        when(userContext.getAuthenticatedUser()).thenReturn(creator1);
        when(encounterService.getEncounters(any(Patient.class), any(Location.class), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), eq(false)))
                .thenReturn(Arrays.asList(e1));
        when(bahmniVisitLocationService.getVisitLocation(loginLocation.getUuid())).thenReturn(loginLocation);

        Encounter encounterReturned = encounterSessionMatcher.findEncounter(null, encounterParameters);
        assertNull(encounterReturned);
    }

    private EncounterParameters getEncounterParameters(Set<Provider> providers, Location location) {
        return getEncounterParameters(providers, location, this.encounterType);
    }

    private EncounterParameters getEncounterParameters(Set<Provider> providers, Location location, EncounterType encounterType) {
        EncounterParameters encounterParameters =  EncounterParameters.instance();
        encounterParameters.setPatient(patient);
        encounterParameters.setEncounterType(encounterType);
        encounterParameters.setProviders(providers);
        encounterParameters.setLocation(location);
        return encounterParameters;
    }
}
