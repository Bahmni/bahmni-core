package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.patient.PatientContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BahmniPatientContextControllerIT extends BaseIntegrationTest {

    @Before
    public void setUp() throws Exception {
        executeDataSet("patientContextDataSet.xml");

    }

    @Test
    public void shouldFetchCorePatientInformation() throws Exception {
        MockHttpServletRequest request = newGetRequest("/rest/v1/bahmnicore/patientcontext", new Parameter("patientUuid", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"));
        MockHttpServletResponse response = handle(request);
        PatientContext patientContext = deserialize(response, PatientContext.class);

        assertNotNull(patientContext);
        assertEquals("101-6", patientContext.getIdentifier());
        assertEquals(0, patientContext.getPersonAttributes().size());
        assertEquals(0, patientContext.getProgramAttributes().size());
    }

    @Test
    public void shouldFetchCorePatientInformationAndConfiguredPersonAttributes() throws Exception {
        MockHttpServletRequest request = newGetRequest("/rest/v1/bahmnicore/patientcontext",
                new Parameter("patientUuid", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"),
                new Parameter("personAttributes", "Birthplace")
        );
        MockHttpServletResponse response = handle(request);
        PatientContext patientContext = deserialize(response, PatientContext.class);

        assertNotNull(patientContext);
        assertEquals("101-6", patientContext.getIdentifier());
        assertEquals(1, patientContext.getPersonAttributes().size());
        assertTrue(patientContext.getPersonAttributes().keySet().contains("Birthplace"));
        assertEquals("London", patientContext.getPersonAttributes().get("Birthplace").get("value"));
        assertEquals(0, patientContext.getProgramAttributes().size());
    }

    @Test
    public void shouldFetchCorePatientInformationAndConfiguredProgramAttributes() throws Exception {
        executeDataSet("programEnrollmentDataSet.xml");

        MockHttpServletRequest request = newGetRequest("/rest/v1/bahmnicore/patientcontext",
                new Parameter("patientUuid", "75e04d42-3ca8-11e3-bf2b-0808633c1b75"),
                new Parameter("programUuid", "9119b9f8-af3d-4ad8-9e2e-2317c3de91c6"),
                new Parameter("programAttributes", "stage")
        );
        MockHttpServletResponse response = handle(request);
        PatientContext patientContext = deserialize(response, PatientContext.class);

        assertNotNull(patientContext);
        assertEquals("75e04d42-3ca8-11e3-bf2b-0808633c1b75", patientContext.getUuid());
        assertEquals(1, patientContext.getProgramAttributes().size());
        assertTrue(patientContext.getProgramAttributes().keySet().contains("stage"));
        assertEquals("Stage1", patientContext.getProgramAttributes().get("stage").get("value"));
        assertEquals("stage description", patientContext.getProgramAttributes().get("stage").get("description"));
        assertEquals(0, patientContext.getPersonAttributes().size());
    }

    @Test
    public void shouldNotFetchAnyConfiguredProgramAttributesWhenThePatientIsNotEnrolledInAnyProgram() throws Exception {
        MockHttpServletRequest request = newGetRequest("/rest/v1/bahmnicore/patientcontext",
                new Parameter("patientUuid", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"),
                new Parameter("programAttributes", "stage")
        );
        MockHttpServletResponse response = handle(request);
        PatientContext patientContext = deserialize(response, PatientContext.class);

        assertNotNull(patientContext);
        assertEquals("da7f524f-27ce-4bb2-86d6-6d1d05312bd5", patientContext.getUuid());
        assertEquals(0, patientContext.getProgramAttributes().size());
    }

    @Test
    public void shouldNotFetchAnyProgramAttributesWhenNoneIsSpecified() throws Exception {
        executeDataSet("programEnrollmentDataSet.xml");

        MockHttpServletRequest request = newGetRequest("/rest/v1/bahmnicore/patientcontext",
                new Parameter("patientUuid", "75e04d42-3ca8-11e3-bf2b-0808633c1b75"),
                new Parameter("programUuid", "9119b9f8-af3d-4ad8-9e2e-2317c3de91c6")
        );
        MockHttpServletResponse response = handle(request);
        PatientContext patientContext = deserialize(response, PatientContext.class);

        assertNotNull(patientContext);
        assertEquals("75e04d42-3ca8-11e3-bf2b-0808633c1b75", patientContext.getUuid());
        assertEquals(0, patientContext.getProgramAttributes().size());
    }

    @Test
    public void shouldFetchConceptNameAsValueForPersonAttributesOfConceptType() throws Exception {
        MockHttpServletRequest request = newGetRequest("/rest/v1/bahmnicore/patientcontext",
                new Parameter("patientUuid", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"),
                new Parameter("personAttributes", "Civil Status")
        );

        MockHttpServletResponse response = handle(request);
        PatientContext patientContext = deserialize(response, PatientContext.class);

        assertNotNull(patientContext);
        assertEquals(1, patientContext.getPersonAttributes().size());
        assertEquals("MARRIED", patientContext.getPersonAttributes().get("Civil Status").get("value"));
        assertEquals("Marriage status of this person", patientContext.getPersonAttributes().get("Civil Status").get("description"));
    }

    @Test
    public void shouldFetchExtraPatientIdentifiersIfConfigured() throws Exception {
        MockHttpServletRequest request = newGetRequest("/rest/v1/bahmnicore/patientcontext",
                new Parameter("patientUuid", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"),
                new Parameter("patientIdentifiers", "Old Identification Number")
        );

        MockHttpServletResponse response = handle(request);
        PatientContext patientContext = deserialize(response, PatientContext.class);

        assertNotNull(patientContext);
        assertEquals("101-6", patientContext.getIdentifier());
        assertEquals(1, patientContext.getAdditionalPatientIdentifiers().size());
        assertEquals("101", patientContext.getAdditionalPatientIdentifiers().get("Old Identification Number"));
    }

    @Test
    public void shouldNotFetchPrimaryIdentifierAsExtraPatientIdentifiersIfConfigured() throws Exception {
        MockHttpServletRequest request = newGetRequest("/rest/v1/bahmnicore/patientcontext",
                new Parameter("patientUuid", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"),
                new Parameter("patientIdentifiers", "OpenMRS Identification Number")
        );

        MockHttpServletResponse response = handle(request);
        PatientContext patientContext = deserialize(response, PatientContext.class);

        assertNotNull(patientContext);
        assertEquals("101-6", patientContext.getIdentifier());
        assertEquals(0, patientContext.getAdditionalPatientIdentifiers().size());
    }
}