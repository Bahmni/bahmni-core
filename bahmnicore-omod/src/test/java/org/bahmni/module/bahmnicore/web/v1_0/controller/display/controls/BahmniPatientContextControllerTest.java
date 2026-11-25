package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.BahmniPatientContextMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.bahmniemrapi.patient.PatientContext;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniPatientContextControllerTest {

    @InjectMocks
    private BahmniPatientContextController bahmniPatientContextController = new BahmniPatientContextController();

    @Mock
    private PatientService patientService;

    @Mock
    private AdministrationService administrationService;

    @Mock
    private BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Mock
    private BahmniPatientContextMapper bahmniPatientContextMapper;

    @Mock
    private UserContext userContext;

    private MockedStatic<Context> contextMockedStatic;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        contextMockedStatic = Mockito.mockStatic(Context.class);
        contextMockedStatic.when(() -> Context.getService(BahmniProgramWorkflowService.class)).thenReturn(bahmniProgramWorkflowService);
    }

    @After
    public void tearDown() {
        contextMockedStatic.close();
    }

    @Test
    public void shouldGetCorePersonInformationIfPersonAttributesAndProgramAttributesAreNotConfigured() {
        String patientUuid = "patientUuid";
        String programUuid = "programUuid";
        Patient patient = new Patient();
        PatientContext expectedPatientContext = new PatientContext();
        List<String> configuredPersonAttributes = Collections.singletonList("Caste");
        List<String> configuredProgramAttributes = Collections.singletonList("IRDB Number");
        List<String> configuredPatientIdentifiers = Collections.singletonList("National Identifier");
        PatientProgram bahmniPatientProgram = new PatientProgram();
        PatientIdentifierType primaryIdentifierType = new PatientIdentifierType();

        when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        when(administrationService.getGlobalProperty("bahmni.primaryIdentifierType")).thenReturn("primary-identifier-uuid");
        when(patientService.getPatientIdentifierTypeByUuid("primary-identifier-uuid")).thenReturn(primaryIdentifierType);
        when(bahmniPatientContextMapper.map(patient, bahmniPatientProgram, configuredPersonAttributes, configuredProgramAttributes, configuredPatientIdentifiers, primaryIdentifierType)).thenReturn(expectedPatientContext);
        when(bahmniProgramWorkflowService.getPatientProgramByUuid(programUuid)).thenReturn(bahmniPatientProgram);

        PatientContext actualPatientContext = bahmniPatientContextController.getPatientContext(patientUuid, programUuid, configuredPersonAttributes, configuredProgramAttributes, configuredPatientIdentifiers);

        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(bahmniPatientContextMapper, times(1)).map(patient, bahmniPatientProgram, configuredPersonAttributes, configuredProgramAttributes, configuredPatientIdentifiers, primaryIdentifierType);
        verify(bahmniProgramWorkflowService, times(1)).getPatientProgramByUuid(programUuid);
        assertEquals(expectedPatientContext, actualPatientContext);
    }
}
