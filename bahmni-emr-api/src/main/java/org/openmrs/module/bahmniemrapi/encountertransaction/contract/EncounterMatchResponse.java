package org.openmrs.module.bahmniemrapi.encountertransaction.contract;

import java.util.Date;

public class EncounterMatchResponse {

    public static final String STATUS_MATCH_FOUND = "match_found";
    public static final String STATUS_NO_MATCH = "no_match";
    public static final String STATUS_NO_ACTIVE_VISIT = "no_active_visit";
    public static final String STATUS_ERROR = "error";

    public static final String REASON_PROVIDER_MISMATCH = "provider_mismatch";
    public static final String REASON_LOCATION_MISMATCH = "location_mismatch";
    public static final String REASON_SESSION_EXPIRED = "session_expired";
    public static final String REASON_NO_ACTIVE_ENCOUNTER = "no_active_encounter";
    public static final String REASON_NO_ACTIVE_VISIT = "no_active_visit";

    public static final String ERROR_CODE_MULTIPLE_ENCOUNTERS_MATCH = "MULTIPLE_ENCOUNTERS_MATCH";

    private String status;
    private String encounterUuid;
    private Date encounterDateTime;
    private Ref encounterType;
    private Ref provider;
    private Ref location;
    private MatchDetails matchDetails;
    private String reason;
    private String reasonDescription;
    private String errorCode;
    private String errorMessage;

    public EncounterMatchResponse() {
    }

    // --- Static factory methods ---

    public static EncounterMatchResponse matchFound(String encounterUuid, Date encounterDateTime,
                                                     Ref encounterType, Ref provider, Ref location,
                                                     MatchDetails matchDetails) {
        EncounterMatchResponse response = new EncounterMatchResponse();
        response.status = STATUS_MATCH_FOUND;
        response.encounterUuid = encounterUuid;
        response.encounterDateTime = encounterDateTime;
        response.encounterType = encounterType;
        response.provider = provider;
        response.location = location;
        response.matchDetails = matchDetails;
        return response;
    }

    public static EncounterMatchResponse noMatch(String reason, String reasonDescription) {
        EncounterMatchResponse response = new EncounterMatchResponse();
        response.status = STATUS_NO_MATCH;
        response.reason = reason;
        response.reasonDescription = reasonDescription;
        return response;
    }

    public static EncounterMatchResponse noActiveVisit() {
        EncounterMatchResponse response = new EncounterMatchResponse();
        response.status = STATUS_NO_ACTIVE_VISIT;
        response.reason = REASON_NO_ACTIVE_VISIT;
        response.reasonDescription = "Patient has no active visit.";
        return response;
    }

    public static EncounterMatchResponse error(String errorCode, String errorMessage) {
        EncounterMatchResponse response = new EncounterMatchResponse();
        response.status = STATUS_ERROR;
        response.errorCode = errorCode;
        response.errorMessage = errorMessage;
        return response;
    }

    // --- Getters and setters ---

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEncounterUuid() {
        return encounterUuid;
    }

    public void setEncounterUuid(String encounterUuid) {
        this.encounterUuid = encounterUuid;
    }

    public Date getEncounterDateTime() {
        return encounterDateTime;
    }

    public void setEncounterDateTime(Date encounterDateTime) {
        this.encounterDateTime = encounterDateTime;
    }

    public Ref getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(Ref encounterType) {
        this.encounterType = encounterType;
    }

    public Ref getProvider() {
        return provider;
    }

    public void setProvider(Ref provider) {
        this.provider = provider;
    }

    public Ref getLocation() {
        return location;
    }

    public void setLocation(Ref location) {
        this.location = location;
    }

    public MatchDetails getMatchDetails() {
        return matchDetails;
    }

    public void setMatchDetails(MatchDetails matchDetails) {
        this.matchDetails = matchDetails;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(String reasonDescription) {
        this.reasonDescription = reasonDescription;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // --- Static nested classes ---

    public static class Ref {
        private String uuid;
        private String display;

        public Ref() {
        }

        public Ref(String uuid, String display) {
            this.uuid = uuid;
            this.display = display;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getDisplay() {
            return display;
        }

        public void setDisplay(String display) {
            this.display = display;
        }
    }

    public static class MatchDetails {
        private Boolean providerMatched;
        private Boolean locationMatched;
        private Boolean withinSessionDuration;
        private Integer sessionDurationMinutes;

        public MatchDetails() {
        }

        public Boolean getProviderMatched() {
            return providerMatched;
        }

        public void setProviderMatched(Boolean providerMatched) {
            this.providerMatched = providerMatched;
        }

        public Boolean getLocationMatched() {
            return locationMatched;
        }

        public void setLocationMatched(Boolean locationMatched) {
            this.locationMatched = locationMatched;
        }

        public Boolean getWithinSessionDuration() {
            return withinSessionDuration;
        }

        public void setWithinSessionDuration(Boolean withinSessionDuration) {
            this.withinSessionDuration = withinSessionDuration;
        }

        public Integer getSessionDurationMinutes() {
            return sessionDurationMinutes;
        }

        public void setSessionDurationMinutes(Integer sessionDurationMinutes) {
            this.sessionDurationMinutes = sessionDurationMinutes;
        }
    }
}
