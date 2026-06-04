package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.model.BahmniName;
import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.bahmni.module.bahmnicore.util.NameMother;
import org.bahmni.module.bahmnicore.util.PatientMother;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PersonName;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class PersonNameMapperTest {

    @Test
    public void shouldCreateNewNameIfNoNameExists() {
        Patient patient = new Patient();
        List<BahmniName> names = Arrays.asList(new BahmniName(new NameMother().getSimpleObjectForName()));

        new PersonNameMapper().map(patient, names);

        assertEquals(names.get(0).getGivenName(), patient.getGivenName());
        assertEquals(names.get(0).getFamilyName(), patient.getFamilyName());
    }

    @Test
    public void shouldVoidNamesSavedBeforeIfThereIsAChangeInName() {
        Patient patient = new Patient();

        List<BahmniName> names = Arrays.asList(new BahmniName(new NameMother().getSimpleObjectForName()));
        BahmniName bahmniName = names.get(0);
        PersonName name = new PersonName(bahmniName.getGivenName() + "old", null, bahmniName
                .getFamilyName());
        name.setId(10);
        patient.addName(name);

        new PersonNameMapper().map(patient, names);

        Set<PersonName> nameList = patient.getNames();
        PersonName oldName = getByFirstName(bahmniName.getGivenName() + "old", nameList);

        assertTrue(oldName.isVoided());
    }

    @Test
    public void shouldMapNameFromPatientToBahmniPatient() {
        PersonNameMapper mapper = new PersonNameMapper();
        Patient patient = new PatientMother().withName("ram", null, "singh").build();
        BahmniPatient bahmniPatient = mapper.mapFromPatient(null, patient);
        assertEquals(patient.getGivenName(), bahmniPatient.getNames().get(0).getGivenName());
        assertEquals(patient.getFamilyName(), bahmniPatient.getNames().get(0).getFamilyName());
    }

    private PersonName getByFirstName(String s, Set<PersonName> nameList) {
        for (PersonName personName : nameList) {
            if (personName.getGivenName().equals(s))
                return personName;
        }
        return null;
    }
}
