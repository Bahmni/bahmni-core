package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.bahmni.module.bahmnicore.web.v1_0.search.BahmniMainResourceControllerTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;

public class BahmniProgramEnrollmentResourceITBahmni extends BahmniMainResourceControllerTest {


    private BahmniProgramWorkflowService service;
    private PatientService patientService;

    @Before
    public void before() {
        this.service = Context.getService(BahmniProgramWorkflowService.class);
        this.patientService = Context.getPatientService();
    }


    @Test
    //TODO Revisit and cleanup. No getting of first element of array
    public void shouldGetProgramEnrollmentsByPatient() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        String PATIENT_IN_A_PROGRAM_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
        req.setParameter("patient", PATIENT_IN_A_PROGRAM_UUID);
        SimpleObject result = deserialize(handle(req));

        Patient patient = patientService.getPatientByUuid(PATIENT_IN_A_PROGRAM_UUID);
        List<PatientProgram> patientPrograms = service.getPatientPrograms(patient, null, null, null, null, null, true);
        Assert.assertEquals(patientPrograms.size(), Util.getResultsSize(result));
        PatientProgram bahmniPatientProgram = (PatientProgram) patientPrograms.get(0);
        List results = (List) result.get("results");
        Assert.assertEquals(bahmniPatientProgram.getUuid(), ((HashMap) results.get(0)).get("uuid"));
    }


    @Override
    public String getURI() {
        return "bahmniprogramenrollment";
    }

    @Override
    public String getUuid() {
        return "b75462a0-4c92-451e-b8bc-e98b38b76534";
    }

    @Override
    public long getAllCount() {
        return 0;
    }
}
