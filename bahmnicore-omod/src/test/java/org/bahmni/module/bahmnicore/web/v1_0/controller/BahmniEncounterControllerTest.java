package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.service.EncounterMatchDecisionService;
import org.bahmni.module.bahmnicore.web.v1_0.VisitClosedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderEntryException;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterSearchParameters;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.EncounterMatchRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.EncounterMatchResponse;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.BahmniEncounterTransactionMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

public class BahmniEncounterControllerTest {
    @Mock
    private EmrEncounterService emrEncounterService;
    @Mock
    private BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper;
    @Mock
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;

    @Mock
    private EncounterService encounterService;
    @Mock
    private EncounterMatchDecisionService encounterMatchDecisionService;
    private BahmniEncounterController bahmniEncounterController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void returnsMultipleEncounterTransactionsIfExists() throws Exception {
        EncounterTransaction et1 = new EncounterTransaction();
        et1.setEncounterUuid("et1");

        BahmniEncounterSearchParameters encounterSearchParameters = new BahmniEncounterSearchParameters();
        encounterSearchParameters.setIncludeAll(false);

        when(bahmniEncounterTransactionService.find(encounterSearchParameters)).thenReturn(et1);
        when(bahmniEncounterTransactionMapper.map(et1, false)).thenReturn(new BahmniEncounterTransaction(et1));

        bahmniEncounterController = new BahmniEncounterController(null, emrEncounterService, null, bahmniEncounterTransactionService, bahmniEncounterTransactionMapper, null);

        BahmniEncounterTransaction bahmniEncounterTransaction = bahmniEncounterController.find(encounterSearchParameters);

        assertEquals(et1.getEncounterUuid(), bahmniEncounterTransaction.getEncounterUuid());
    }

    @Test
    public void shouldReturnEmptyEncounterTransactionIfThereAreNoEncountersExists() throws Exception {
        BahmniEncounterSearchParameters encounterSearchParameters = new BahmniEncounterSearchParameters();
        encounterSearchParameters.setIncludeAll(false);

        when(emrEncounterService.find(encounterSearchParameters)).thenReturn(null);
        when(bahmniEncounterTransactionMapper.map(any(EncounterTransaction.class), anyBoolean())).thenReturn(new BahmniEncounterTransaction(new EncounterTransaction()));

        bahmniEncounterController = new BahmniEncounterController(null, emrEncounterService, null, bahmniEncounterTransactionService, bahmniEncounterTransactionMapper, null);
        BahmniEncounterTransaction bahmniEncounterTransactions = bahmniEncounterController.find(encounterSearchParameters);

        assertNull(bahmniEncounterTransactions.getEncounterUuid());
    }

    @Test(expected=VisitClosedException.class)
    public void shouldThrowVisitClosedExceptionIfEncounterVisitIsClosed() throws Exception{
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Visit visit = new Visit();
        visit.setId(123);
        visit.setStopDatetime(format.parse("2016-03-08 12:46:46"));

        Encounter encounter = new Encounter();
        encounter.setId(321);
        encounter.setUuid("410491d2-b617-42ad-bf0f-de2fc9b42998");
        encounter.setVisit(visit);

        bahmniEncounterController = new BahmniEncounterController(encounterService, emrEncounterService, null, bahmniEncounterTransactionService, bahmniEncounterTransactionMapper, null);

        when(encounterService.getEncounterByUuid("410491d2-b617-42ad-bf0f-de2fc9b42998")).thenReturn(encounter);

        bahmniEncounterController.delete("410491d2-b617-42ad-bf0f-de2fc9b42998","Undo Discharge");
    }
    @Test
    public void shouldThrowBadRequestStatusCodeForOrderEntryException() throws Exception{
        bahmniEncounterController = new BahmniEncounterController(encounterService, emrEncounterService, null, bahmniEncounterTransactionService, bahmniEncounterTransactionMapper, null);

        OrderEntryException mockException = new OrderEntryException("Order.cannot.have.more.than.one");

        ResponseEntity<Object> response = bahmniEncounterController.handleOrderEntryException(mockException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        Map<String, String> errorBody = (Map<String, String>) responseBody.get("error");

        assertEquals("[Order.cannot.have.more.than.one]", errorBody.get("message"));
    }

    @Test
    public void matchDecision_shouldDelegateToServiceAndReturnResponse() {
        bahmniEncounterController = new BahmniEncounterController(encounterService, emrEncounterService, null,
                bahmniEncounterTransactionService, bahmniEncounterTransactionMapper, encounterMatchDecisionService);

        EncounterMatchRequest request = new EncounterMatchRequest();
        request.setVisitUuid("visit-uuid");
        request.setPatientUuid("patient-uuid");
        request.setLocationUuid("location-uuid");

        EncounterMatchResponse expectedResponse = EncounterMatchResponse.noActiveVisit();
        when(encounterMatchDecisionService.decideMatch(request)).thenReturn(expectedResponse);

        Map<String, Object> actualResponse = bahmniEncounterController.matchDecision(request);

        assertEquals("no_active_visit", actualResponse.get("status"));
        assertEquals("no_active_visit", actualResponse.get("reason"));
        assertNull(actualResponse.get("errorCode"));
    }

    @Test
    public void matchDecision_shouldReturnOnlyNonNullFields() {
        bahmniEncounterController = new BahmniEncounterController(
                encounterService,
                emrEncounterService,
                null,
                bahmniEncounterTransactionService,
                bahmniEncounterTransactionMapper,
                encounterMatchDecisionService
        );

        EncounterMatchRequest request = new EncounterMatchRequest();

        EncounterMatchResponse response = new EncounterMatchResponse();
        response.setStatus("MATCHED");
        response.setEncounterUuid("uuid-123");
        response.setReason(null); // should be removed

        when(encounterMatchDecisionService.decideMatch(request)).thenReturn(response);

        Map<String, Object> result = bahmniEncounterController.matchDecision(request);

        assertEquals("MATCHED", result.get("status"));
        assertEquals("uuid-123", result.get("encounterUuid"));
        assertNull(result.get("reason")); // null should not be present
    }

    @Test
    public void matchDecision_shouldReturnEmptyMapWhenAllFieldsNull() {
        bahmniEncounterController = new BahmniEncounterController(
                encounterService,
                emrEncounterService,
                null,
                bahmniEncounterTransactionService,
                bahmniEncounterTransactionMapper,
                encounterMatchDecisionService
        );

        EncounterMatchRequest request = new EncounterMatchRequest();
        EncounterMatchResponse response = new EncounterMatchResponse(); // all null

        when(encounterMatchDecisionService.decideMatch(request)).thenReturn(response);

        Map<String, Object> result = bahmniEncounterController.matchDecision(request);

        assertEquals(0, result.size());
    }

    @Test
    public void matchDecision_shouldHandleErrorFields() {
        bahmniEncounterController = new BahmniEncounterController(
                encounterService,
                emrEncounterService,
                null,
                bahmniEncounterTransactionService,
                bahmniEncounterTransactionMapper,
                encounterMatchDecisionService
        );

        EncounterMatchRequest request = new EncounterMatchRequest();

        EncounterMatchResponse response = new EncounterMatchResponse();
        response.setErrorCode("ERR_001");
        response.setErrorMessage("Something went wrong");

        when(encounterMatchDecisionService.decideMatch(request)).thenReturn(response);

        Map<String, Object> result = bahmniEncounterController.matchDecision(request);

        assertEquals("ERR_001", result.get("errorCode"));
        assertEquals("Something went wrong", result.get("errorMessage"));
    }
}