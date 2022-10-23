package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicommons.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Deprecated.
 * This test is redundant if PatientDaoImpl.getPatients(String, string ... )
 * uses a prepared statement with parameters instead of building string queries
 * with parameters.
 *
 * All the test cases have been migrated to BahmniPatientDaoIT.
 * This exists only for historical and reference point for the new tests
 * @see @{@link BahmniPatientDaoIT} instead.
 *
 */
@Deprecated
@Ignore
public class BahmniPatientDaoImplIT extends BaseIntegrationTest {
    @Autowired
    private PatientDao patientDao;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        executeDataSet("apiTestData.xml");
    }
    @Test
    public void shouldFetchPatientsWithPartialIdentifierMatch() throws Exception {
        String partialIdentifier = "300001";
        boolean shouldMatchExactPatientId = false;
        List<Patient> patients = patientDao.getPatients(partialIdentifier, shouldMatchExactPatientId);
        assertEquals(2, patients.size());
        List<Person> persons = new ArrayList<>();
        Person person1 = new Person();
        Person person2 = new Person();
        person1.setUuid("df877447-6745-45be-b859-403241d991dd");
        person2.setUuid("df888447-6745-45be-b859-403241d991dd");
        persons.add(person1);
        persons.add(person2);
        assertTrue(persons.contains(patients.get(0)));
        assertTrue(persons.contains(patients.get(1)));
    }

    @Test
    public void shouldReturnEmptyListForNoIdentifierMatch() throws Exception {
        String partialIdentifier = "3000001";
        boolean shouldMatchExactPatientId = false;
        List<Patient> patients = patientDao.getPatients(partialIdentifier, shouldMatchExactPatientId);
        assertEquals(0, patients.size());
    }
}
