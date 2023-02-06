package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.SMS.PrescriptionSMS;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.SharePrescriptionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.api.ConceptService;

import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class BahmniDrugOrderControllerTest {

    @Mock
    ConceptService conceptService;
    @Mock
    BahmniDrugOrderService bahmniDrugOrderService;
    @Mock
    SharePrescriptionService sharePrescriptionService;

    @InjectMocks
    BahmniDrugOrderController bahmniDrugOrderController;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldReturnNullIfConceptNotFound() throws Exception {
        String drugConceptSetName = "All TB Drugs";
        when(conceptService.getConceptByName(drugConceptSetName)).thenReturn(null);
        Set<org.openmrs.Concept> drugConcepts = bahmniDrugOrderController.getDrugConcepts(drugConceptSetName);
        assertNull(drugConcepts);
    }

    @Test
    public void shouldReturnNullIfDrugConceptNameIsNull() {
        Set<org.openmrs.Concept> drugConcepts = bahmniDrugOrderController.getDrugConcepts(null);
        assertNull(drugConcepts);
    }

    @Test
    public void shouldCallSendPrescriptionSMSServiceMethod() throws Exception {
        PrescriptionSMS prescriptionSMS = new PrescriptionSMS();
        prescriptionSMS.setVisitUuid("visit-uuid");
        prescriptionSMS.setLocale("en");
        bahmniDrugOrderController.sendPrescriptionSMS(prescriptionSMS);
        verify(sharePrescriptionService, times(1)).sendPresciptionSMS(prescriptionSMS);
    }
}