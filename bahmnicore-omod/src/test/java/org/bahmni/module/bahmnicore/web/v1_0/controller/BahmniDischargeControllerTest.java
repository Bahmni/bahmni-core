package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.bedmanagement.service.BedManagementService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class BahmniDischargeControllerTest {
    @Mock
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;
    @Mock
    private PatientService patientService;
    @Mock
    private BedManagementService bedManagementService;

    @InjectMocks
    private BahmniDischargeController bahmniDischargeController;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        PowerMockito.mockStatic(Context.class);
        when(Context.getPatientService()).thenReturn(patientService);
        List<OpenmrsService> services = new ArrayList<>();
        services.add(bedManagementService);
        when(Context.getModuleOpenmrsServices(BedManagementService.class.getName())).thenReturn(services);
    }

    @Test
    public void shouldDischargeAndUnassignBedOfPatient() throws Exception {
        String patientUuid = "patientUuid";
        Patient patient = new Patient();
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setPatientUuid(patientUuid);
        when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        when(bahmniEncounterTransactionService.save(bahmniEncounterTransaction)).thenReturn(bahmniEncounterTransaction);

        BahmniEncounterTransaction encounterTransaction = bahmniDischargeController.discharge(bahmniEncounterTransaction);

        verify(bedManagementService, times(1)).unAssignPatientFromBed(patient);
        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(bahmniEncounterTransactionService, times(1)).save(bahmniEncounterTransaction);
        assertEquals(encounterTransaction, bahmniEncounterTransaction);
    }
}