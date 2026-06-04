package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;

import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

public class PatientIdentifierMapperTest {

    @Mock
    private PatientService patientService;

    @Mock
    private AdministrationService administrationService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldMapIdentifierFromPatientToBahmniPatient() {
        PatientIdentifier identifier = new PatientIdentifier("GAN001", new PatientIdentifierType(), new Location());
        Patient patient = new Patient();
        patient.setIdentifiers(new HashSet<>(Arrays.asList(identifier)));

        BahmniPatient bahmniPatient = new PatientIdentifierMapper(patientService, administrationService).mapFromPatient(null, patient);

        assertEquals(patient.getPatientIdentifier().getIdentifier(), bahmniPatient.getIdentifier());

    }
}
