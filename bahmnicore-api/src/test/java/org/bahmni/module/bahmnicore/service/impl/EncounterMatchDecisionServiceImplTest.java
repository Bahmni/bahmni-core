package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.lang3.time.DateUtils;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationService;
import org.bahmni.module.bahmnicore.matcher.EncounterSessionMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.EncounterMatchRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.EncounterMatchResponse;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.EncounterTypeIdentifier;
import org.openmrs.module.emrapi.encounter.EncounterParameters;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
public class EncounterMatchDecisionServiceImplTest {

    @Mock
    private VisitService visitService;
    @Mock
    private PatientService patientService;
    @Mock
    private LocationService locationService;
    @Mock
    private ProviderService providerService;
    @Mock
    private EncounterService encounterService;
    @Mock
    private EncounterSessionMatcher encounterSessionMatcher;
    @Mock
    private EncounterTypeIdentifier encounterTypeIdentifier;
    @Mock
    private BahmniVisitLocationService bahmniVisitLocationService;
    @Mock
    private AdministrationService administrationService;

    private EncounterMatchDecisionServiceImpl service;

    private Visit activeVisit;
    private Patient patient;
    private Location location;
    private EncounterType encounterType;
    private Provider provider;

    @Before
    public void setUp() {
        initMocks(this);
        service = new EncounterMatchDecisionServiceImpl(
                visitService, patientService, locationService, providerService,
                encounterService, encounterSessionMatcher, encounterTypeIdentifier,
                bahmniVisitLocationService, administrationService);

        activeVisit = new Visit();
        activeVisit.setId(1);
        activeVisit.setUuid("visit-uuid");

        patient = new Patient();
        patient.setUuid("patient-uuid");

        location = new Location();
        location.setUuid("location-uuid");
        location.setName("OPD");

        encounterType = new EncounterType("Consultation", "Default");
        encounterType.setUuid("enc-type-uuid");

        provider = new Provider();
        provider.setUuid("provider-uuid");
        provider.setName("Dr. Smith");

        when(administrationService.getGlobalProperty("bahmni.encountersession.duration")).thenReturn("60");
        when(visitService.getVisitByUuid("visit-uuid")).thenReturn(activeVisit);
        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(patient);
        when(locationService.getLocationByUuid("location-uuid")).thenReturn(location);
        when(encounterTypeIdentifier.getDefaultEncounterType()).thenReturn(encounterType);
    }

    // --- Helper methods ---

    private EncounterMatchRequest buildRequest() {
        EncounterMatchRequest request = new EncounterMatchRequest();
        request.setVisitUuid("visit-uuid");
        request.setPatientUuid("patient-uuid");
        request.setLocationUuid("location-uuid");
        request.setEncounterTypeUuids(Arrays.asList("enc-type-uuid"));
        request.setProviderUuids(Arrays.asList("provider-uuid"));
        return request;
    }

    private Encounter buildEncounter(Date encounterDatetime) {
        Encounter encounter = new Encounter();
        encounter.setUuid("enc-uuid");
        encounter.setEncounterDatetime(encounterDatetime);
        encounter.setEncounterType(encounterType);
        encounter.setLocation(location);

        Provider p = new Provider();
        p.setUuid("provider-uuid");
        p.setName("Dr. Smith");
        EncounterProvider ep = new EncounterProvider();
        ep.setProvider(p);
        Set<EncounterProvider> eps = new HashSet<EncounterProvider>();
        eps.add(ep);
        encounter.setEncounterProviders(eps);

        return encounter;
    }

    // AC 1: Match found — same provider, same location, within session
    @Test
    public void match_found_whenProviderLocationAndSessionMatch() {
        EncounterMatchRequest request = buildRequest();
        Encounter matchedEncounter = buildEncounter(new Date());

        when(encounterService.getEncounterTypeByUuid("enc-type-uuid")).thenReturn(encounterType);
        when(providerService.getProviderByUuid("provider-uuid")).thenReturn(provider);
        when(encounterSessionMatcher.findEncounter(eq(activeVisit), any(EncounterParameters.class)))
                .thenReturn(matchedEncounter);

        EncounterMatchResponse response = service.decideMatch(request);

        assertEquals("match_found", response.getStatus());
        assertEquals("enc-uuid", response.getEncounterUuid());
        assertNotNull(response.getMatchDetails());
        assertEquals(Boolean.TRUE, response.getMatchDetails().getProviderMatched());
        assertEquals(Boolean.TRUE, response.getMatchDetails().getLocationMatched());
        assertEquals(Boolean.TRUE, response.getMatchDetails().getWithinSessionDuration());
        assertEquals(Integer.valueOf(60), response.getMatchDetails().getSessionDurationMinutes());
        assertNotNull(response.getEncounterType());
        assertNotNull(response.getLocation());
    }

