package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.openmrs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationService;
import org.bahmni.module.bahmnicore.matcher.EncounterSessionMatcher;
import org.bahmni.module.bahmnicore.service.EncounterMatchDecisionService;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.EncounterMatchRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.EncounterMatchResponse;
import org.openmrs.module.emrapi.encounter.EncounterParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class EncounterMatchDecisionServiceImpl implements EncounterMatchDecisionService {

    private static final Logger logger = LoggerFactory.getLogger(EncounterMatchDecisionServiceImpl.class);

    private final VisitService visitService;
    private final PatientService patientService;
    private final LocationService locationService;
    private final ProviderService providerService;
    private final EncounterService encounterService;
    private final EncounterSessionMatcher encounterSessionMatcher;
    private final BahmniVisitLocationService bahmniVisitLocationService;
    private final AdministrationService administrationService;

    @Autowired
    public EncounterMatchDecisionServiceImpl(VisitService visitService,
                                             PatientService patientService,
                                             LocationService locationService,
                                             ProviderService providerService,
                                             EncounterService encounterService,
                                             EncounterSessionMatcher encounterSessionMatcher,
                                             BahmniVisitLocationService bahmniVisitLocationService,
                                             @Qualifier("adminService") AdministrationService administrationService) {
        this.visitService = visitService;
        this.patientService = patientService;
        this.locationService = locationService;
        this.providerService = providerService;
        this.encounterService = encounterService;
        this.encounterSessionMatcher = encounterSessionMatcher;
        this.bahmniVisitLocationService = bahmniVisitLocationService;
        this.administrationService = administrationService;
    }

    @Override
    public EncounterMatchResponse decideMatch(EncounterMatchRequest request) {
        logger.debug("Encounter match decision requested for visit: {}, patient: {}",
                     request.getVisitUuid(), request.getPatientUuid());

        Visit visit = visitService.getVisitByUuid(request.getVisitUuid());
        if (visit == null) {
            logger.info("Visit not found. Returning no_active_visit.");
            return EncounterMatchResponse.noActiveVisit();
        }

        if (visit.getStopDatetime() != null) {
            logger.info("Visit is inactive (stopped). Returning no_active_visit.");
            return EncounterMatchResponse.noActiveVisit();
        }

        Patient patient = patientService.getPatientByUuid(request.getPatientUuid());
        if (patient == null) {
            logger.warn("Patient not found for UUID: {}", request.getPatientUuid());
            return EncounterMatchResponse.error("INVALID_PATIENT", "Patient not found.");
        }

        Location location = locationService.getLocationByUuid(request.getLocationUuid());
        if (location == null) {
            logger.warn("Location not found for UUID: {}", request.getLocationUuid());
            return EncounterMatchResponse.error("INVALID_LOCATION", "Location not found.");
        }

        String providerUuid = request.getProviderUuid();
        boolean providerSpecified = providerUuid != null && !providerUuid.isEmpty();

        Provider provider = null;
        if (providerSpecified) {
            provider = resolveProvider(providerUuid);
            if (provider == null) {
                logger.warn("Provider not found for UUID: {}", providerUuid);
                return EncounterMatchResponse.noMatch(
                        EncounterMatchResponse.REASON_PROVIDER_MISMATCH,
                        "Specified provider not found. A new encounter will be created.");
            }
        }

        Date encounterDateTime = request.getEncounterDateTime() != null ? request.getEncounterDateTime() : new Date();
        Set<Provider> providerSet = provider != null ? new HashSet<>(Arrays.asList(provider)) : new HashSet<>();

        EncounterParameters params = EncounterParameters.instance()
                .setPatient(patient)
                .setLocation(location)
                .setProviders(providerSet)
                .setEncounterDateTime(encounterDateTime);

        if (request.getPatientProgramUuid() != null) {
            Map<String, Object> context = new HashMap<>();
            context.put(EncounterSessionMatcher.PATIENT_PROGAM_UUID, request.getPatientProgramUuid());
            params.setContext(context);
        }

        Encounter matchedEncounter;
        try {
            matchedEncounter = encounterSessionMatcher.findEncounter(visit, params);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("More than one encounter matches")) {
                logger.warn("Multiple encounters match criteria");
                return EncounterMatchResponse.error(
                        EncounterMatchResponse.ERROR_CODE_MULTIPLE_ENCOUNTERS_MATCH,
                        "Multiple encounters match the criteria. Please contact administrator.");
            }
            throw e;
        }

        if (matchedEncounter != null) {
            logger.info("Encounter match found: {}", matchedEncounter.getUuid());
            return buildMatchFoundResponse(matchedEncounter);
        }

        logger.debug("No match found by matcher. Running diagnostic to determine reason.");
        EncounterMatchResponse diagResult = diagnoseNoMatch(patient, visit, encounterDateTime, location, providerSet);
        logger.info("Encounter match decision: {} - {}", diagResult.getStatus(), diagResult.getReason());
        return diagResult;
    }

    private EncounterMatchResponse buildMatchFoundResponse(Encounter encounter) {
        logger.debug("Building match_found response for encounter: {}", encounter.getUuid());

        EncounterMatchResponse.Ref encounterTypeRef = null;
        if (encounter.getEncounterType() != null) {
            encounterTypeRef = new EncounterMatchResponse.Ref(
                    encounter.getEncounterType().getUuid(),
                    encounter.getEncounterType().getName());
        }

        EncounterMatchResponse.Ref providerRef = null;
        if (CollectionUtils.isNotEmpty(encounter.getEncounterProviders())) {
            EncounterProvider ep = encounter.getEncounterProviders().iterator().next();
            if (ep.getProvider() != null) {
                providerRef = new EncounterMatchResponse.Ref(
                        ep.getProvider().getUuid(),
                        ep.getProvider().getName());
            }
        }

        EncounterMatchResponse.Ref locationRef = null;
        if (encounter.getLocation() != null) {
            locationRef = new EncounterMatchResponse.Ref(
                    encounter.getLocation().getUuid(),
                    encounter.getLocation().getName());
        }

        EncounterMatchResponse.MatchDetails matchDetails = new EncounterMatchResponse.MatchDetails();
        matchDetails.setProviderMatched(Boolean.TRUE);
        matchDetails.setLocationMatched(Boolean.TRUE);
        matchDetails.setWithinSessionDuration(Boolean.TRUE);
        matchDetails.setSessionDurationMinutes(getSessionDurationMinutes());

        return EncounterMatchResponse.matchFound(
                encounter.getUuid(),
                encounter.getEncounterDatetime(),
                encounterTypeRef,
                providerRef,
                locationRef,
                matchDetails);
    }

    private EncounterMatchResponse diagnoseNoMatch(Patient patient, Visit visit,
                                                    Date encounterDateTime, Location location,
                                                    Set<Provider> requestedProviders) {
        logger.debug("Querying encounters to determine no-match reason");

        // Query all encounters in this visit (no encounter type filter)
        // to determine why the matcher returned null.
        Date startOfDay = DateUtils.truncate(encounterDateTime, Calendar.DATE);
        logger.info("  [DIAGNOSTIC] Query params: startOfDay=" + startOfDay + ", endDate=" + encounterDateTime);

        Collection<Encounter> candidates = encounterService.getEncounters(
                patient,
                null,
                startOfDay,
                encounterDateTime,
                new ArrayList<Form>(),
                null,
                null,
                null,
                Arrays.asList(visit),
                false);

        if (CollectionUtils.isEmpty(candidates)) {
            logger.debug("No encounters found in visit");
            return EncounterMatchResponse.noMatch(
                    EncounterMatchResponse.REASON_NO_ACTIVE_ENCOUNTER,
                    "No matching encounter found in this visit.");
        }

        Encounter candidate = getMostRecent(candidates);
        logger.debug("Evaluating most recent encounter: {}", candidate.getUuid());

        int sessionDurationMinutes = getSessionDurationMinutes();
        Date sessionCutoff = DateUtils.addMinutes(encounterDateTime, -sessionDurationMinutes);

        if (candidate.getEncounterDatetime() != null && candidate.getEncounterDatetime().before(sessionCutoff)) {
            logger.debug("Session expired for encounter: {}", candidate.getUuid());
            return EncounterMatchResponse.noMatch(
                    EncounterMatchResponse.REASON_SESSION_EXPIRED,
                    "Encounter exists but is outside the session duration window. A new encounter will be created.");
        }

        if (CollectionUtils.isNotEmpty(requestedProviders)) {
            Set<Provider> encounterProviders = extractProviders(candidate);
            boolean anyMatch = false;
            for (Provider requested : requestedProviders) {
                for (Provider ep : encounterProviders) {
                    if (requested.getUuid() != null && requested.getUuid().equals(ep.getUuid())) {
                        anyMatch = true;
                        break;
                    }
                }
                if (anyMatch) {
                    break;
                }
            }
            if (!anyMatch) {
                logger.debug("Provider mismatch for encounter: {}", candidate.getUuid());
                return EncounterMatchResponse.noMatch(
                        EncounterMatchResponse.REASON_PROVIDER_MISMATCH,
                        "Encounter exists but belongs to different provider. A new encounter will be created.");
            }
        }

        if (location != null && candidate.getLocation() != null) {
            Location requestedVisitLocation = bahmniVisitLocationService.getVisitLocation(location.getUuid());
            Location encounterVisitLocation = bahmniVisitLocationService.getVisitLocation(candidate.getLocation().getUuid());

            if (requestedVisitLocation != null && !requestedVisitLocation.equals(encounterVisitLocation)) {
                logger.debug("Location mismatch for encounter: {}", candidate.getUuid());
                return EncounterMatchResponse.noMatch(
                        EncounterMatchResponse.REASON_LOCATION_MISMATCH,
                        "Encounter exists but is in a different location. A new encounter will be created.");
            }
        }

        logger.debug("All diagnostic checks passed. Treating as match.");
        return buildMatchFoundResponse(candidate);
    }

    private Set<Provider> extractProviders(Encounter encounter) {
        Set<Provider> providers = new HashSet<Provider>();
        if (encounter.getEncounterProviders() != null) {
            for (EncounterProvider ep : encounter.getEncounterProviders()) {
                if (ep.getProvider() != null) {
                    providers.add(ep.getProvider());
                }
            }
        }
        return providers;
    }

    private Encounter getMostRecent(Collection<Encounter> encounters) {
        Encounter mostRecent = null;
        for (Encounter e : encounters) {
            if (mostRecent == null) {
                mostRecent = e;
            } else if (e.getEncounterDatetime() != null
                    && (mostRecent.getEncounterDatetime() == null
                    || e.getEncounterDatetime().after(mostRecent.getEncounterDatetime()))) {
                mostRecent = e;
            }
        }
        return mostRecent;
    }

    private Provider resolveProvider(String providerUuid) {
        if (providerUuid != null && !providerUuid.isEmpty()) {
            logger.info("Resolving provider UUID: " + providerUuid);
            Provider provider = providerService.getProviderByUuid(providerUuid);
            if (provider != null) {
                logger.info("  Resolved provider UUID " + providerUuid + " -> ID=" + provider.getId());
                return provider;
            } else {
                logger.warn("  Could not resolve provider UUID: " + providerUuid);
            }
        } else {
            logger.info("No provider UUID provided");
        }
        return null;
    }

    private Date getSearchStartDate(Date endDate) {
        int sessionDuration = getSessionDurationMinutes();
        Date startDate = DateUtils.addMinutes(endDate, -sessionDuration);
        if (!DateUtils.isSameDay(startDate, endDate)) {
            return DateUtils.truncate(endDate, Calendar.DATE);
        }
        return startDate;
    }

    private int getSessionDurationMinutes() {
        String configured = administrationService.getGlobalProperty("bahmni.encountersession.duration");
        if (configured != null) {
            try {
                return Integer.parseInt(configured);
            } catch (NumberFormatException e) {
                logger.warn("Invalid bahmni.encountersession.duration global property value: {}", configured);
            }
        }
        return EncounterSessionMatcher.DEFAULT_SESSION_DURATION_IN_MINUTES;
    }
}
