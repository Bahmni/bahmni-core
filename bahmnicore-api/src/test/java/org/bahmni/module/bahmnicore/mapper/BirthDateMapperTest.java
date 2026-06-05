package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.model.Age;
import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.junit.Test;
import org.openmrs.Patient;

import java.util.Date;

import static junit.framework.Assert.assertEquals;

public class BirthDateMapperTest {

    @Test
    public void shouldMapFromPatientToBahmniPatient() {
        Patient patient = new Patient();
        patient.setBirthdate(new Date());

        BirthDateMapper mapper = new BirthDateMapper();
        BahmniPatient bahmniPatient = mapper.mapFromPatient(null, patient);

        assertEquals(patient.getBirthdate(),bahmniPatient.getBirthdate());
        assertEquals(new Age(0,0,0), bahmniPatient.getAge());
    }
}
