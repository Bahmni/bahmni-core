package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.bahmni.module.bahmnicore.model.BahmniPersonAttribute;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PersonService;

import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

public class PersonAttributeMapperTest {

    @Mock
    private PersonService personService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldMapPersonAttributesToPatientAttributes() {
        Patient patient = new Patient();
        PersonAttribute attribute = new PersonAttribute();
        PersonAttributeType attributeType = new PersonAttributeType();
        attributeType.setUuid("myuuid");
        attribute.setAttributeType(attributeType);
        attribute.setValue("blah");

        patient.setAttributes(new HashSet<>(Arrays.asList(attribute)));

        BahmniPatient bahmniPatient = new PersonAttributeMapper(personService).mapFromPatient(null, patient);

        BahmniPersonAttribute bahmniPersonAttribute = bahmniPatient.getAttributes().get(0);
        assertEquals(attribute.getAttributeType().getUuid(), bahmniPersonAttribute.getPersonAttributeUuid());
        assertEquals(attribute.getValue(), bahmniPersonAttribute.getValue());
    }
}
