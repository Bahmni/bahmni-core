package org.openmrs.module.bahmniemrapi.encountertransaction.contract;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EncounterMatchRequestTest {

    @Test
    public void shouldSetAndGetAllFields() {
        EncounterMatchRequest request = new EncounterMatchRequest();

        Date now = new Date();

        request.setPatientUuid("patient-uuid");
        request.setVisitUuid("visit-uuid");
        request.setProviderUuid("provider-uuid");
        request.setLocationUuid("location-uuid");
        request.setEncounterDateTime(now);
        request.setPatientProgramUuid("program-uuid");

        assertEquals("patient-uuid", request.getPatientUuid());
        assertEquals("visit-uuid", request.getVisitUuid());
        assertEquals("provider-uuid", request.getProviderUuid());
        assertEquals("location-uuid", request.getLocationUuid());
        assertEquals(now, request.getEncounterDateTime());
        assertEquals("program-uuid", request.getPatientProgramUuid());
    }

    @Test
    public void shouldReturnNullForAllFieldsByDefault() {
        EncounterMatchRequest request = new EncounterMatchRequest();

        assertNull(request.getPatientUuid());
        assertNull(request.getVisitUuid());
        assertNull(request.getProviderUuid());
        assertNull(request.getLocationUuid());
        assertNull(request.getEncounterDateTime());
        assertNull(request.getPatientProgramUuid());
    }

    @Test
    public void shouldAllowUpdatingFieldValues() {
        EncounterMatchRequest request = new EncounterMatchRequest();

        request.setPatientUuid("initial");
        assertEquals("initial", request.getPatientUuid());

        request.setPatientUuid("updated");
        assertEquals("updated", request.getPatientUuid());
    }
}