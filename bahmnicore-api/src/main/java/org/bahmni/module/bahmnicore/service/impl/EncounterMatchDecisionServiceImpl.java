package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationService;
import org.bahmni.module.bahmnicore.matcher.EncounterSessionMatcher;
import org.bahmni.module.bahmnicore.service.EncounterMatchDecisionService;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterType;
import org.openmrs.Form;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class EncounterMatchDecisionServiceImpl implements EncounterMatchDecisionService {

    private static final Logger logger = LogManager.getLogger(EncounterMatchDecisionServiceImpl.class);

    private final VisitService visitService;
    private final PatientService patientService;
    private final LocationService locationService;
    private final ProviderService providerService;
    private final EncounterService encounterService;
    private final EncounterSessionMatcher encounterSessionMatcher;
    private final EncounterTypeIdentifier encounterTypeIdentifier;
    private final BahmniVisitLocationService bahmniVisitLocationService;
    private final AdministrationService administrationService;

    @Autowired
    public EncounterMatchDecisionServiceImpl(VisitService visitService,
                                             PatientService patientService,
                                             LocationService locationService,
                                             ProviderService providerService,
                                             EncounterService encounterService,
                                             EncounterSessionMatcher encounterSessionMatcher,
                                             EncounterTypeIdentifier encounterTypeIdentifier,
                                             BahmniVisitLocationService bahmniVisitLocationService,
                                             @Qualifier("adminService") AdministrationService administrationService) {
        this.visitService = visitService;
        this.patientService = patientService;
        this.locationService = locationService;
        this.providerService = providerService;
        this.encounterService = encounterService;
        this.encounterSessionMatcher = encounterSessionMatcher;
        this.encounterTypeIdentifier = encounterTypeIdentifier;
        this.bahmniVisitLocationService = bahmniVisitLocationService;
        this.administrationService = administrationService;
    }

    @Override
    public EncounterMatchResponse decideMatch(EncounterMatchRequest request) {
        logger.info("========== ENCOUNTER MATCH DECISION START ==========");
        logger.info("Request: patientUuid=" + request.getPatientUuid() + ", visitUuid=" + request.getVisitUuid() +
                   ", locationUuid=" + request.getLocationUuid() + ", encounterDateTime=" + request.getEncounterDateTime());

        // Step 1: Resolve visit — no active visit if null or already stopped
        logger.info("STEP 1: Looking up visit by UUID: " + request.getVisitUuid());
        Visit visit = visitService.getVisitByUuid(request.getVisitUuid());

        if (visit == null) {
            logger.warn("RESULT: Visit not found for UUID: " + request.getVisitUuid());
            return EncounterMatchResponse.noActiveVisit();
        }

        logger.info("Visit found. UUID=" + visit.getUuid() + ", ID=" + visit.getId());
        logger.info("Visit stopDatetime: " + visit.getStopDatetime());

        if (visit.getStopDatetime() != null) {
            logger.warn("RESULT: Visit is inactive (stopDatetime is set): " + visit.getStopDatetime());
            return EncounterMatchResponse.noActiveVisit();
        }

        logger.info("Visit is ACTIVE");

        // Step 2: Resolve dependencies from request
        logger.info("STEP 2: Resolving dependencies...");
        Patient patient = patientService.getPatientByUuid(request.getPatientUuid());
        logger.info("Patient resolved: " + (patient != null ? "UUID=" + patient.getUuid() : "NULL"));

        Location location = locationService.getLocationByUuid(request.getLocationUuid());
        logger.info("Location resolved: " + (location != null ? "UUID=" + location.getUuid() + ", Name=" + location.getName() : "NULL"));

        Set<Provider> providers = resolveProviders(request.getProviderUuids());
        logger.info("Providers resolved: count=" + (providers != null ? providers.size() : 0));
        if (providers != null && !providers.isEmpty()) {
            for (Provider p : providers) {
                logger.info("  - Provider: UUID=" + p.getUuid() + ", ID=" + p.getId());
            }
        }

        EncounterType encounterType = resolveEncounterType(request.getEncounterTypeUuids());
        logger.info("EncounterType resolved: " + (encounterType != null ? "UUID=" + encounterType.getUuid() + ", Name=" + encounterType.getName() : "NULL"));

        Date encounterDateTime = request.getEncounterDateTime() != null ? request.getEncounterDateTime() : new Date();
        logger.info("EncounterDateTime: " + encounterDateTime);

        // Step 3: Build EncounterParameters for the matcher
        logger.info("STEP 3: Building EncounterParameters for matcher...");
        EncounterParameters params = EncounterParameters.instance()
                .setPatient(patient)
                .setLocation(location)
                .setProviders(providers)
                .setEncounterType(encounterType)
                .setEncounterDateTime(encounterDateTime);

        if (request.getPatientProgramUuid() != null) {
            logger.info("PatientProgram UUID provided: " + request.getPatientProgramUuid());
            Map<String, Object> context = new HashMap<String, Object>();
            context.put(EncounterSessionMatcher.PATIENT_PROGAM_UUID, request.getPatientProgramUuid());
            params.setContext(context);
        }

        // Step 4: Invoke matcher
        logger.info("STEP 4: Calling EncounterSessionMatcher.findEncounter()...");
        Encounter matchedEncounter;
        try {
            matchedEncounter = encounterSessionMatcher.findEncounter(visit, params);
            logger.info("Matcher returned: " + (matchedEncounter != null ? "Encounter found (UUID=" + matchedEncounter.getUuid() + ")" : "NULL (no match)"));
        } catch (RuntimeException e) {
            logger.error("Matcher threw exception: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("More than one encounter matches")) {
                logger.warn("RESULT: Multiple encounters match criteria");
                return EncounterMatchResponse.error(
                        "MULTIPLE_ENCOUNTERS_MATCH",
                        "Multiple encounters match the criteria. Please contact administrator.");
            }
            throw e;
        }

        if (matchedEncounter != null) {
            logger.info("RESULT: match_found");
            logger.info("========== ENCOUNTER MATCH DECISION END ==========");
            return buildMatchFoundResponse(matchedEncounter);
        }

        // Step 5: Diagnostic pass — matcher returned null, determine reason
        logger.info("STEP 5: Matcher returned null - running diagnostic pass to determine reason...");
        EncounterMatchResponse diagResult = diagnoseNoMatch(patient, visit, encounterType, encounterDateTime, location, providers);
        logger.info("RESULT: " + diagResult.getStatus() + " - " + diagResult.getReason());
        logger.info("========== ENCOUNTER MATCH DECISION END ==========");
        return diagResult;
    }

    private EncounterMatchResponse buildMatchFoundResponse(Encounter encounter) {
        logger.info("Building match_found response for encounter UUID: " + encounter.getUuid());
        logger.info("Encounter details: dateTime=" + encounter.getEncounterDatetime() + ", location=" +
                   (encounter.getLocation() != null ? encounter.getLocation().getUuid() : "NULL"));

        EncounterMatchResponse.Ref encounterTypeRef = null;
        if (encounter.getEncounterType() != null) {
            encounterTypeRef = new EncounterMatchResponse.Ref(
                    encounter.getEncounterType().getUuid(),
                    encounter.getEncounterType().getName());
            logger.info("EncounterType: " + encounter.getEncounterType().getName());
        }

        EncounterMatchResponse.Ref providerRef = null;
        if (CollectionUtils.isNotEmpty(encounter.getEncounterProviders())) {
            EncounterProvider ep = encounter.getEncounterProviders().iterator().next();
            if (ep.getProvider() != null) {
                providerRef = new EncounterMatchResponse.Ref(
                        ep.getProvider().getUuid(),
                        ep.getProvider().getName());
                logger.info("Provider: " + ep.getProvider().getName());
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

    private EncounterMatchResponse diagnoseNoMatch(Patient patient, Visit visit, EncounterType encounterType,
                                                    Date encounterDateTime, Location location,
                                                    Set<Provider> requestedProviders) {
        logger.info("  [DIAGNOSTIC] Querying encounters to determine no-match reason...");

        // Query encounters in this visit for the encounter type, ignoring provider/location filters,
        // to determine why the matcher returned null.
        Date startOfDay = DateUtils.truncate(encounterDateTime, Calendar.DATE);
        logger.info("  [DIAGNOSTIC] Query params: startOfDay=" + startOfDay + ", endDate=" + encounterDateTime +
                   ", encounterType=" + (encounterType != null ? encounterType.getUuid() : "NULL"));

        Collection<Encounter> candidates = encounterService.getEncounters(
                patient,
                null,
                startOfDay,
                encounterDateTime,
                new ArrayList<Form>(),
                Arrays.asList(encounterType),
                null,
                null,
                Arrays.asList(visit),
                false);

        logger.info("  [DIAGNOSTIC] Encounters found in date range: count=" + (candidates != null ? candidates.size() : 0));

        if (CollectionUtils.isEmpty(candidates)) {
            logger.info("  [DIAGNOSTIC] No encounters found - returning no_active_encounter");
            return EncounterMatchResponse.noMatch(
                    "no_active_encounter",
                    "No matching encounter found in this visit.");
        }

        // Inspect the most recent candidate encounter
        Encounter candidate = getMostRecent(candidates);
        logger.info("  [DIAGNOSTIC] Selected most recent encounter: UUID=" + candidate.getUuid() +
                   ", dateTime=" + candidate.getEncounterDatetime());

        int sessionDurationMinutes = getSessionDurationMinutes();
        Date sessionCutoff = DateUtils.addMinutes(encounterDateTime, -sessionDurationMinutes);
        logger.info("  [DIAGNOSTIC] Session window: " + sessionDurationMinutes + " minutes, cutoff=" + sessionCutoff);

        // Check 1: session_expired (highest precedence)
        if (candidate.getEncounterDatetime() != null && candidate.getEncounterDatetime().before(sessionCutoff)) {
            logger.info("  [DIAGNOSTIC] FAILED: Encounter is BEFORE session cutoff - session_expired");
            return EncounterMatchResponse.noMatch(
                    "session_expired",
                    "Encounter exists but is outside the session duration window. A new encounter will be created.");
        }
        logger.info("  [DIAGNOSTIC] PASS: Encounter is within session window");

        // Check 2: provider_mismatch
        if (CollectionUtils.isNotEmpty(requestedProviders)) {
            logger.info("  [DIAGNOSTIC] Checking provider match...");
            Set<Provider> encounterProviders = extractProviders(candidate);
            logger.info("  [DIAGNOSTIC] Encounter providers: count=" + encounterProviders.size());
            for (Provider ep : encounterProviders) {
                logger.info("    - Provider UUID: " + ep.getUuid() + ", Name: " + ep.getName());
            }

            boolean anyMatch = false;
            for (Provider requested : requestedProviders) {
                for (Provider ep : encounterProviders) {
                    if (requested.getUuid() != null && requested.getUuid().equals(ep.getUuid())) {
                        anyMatch = true;
                        logger.info("  [DIAGNOSTIC] Found matching provider: " + requested.getUuid());
                        break;
                    }
                }
                if (anyMatch) {
                    break;
                }
            }
            if (!anyMatch) {
                logger.info("  [DIAGNOSTIC] FAILED: No provider match - provider_mismatch");
                return EncounterMatchResponse.noMatch(
                        "provider_mismatch",
                        "Encounter exists but belongs to different provider. A new encounter will be created.");
            }
            logger.info("  [DIAGNOSTIC] PASS: Provider matches");
        } else {
            logger.info("  [DIAGNOSTIC] No requested providers - checking if encounter has providers and matches current user...");
            Set<Provider> encounterProviders = extractProviders(candidate);
            logger.info("  [DIAGNOSTIC] Encounter providers: count=" + encounterProviders.size());

            if (CollectionUtils.isEmpty(encounterProviders)) {
                logger.info("  [DIAGNOSTIC] Encounter has no providers - checking if created by current user...");

                org.openmrs.User encounterCreator = candidate.getCreator();
                org.openmrs.User currentUser = null;
                try {
                    currentUser = org.openmrs.api.context.Context.getUserContext().getAuthenticatedUser();
                } catch (Exception e) {
                    logger.warn("  [DIAGNOSTIC] Could not get current user: " + e.getMessage());
                }

                logger.info("  [DIAGNOSTIC] Encounter creator: " + (encounterCreator != null ? "ID=" + encounterCreator.getId() + ", Username=" + encounterCreator.getUsername() : "NULL"));
                logger.info("  [DIAGNOSTIC] Current user: " + (currentUser != null ? "ID=" + currentUser.getId() + ", Username=" + currentUser.getUsername() : "NULL"));

                boolean isSameUser = encounterCreator != null &&
                                    currentUser != null &&
                                    encounterCreator.getId().intValue() == currentUser.getId().intValue();
                logger.info("  [DIAGNOSTIC] isSameUser: " + isSameUser);

                if (!isSameUser) {
                    logger.info("  [DIAGNOSTIC] FAILED: Encounter created by different user - provider_mismatch");
                    return EncounterMatchResponse.noMatch(
                            "provider_mismatch",
                            "Encounter exists but belongs to different provider. A new encounter will be created.");
                }
                logger.info("  [DIAGNOSTIC] PASS: Same user");
            } else {
                logger.info("  [DIAGNOSTIC] Encounter has providers - cannot match when no providers requested");
                return EncounterMatchResponse.noMatch(
                        "provider_mismatch",
                        "Encounter exists but belongs to different provider. A new encounter will be created.");
            }
        }

        // Check 3: location_mismatch
        if (location != null && candidate.getLocation() != null) {
            logger.info("  [DIAGNOSTIC] Checking location match...");
            logger.info("  [DIAGNOSTIC] Requested location UUID: " + location.getUuid());
            logger.info("  [DIAGNOSTIC] Encounter location UUID: " + candidate.getLocation().getUuid());

            Location requestedVisitLocation = bahmniVisitLocationService.getVisitLocation(location.getUuid());
            Location encounterVisitLocation = bahmniVisitLocationService.getVisitLocation(candidate.getLocation().getUuid());

            logger.info("  [DIAGNOSTIC] Requested visit location: " + (requestedVisitLocation != null ? requestedVisitLocation.getUuid() : "NULL"));
            logger.info("  [DIAGNOSTIC] Encounter visit location: " + (encounterVisitLocation != null ? encounterVisitLocation.getUuid() : "NULL"));

            if (requestedVisitLocation != null && !requestedVisitLocation.equals(encounterVisitLocation)) {
                logger.info("  [DIAGNOSTIC] FAILED: Locations do not match - location_mismatch");
                return EncounterMatchResponse.noMatch(
                        "location_mismatch",
                        "Encounter exists but is in a different location. A new encounter will be created.");
            }
            logger.info("  [DIAGNOSTIC] PASS: Locations match");
        } else {
            logger.info("  [DIAGNOSTIC] Location check skipped (location or candidate location is null)");
        }

        // All checks passed — this is a match
        logger.info("  [DIAGNOSTIC] All checks passed! Returning match_found");
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

    private Set<Provider> resolveProviders(List<String> providerUuids) {
        Set<Provider> providers = new HashSet<Provider>();
        if (CollectionUtils.isNotEmpty(providerUuids)) {
            logger.info("Resolving " + providerUuids.size() + " provider UUIDs...");
            for (String uuid : providerUuids) {
                Provider provider = providerService.getProviderByUuid(uuid);
                if (provider != null) {
                    logger.info("  Resolved provider UUID " + uuid + " -> ID=" + provider.getId());
                    providers.add(provider);
                } else {
                    logger.warn("  Could not resolve provider UUID: " + uuid);
                }
            }
        } else {
            logger.info("No provider UUIDs provided");
        }
        return providers;
    }

    private EncounterType resolveEncounterType(List<String> encounterTypeUuids) {
        if (CollectionUtils.isNotEmpty(encounterTypeUuids)) {
            logger.info("Resolving encounter type from UUIDs: " + encounterTypeUuids);
            EncounterType encounterType = encounterService.getEncounterTypeByUuid(encounterTypeUuids.get(0));
            if (encounterType != null) {
                logger.info("Resolved encounter type UUID " + encounterTypeUuids.get(0) + " -> " + encounterType.getName());
                return encounterType;
            } else {
                logger.warn("Could not resolve encounter type UUID: " + encounterTypeUuids.get(0));
            }
        }
        EncounterType defaultType = encounterTypeIdentifier.getDefaultEncounterType();
        logger.info("Using default encounter type: " + (defaultType != null ? defaultType.getName() : "NULL"));
        return defaultType;
    }

    private int getSessionDurationMinutes() {
        String configured = administrationService.getGlobalProperty("bahmni.encountersession.duration");
        logger.info("bahmni.encountersession.duration global property: " + configured);
        if (configured != null) {
            try {
                int duration = Integer.parseInt(configured);
                logger.info("Session duration: " + duration + " minutes");
                return duration;
            } catch (NumberFormatException e) {
                logger.warn("Invalid bahmni.encountersession.duration global property value: " + configured);
            }
        }
        return EncounterSessionMatcher.DEFAULT_SESSION_DURATION_IN_MINUTES;
    }
}
