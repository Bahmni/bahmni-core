package org.bahmni.module.admin.csv.persister;

import org.bahmni.csv.Messages;
import org.bahmni.module.admin.BaseIntegrationTest;
import org.bahmni.module.admin.csv.models.PatientProgramRow;
import org.bahmni.module.bahmnicommons.api.service.BahmniPatientService;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PatientProgramPersisterIT extends BaseIntegrationTest {

    @Autowired
    private PatientProgramPersister patientProgramPersister;
    @Autowired
    private ProgramWorkflowService programWorkflowService;
    @Autowired
    private BahmniPatientService patientService;

    protected UserContext userContext;

    @Before
    public void setUp() throws Exception {
        String path;
        executeDataSet("dataSetup.xml");
        path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", path);

        Context.authenticate("admin", "test");
        userContext = Context.getUserContext();
        patientProgramPersister.init(userContext, null);
    }

    @Test
    public void enrollPatientInAProgram() throws Exception {
        PatientProgramRow patientProgramRow = new PatientProgramRow();
        patientProgramRow.patientIdentifier = "GAN200000";
        patientProgramRow.programName = "Diabetes Program";
        patientProgramRow.enrollmentDateTime = "1111-11-11";

        Messages persistenceResult = patientProgramPersister.persist(patientProgramRow);
        assertTrue("Should have persisted the encounter row with the program. ", persistenceResult.isEmpty());

        Context.openSession();
        Context.authenticate("admin", "test");
        Patient patient = patientService.get("GAN200000", true).get(0);
        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(patient, null, null, null, null, null, false);

        assertTrue("patient should have been enrolled in a program", !patientPrograms.isEmpty());
        assertEquals("Diabetes Program", patientPrograms.get(0).getProgram().getName());

        Context.flushSession();
        Context.closeSession();
    }

    @Test
    public void shouldNotEnrollAnAlreadyEnrolledPatientInAProgram() throws Exception {
        PatientProgramRow patientProgramRow = new PatientProgramRow();
        patientProgramRow.patientIdentifier = "SEM200000";
        patientProgramRow.enrollmentDateTime = "1111-11-11";
        patientProgramRow.programName = "DIABETES PROGRAM";

        Messages errorMessages = patientProgramPersister.persist(patientProgramRow);
        assertTrue(errorMessages.toString().contains("Patient already enrolled in Diabetes Program"));
    }

}
