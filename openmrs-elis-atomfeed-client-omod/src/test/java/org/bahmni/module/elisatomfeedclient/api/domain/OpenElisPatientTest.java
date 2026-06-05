package org.bahmni.module.elisatomfeedclient.api.domain;

import org.joda.time.LocalDate;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class OpenElisPatientTest {

    @Test
    public void shouldReturnBirthDateAsDate() throws Exception {
        OpenElisPatient openElisPatient = new OpenElisPatient();
        LocalDate today = LocalDate.now();
        openElisPatient.setDateOfBirth(today.toString("yyyy-MM-dd"));

        assertEquals(today.toDate(), openElisPatient.getDateOfBirthAsDate());
    }

}
