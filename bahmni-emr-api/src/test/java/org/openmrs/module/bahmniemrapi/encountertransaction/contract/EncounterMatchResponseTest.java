package org.openmrs.module.bahmniemrapi.encountertransaction.contract;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class EncounterMatchResponseTest {

    @Test
    public void matchFound_shouldPopulateAllFieldsCorrectly() {
        Date now = new Date();

        EncounterMatchResponse.Ref encounterType =
                new EncounterMatchResponse.Ref("type-uuid", "Visit");
        EncounterMatchResponse.Ref provider =
                new EncounterMatchResponse.Ref("provider-uuid", "Dr. X");
        EncounterMatchResponse.Ref location =
                new EncounterMatchResponse.Ref("location-uuid", "Clinic A");

        EncounterMatchResponse.MatchDetails matchDetails =
                new EncounterMatchResponse.MatchDetails();
        matchDetails.setProviderMatched(true);
        matchDetails.setLocationMatched(true);

        EncounterMatchResponse response = EncounterMatchResponse.matchFound(
                "enc-uuid",
                now,
                encounterType,
                provider,
                location,
                matchDetails
        );

        assertEquals(EncounterMatchResponse.STATUS_MATCH_FOUND, response.getStatus());
        assertEquals("enc-uuid", response.getEncounterUuid());
        assertEquals(now, response.getEncounterDateTime());
        assertEquals(encounterType, response.getEncounterType());
        assertEquals(provider, response.getProvider());
        assertEquals(location, response.getLocation());
        assertEquals(matchDetails, response.getMatchDetails());
    }

    @Test
    public void noMatch_shouldSetStatusReasonAndDescription() {
        EncounterMatchResponse response =
                EncounterMatchResponse.noMatch("provider_mismatch", "Provider does not match");

        assertEquals(EncounterMatchResponse.STATUS_NO_MATCH, response.getStatus());
        assertEquals("provider_mismatch", response.getReason());
        assertEquals("Provider does not match", response.getReasonDescription());
    }

    @Test
    public void noActiveVisit_shouldSetPredefinedValues() {
        EncounterMatchResponse response = EncounterMatchResponse.noActiveVisit();

        assertEquals(EncounterMatchResponse.STATUS_NO_ACTIVE_VISIT, response.getStatus());
        assertEquals(EncounterMatchResponse.REASON_NO_ACTIVE_VISIT, response.getReason());
        assertEquals("Patient has no active visit.", response.getReasonDescription());
    }

    @Test
    public void error_shouldSetErrorFields() {
        EncounterMatchResponse response =
                EncounterMatchResponse.error("ERR_001", "Something went wrong");

        assertEquals(EncounterMatchResponse.STATUS_ERROR, response.getStatus());
        assertEquals("ERR_001", response.getErrorCode());
        assertEquals("Something went wrong", response.getErrorMessage());
    }

    @Test
    public void settersAndGetters_shouldWorkCorrectly() {
        EncounterMatchResponse response = new EncounterMatchResponse();
        Date now = new Date();

        response.setStatus("status");
        response.setEncounterUuid("uuid");
        response.setEncounterDateTime(now);
        response.setReason("reason");
        response.setReasonDescription("desc");
        response.setErrorCode("code");
        response.setErrorMessage("msg");

        assertEquals("status", response.getStatus());
        assertEquals("uuid", response.getEncounterUuid());
        assertEquals(now, response.getEncounterDateTime());
        assertEquals("reason", response.getReason());
        assertEquals("desc", response.getReasonDescription());
        assertEquals("code", response.getErrorCode());
        assertEquals("msg", response.getErrorMessage());
    }

    @Test
    public void ref_shouldSetAndGetValues() {
        EncounterMatchResponse.Ref ref = new EncounterMatchResponse.Ref();

        ref.setUuid("uuid");
        ref.setDisplay("display");

        assertEquals("uuid", ref.getUuid());
        assertEquals("display", ref.getDisplay());
    }

    @Test
    public void matchDetails_shouldSetAndGetValues() {
        EncounterMatchResponse.MatchDetails details =
                new EncounterMatchResponse.MatchDetails();

        details.setProviderMatched(true);
        details.setLocationMatched(false);
        details.setWithinSessionDuration(true);
        details.setSessionDurationMinutes(30);

        assertTrue(details.getProviderMatched());
        assertFalse(details.getLocationMatched());
        assertTrue(details.getWithinSessionDuration());
        assertEquals(Integer.valueOf(30), details.getSessionDurationMinutes());
    }

    @Test
    public void defaultConstructor_shouldHaveNullFields() {
        EncounterMatchResponse response = new EncounterMatchResponse();

        assertNull(response.getStatus());
        assertNull(response.getEncounterUuid());
        assertNull(response.getEncounterDateTime());
        assertNull(response.getEncounterType());
        assertNull(response.getProvider());
        assertNull(response.getLocation());
        assertNull(response.getMatchDetails());
        assertNull(response.getReason());
        assertNull(response.getReasonDescription());
        assertNull(response.getErrorCode());
        assertNull(response.getErrorMessage());
    }
}