package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.FormDraftRequest;
import org.bahmni.module.bahmnicore.contract.FormDraftResponse;
import org.bahmni.module.bahmnicore.contract.FormDraftSummaryResponse;
import org.bahmni.module.bahmnicore.model.FormDraft;
import org.bahmni.module.bahmnicore.service.FormDraftService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FormDraftControllerTest {

    private FormDraftController controller;
    private FormDraftService formDraftService;

    private static final String PATIENT_UUID = "patient-uuid-123";
    private static final String PROVIDER_UUID = "provider-uuid-456";
    private static final String DRAFT_UUID = "draft-uuid";
    private static final String FORM_DATA_PATH = "/path/to/draft.json";

    @Before
    public void setUp() throws Exception {
        formDraftService = mock(FormDraftService.class);
        controller = new FormDraftController();
        // Use reflection to inject the mock service since there's no public setter
        java.lang.reflect.Field field = controller.getClass().getDeclaredField("formDraftService");
        field.setAccessible(true);
        field.set(controller, formDraftService);
    }

    @Test
    public void getDraft_shouldReturnEmptyResponseWhenNoDraftExists() {
        when(formDraftService.getDraft(PATIENT_UUID, PROVIDER_UUID)).thenReturn(null);

        ResponseEntity<?> response = controller.getDraft(PATIENT_UUID, PROVIDER_UUID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof FormDraftResponse);
    }

    @Test
    public void getDraft_shouldReturnBadRequestWhenServiceThrowsException() {
        doThrow(new IllegalArgumentException("Invalid UUID")).when(formDraftService).getDraft(PATIENT_UUID, PROVIDER_UUID);

        ResponseEntity<?> response = controller.getDraft(PATIENT_UUID, PROVIDER_UUID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void saveDraft_shouldReturnBadRequestWhenValidationFails() {
        FormDraftRequest request = buildFormDraftRequest(null, PROVIDER_UUID, "{\"form\":\"data\"}");
        doThrow(new IllegalArgumentException("Patient UUID is required")).when(formDraftService).saveDraft(any(FormDraftRequest.class));

        ResponseEntity<?> response = controller.saveDraft(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void saveDraft_shouldReturnBadRequestWhenServiceThrowsException() {
        FormDraftRequest request = buildFormDraftRequest(PATIENT_UUID, PROVIDER_UUID, "{\"form\":\"data\"}");
        doThrow(new RuntimeException("Unexpected error")).when(formDraftService).saveDraft(any(FormDraftRequest.class));

        ResponseEntity<?> response = controller.saveDraft(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void markDraftAsSaved_shouldReturnBadRequestWhenPatientUuidIsNull() {
        doThrow(new IllegalArgumentException("Patient UUID is required")).when(formDraftService)
                .markDraftAsSaved(null, PROVIDER_UUID);

        ResponseEntity<Object> response = controller.markDraftAsSaved(null, PROVIDER_UUID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void markDraftAsSaved_shouldReturnBadRequestWhenProviderUuidIsEmpty() {
        doThrow(new IllegalArgumentException("Provider UUID is required")).when(formDraftService)
                .markDraftAsSaved(PATIENT_UUID, "");

        ResponseEntity<Object> response = controller.markDraftAsSaved(PATIENT_UUID, "");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void markDraftAsSaved_shouldReturnBadRequestWhenServiceThrows() {
        doThrow(new RuntimeException("Service error")).when(formDraftService)
                .markDraftAsSaved(PATIENT_UUID, PROVIDER_UUID);

        ResponseEntity<Object> response = controller.markDraftAsSaved(PATIENT_UUID, PROVIDER_UUID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    
    @Test
    public void getDraftsByProvider_returns200WithList() {
        FormDraftSummaryResponse summary = new FormDraftSummaryResponse();
        summary.setDraftUuid("draft-uuid-1");
        summary.setPatientUuid("patient-uuid-1");
        summary.setPatientName("John Doe");
        summary.setPatientIdentifier("ET001");
        summary.setTimestamp(1000L);
        when(formDraftService.getDraftsByProvider(PROVIDER_UUID)).thenReturn(Collections.singletonList(summary));

        ResponseEntity<Object> response = controller.getDraftsByProvider(PROVIDER_UUID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<?> body = (List<?>) response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
    }

    @Test
    public void getDraftsByProvider_returns200WithEmptyList() {
        when(formDraftService.getDraftsByProvider(PROVIDER_UUID)).thenReturn(Collections.emptyList());

        ResponseEntity<Object> response = controller.getDraftsByProvider(PROVIDER_UUID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<?> body = (List<?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.isEmpty());
    }

    @Test
    public void getDraftsByProvider_returns400_whenProviderUuidIsInvalid() {
        doThrow(new IllegalArgumentException("Provider UUID is required")).when(formDraftService)
                .getDraftsByProvider("   ");

        ResponseEntity<Object> response = controller.getDraftsByProvider("   ");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // --- Helpers ---

    private FormDraftRequest buildFormDraftRequest(String patientUuid, String providerUuid, String formData) {
        FormDraftRequest request = new FormDraftRequest();
        request.setPatientUuid(patientUuid);
        request.setProviderUuid(providerUuid);
        request.setFormData(formData);
        return request;
    }

    private FormDraft buildFormDraft(String uuid, String formDataPath) {
        FormDraft draft = new FormDraft();
        draft.setUuid(uuid);
        draft.setFormDataPath(formDataPath);
        draft.setDateCreated(new Date());
        return draft;
    }
}