    // AC 2: Provider mismatch
    @Test
    public void no_match_whenProviderDiffers() {
        EncounterMatchRequest request = buildRequest();

        Provider requestedProvider = new Provider();
        requestedProvider.setUuid("provider-uuid");
        when(providerService.getProviderByUuid("provider-uuid")).thenReturn(requestedProvider);
        when(encounterService.getEncounterTypeByUuid("enc-type-uuid")).thenReturn(encounterType);
        when(encounterSessionMatcher.findEncounter(eq(activeVisit), any(EncounterParameters.class)))
                .thenReturn(null);

        // Candidate encounter has a different provider
        Encounter candidate = new Encounter();
        candidate.setEncounterDatetime(new Date());
        candidate.setEncounterType(encounterType);
        candidate.setLocation(location);
        Provider otherProvider = new Provider();
        otherProvider.setUuid("other-provider-uuid");
        EncounterProvider ep = new EncounterProvider();
        ep.setProvider(otherProvider);
        Set<EncounterProvider> eps = new HashSet<EncounterProvider>();
        eps.add(ep);
        candidate.setEncounterProviders(eps);

        when(encounterService.getEncounters(any(Patient.class), eq(null), any(Date.class), any(Date.class),
                anyCollection(), anyCollection(), eq(null), eq(null), anyCollection(), eq(false)))
                .thenReturn(Arrays.asList(candidate));

        EncounterMatchResponse response = service.decideMatch(request);

        assertEquals("no_match", response.getStatus());
        assertEquals("provider_mismatch", response.getReason());
        assertNotNull(response.getReasonDescription());
    }

    // AC 3: Location mismatch
    @Test
    public void no_match_whenLocationDiffers() {
        EncounterMatchRequest request = buildRequest();
        request.setProviderUuids(null); // no provider filter — isolate location check

        when(encounterService.getEncounterTypeByUuid("enc-type-uuid")).thenReturn(encounterType);
        when(encounterSessionMatcher.findEncounter(eq(activeVisit), any(EncounterParameters.class)))
                .thenReturn(null);

        Location otherLocation = new Location();
        otherLocation.setUuid("other-location-uuid");
        otherLocation.setName("Ward");

        Encounter candidate = new Encounter();
        candidate.setEncounterDatetime(new Date());
        candidate.setEncounterType(encounterType);
        candidate.setLocation(otherLocation);
        candidate.setEncounterProviders(new HashSet<EncounterProvider>());

        when(encounterService.getEncounters(any(Patient.class), eq(null), any(Date.class), any(Date.class),
                anyCollection(), anyCollection(), eq(null), eq(null), anyCollection(), eq(false)))
                .thenReturn(Arrays.asList(candidate));

        Location requestedVisitLocation = new Location();
        requestedVisitLocation.setUuid("visit-location-uuid");
        Location otherVisitLocation = new Location();
        otherVisitLocation.setUuid("other-visit-location-uuid");

        when(bahmniVisitLocationService.getVisitLocation("location-uuid")).thenReturn(requestedVisitLocation);
        when(bahmniVisitLocationService.getVisitLocation("other-location-uuid")).thenReturn(otherVisitLocation);

        EncounterMatchResponse response = service.decideMatch(request);

        assertEquals("no_match", response.getStatus());
        assertEquals("location_mismatch", response.getReason());
        assertNotNull(response.getReasonDescription());
    }

    // AC 4: Session expired
    @Test
    public void no_match_whenSessionExpired() {
        EncounterMatchRequest request = buildRequest();

        when(encounterService.getEncounterTypeByUuid("enc-type-uuid")).thenReturn(encounterType);
        when(providerService.getProviderByUuid("provider-uuid")).thenReturn(provider);
        when(encounterSessionMatcher.findEncounter(eq(activeVisit), any(EncounterParameters.class)))
                .thenReturn(null);

        // Encounter datetime is 90 minutes ago (outside 60-min session)
        Date oldDatetime = DateUtils.addMinutes(new Date(), -90);
        Encounter candidate = buildEncounter(oldDatetime);

        when(encounterService.getEncounters(any(Patient.class), eq(null), any(Date.class), any(Date.class),
                anyCollection(), anyCollection(), eq(null), eq(null), anyCollection(), eq(false)))
                .thenReturn(Arrays.asList(candidate));

        EncounterMatchResponse response = service.decideMatch(request);

        assertEquals("no_match", response.getStatus());
        assertEquals("session_expired", response.getReason());
        assertNotNull(response.getReasonDescription());
    }

