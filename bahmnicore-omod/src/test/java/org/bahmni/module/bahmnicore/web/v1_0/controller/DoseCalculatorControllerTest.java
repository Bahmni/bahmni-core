package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.api.APIException;
import org.openmrs.module.rulesengine.domain.DosageRequest;
import org.openmrs.module.rulesengine.domain.Dose;
import org.openmrs.module.rulesengine.engine.RulesEngine;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DoseCalculatorControllerTest {

    private static final String DOSAGE_REQUEST = "{ \"patientUuid\": \"id\", " +
            "\"drugName\": \"testDrug\", \"baseDose\": 5.0, \"doseUnit\": \"mg/kg\", \"orderSetName\": \"testOrderSet\"," +
            "\"dosingRule\": \"testrule\"}";

    @InjectMocks
    DoseCalculatorController doseCalculatorController;

    @Mock
    RulesEngine rulesEngine;

    @Test
    public void shouldParseTheJsonDoseRequestAndReturnsConvertedDoseObject() throws Exception {
        Dose testDose = new Dose("testdrug", 150, Dose.DoseUnit.mg);
        when(rulesEngine.calculateDose(any(DosageRequest.class))).thenReturn(testDose);

        ResponseEntity<Object> response = doseCalculatorController.calculateDose(DOSAGE_REQUEST);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Dose dose = (Dose) response.getBody();
        assertNotNull(dose);
        assertEquals(150, dose.getValue(), 1);
        assertEquals("testdrug", dose.getDrugName());
        verify(rulesEngine, times(1)).calculateDose(any(DosageRequest.class));
    }

    @Test
    public void shouldReturnBadRequestWithValidationMessageWhenRuleValidationFails() throws Exception {
        when(rulesEngine.calculateDose(any(DosageRequest.class)))
                .thenThrow(new Exception("Please enter WEIGHT to calculate dose."));

        ResponseEntity<Object> response = doseCalculatorController.calculateDose(DOSAGE_REQUEST);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Please enter WEIGHT to calculate dose.", errorMessage(response));
    }

    @Test
    public void shouldReturnBadRequestWhenDosageRequestJsonIsMalformed() throws Exception {
        ResponseEntity<Object> response = doseCalculatorController.calculateDose("{ not json");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(errorMessage(response));
        verify(rulesEngine, never()).calculateDose(any(DosageRequest.class));
    }

    @Test(expected = APIException.class)
    public void shouldPropagateRuntimeExceptionsAsServerErrors() throws Exception {
        when(rulesEngine.calculateDose(any(DosageRequest.class)))
                .thenThrow(new APIException("Concept [WEIGHT] is not configured in rules."));

        doseCalculatorController.calculateDose(DOSAGE_REQUEST);
    }

    @SuppressWarnings("unchecked")
    private String errorMessage(ResponseEntity<Object> response) {
        SimpleObject body = (SimpleObject) response.getBody();
        return (String) ((Map<String, Object>) body.get("error")).get("message");
    }
}
