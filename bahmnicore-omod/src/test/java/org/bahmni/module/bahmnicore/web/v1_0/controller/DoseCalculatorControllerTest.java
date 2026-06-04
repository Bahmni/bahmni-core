package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.rulesengine.domain.DosageRequest;
import org.openmrs.module.rulesengine.domain.Dose;
import org.openmrs.module.rulesengine.engine.RulesEngine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DoseCalculatorControllerTest {

    @InjectMocks
    DoseCalculatorController doseCalculatorController;

    @Mock
    RulesEngine rulesEngine;

    @Test
    public void shouldParseTheJsonDoseRequestAndReturnsConvertedDoseObject() throws Exception {
        Dose testDose = new Dose("testdrug", 150, Dose.DoseUnit.mg);
        when(rulesEngine.calculateDose(any(DosageRequest.class))).thenReturn(testDose);
        String dosageRequest="{ \"patientUuid\": \"id\", " +
                "\"drugName\": \"testDrug\", \"baseDose\": 5.0, \"doseUnit\": \"mg/kg\", \"orderSetName\": \"testOrderSet\"," +
                "\"dosingRule\": \"testrule\"}";
        Dose dose = doseCalculatorController.calculateDose(dosageRequest);
        assertNotNull(dose);
        assertEquals(150,dose.getValue(),1);
        assertEquals("testdrug",dose.getDrugName());
        verify(rulesEngine, times(1)).calculateDose(any(DosageRequest.class));
    }
}