    // AC 5: No active visit — visit.stopDatetime is set
    @Test
    public void no_active_visit_whenVisitIsStopped() {
        Visit stoppedVisit = new Visit();
        stoppedVisit.setId(1);
        stoppedVisit.setStopDatetime(new Date());
        when(visitService.getVisitByUuid("visit-uuid")).thenReturn(stoppedVisit);

        EncounterMatchRequest request = buildRequest();
        EncounterMatchResponse response = service.decideMatch(request);

        assertEquals("no_active_visit", response.getStatus());
    }

    // AC 5 (variant): No active visit — visitUuid not found
    @Test
    public void no_active_visit_whenVisitNotFound() {
        when(visitService.getVisitByUuid("visit-uuid")).thenReturn(null);

        EncounterMatchRequest request = buildRequest();
        EncounterMatchResponse response = service.decideMatch(request);

        assertEquals("no_active_visit", response.getStatus());
    }

    // AC 6: No encounters found at all
    @Test
    public void no_match_whenNoEncountersInVisit() {
        EncounterMatchRequest request = buildRequest();

        when(encounterService.getEncounterTypeByUuid("enc-type-uuid")).thenReturn(encounterType);
        when(providerService.getProviderByUuid("provider-uuid")).thenReturn(provider);
        when(encounterSessionMatcher.findEncounter(eq(activeVisit), any(EncounterParameters.class)))
                .thenReturn(null);
        when(encounterService.getEncounters(any(Patient.class), eq(null), any(Date.class), any(Date.class),
                anyCollection(), anyCollection(), eq(null), eq(null), anyCollection(), eq(false)))
                .thenReturn(Collections.<Encounter>emptyList());

        EncounterMatchResponse response = service.decideMatch(request);

        assertEquals("no_match", response.getStatus());
        assertEquals("no_active_encounter", response.getReason());
    }

    // AC 7: Multiple encounters match — matcher throws
    @Test
    public void error_whenMultipleEncountersMatch() {
        EncounterMatchRequest request = buildRequest();

        when(encounterService.getEncounterTypeByUuid("enc-type-uuid")).thenReturn(encounterType);
        when(providerService.getProviderByUuid("provider-uuid")).thenReturn(provider);
        when(encounterSessionMatcher.findEncounter(eq(activeVisit), any(EncounterParameters.class)))
                .thenThrow(new RuntimeException("More than one encounter matches the criteria"));

        EncounterMatchResponse response = service.decideMatch(request);

        assertEquals("error", response.getStatus());
        assertEquals("MULTIPLE_ENCOUNTERS_MATCH", response.getErrorCode());
        assertNotNull(response.getErrorMessage());
    }

    // AC 8: Configurable session duration — global property = 120, 90-min-old encounter should match
    @Test
    public void match_found_whenEncounterWithin120MinSessionDuration() {
        when(administrationService.getGlobalProperty("bahmni.encountersession.duration")).thenReturn("120");

        EncounterMatchRequest request = buildRequest();
        // Encounter is 90 minutes old — within 120 min session, so matcher returns it
        Date encounterDatetime = DateUtils.addMinutes(new Date(), -90);
        Encounter matchedEncounter = buildEncounter(encounterDatetime);

        when(encounterService.getEncounterTypeByUuid("enc-type-uuid")).thenReturn(encounterType);
        when(providerService.getProviderByUuid("provider-uuid")).thenReturn(provider);
        when(encounterSessionMatcher.findEncounter(eq(activeVisit), any(EncounterParameters.class)))
                .thenReturn(matchedEncounter);

        EncounterMatchResponse response = service.decideMatch(request);

        assertEquals("match_found", response.getStatus());
        assertEquals("enc-uuid", response.getEncounterUuid());
        assertEquals(Integer.valueOf(120), response.getMatchDetails().getSessionDurationMinutes());
    }
}
