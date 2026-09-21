package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.FormDraftRequest;
import org.bahmni.module.bahmnicore.contract.FormDraftResponse;
import org.bahmni.module.bahmnicore.contract.FormDraftSummaryResponse;
import org.bahmni.module.bahmnicore.model.FormDraft;
import org.bahmni.module.bahmnicore.service.FormDraftService;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.ProviderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openmrs.api.APIAuthenticationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FormDraftControllerTest {

    private TestableFormDraftController controller;
    private FormDraftService formDraftService;
    private ProviderService providerService;
    private Person authenticatedPerson;

    private static final String PATIENT_UUID = "patient-uuid-123";
    private static final String PROVIDER_UUID = "provider-uuid-456";
    private static final String DRAFT_UUID = "draft-uuid";
    private static final String FORM_DATA_PATH = "/path/to/draft.json";

    @Before
    public void setUp() throws Exception {
        formDraftService = mock(FormDraftService.class);
        providerService = mock(ProviderService.class);

        controller = new TestableFormDraftController();

        java.lang.reflect.Field serviceField = FormDraftController.class.getDeclaredField("formDraftService");
        serviceField.setAccessible(true);
        serviceField.set(controller, formDraftService);

        java.lang.reflect.Field providerServiceField = FormDraftController.class.getDeclaredField("providerService");
        providerServiceField.setAccessible(true);
        providerServiceField.set(controller, providerService);

        authenticatedPerson = new Person();
        User mockUser = new User();
        mockUser.setPerson(authenticatedPerson);
        controller.setAuthenticatedUser(mockUser);

        Provider provider = new Provider();
        provider.setUuid(PROVIDER_UUID);
        when(providerService.getProvidersByPerson(authenticatedPerson, false))
                .thenReturn(Collections.singletonList(provider));
    }

    private static class TestableFormDraftController extends FormDraftController {
        private User authenticatedUser;

        @Override
        protected User getAuthenticatedUser() {
            return authenticatedUser;
        }

        void setAuthenticatedUser(User user) {
            this.authenticatedUser = user;
        }
    }

    @Test
    public void getDraft_shouldReturnEmptyResponseWhenNoDraftExists() {
        when(formDraftService.getDraft(PATIENT_UUID, PROVIDER_UUID)).thenReturn(null);

        ResponseEntity<?> response = controller.getDraft(PATIENT_UUID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof FormDraftResponse);
    }

    @Test
    public void getDraft_shouldReturnBadRequestWhenServiceThrowsException() {
        doThrow(new IllegalArgumentException("Invalid UUID")).when(formDraftService).getDraft(PATIENT_UUID, PROVIDER_UUID);

        ResponseEntity<?> response = controller.getDraft(PATIENT_UUID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void saveDraft_shouldReturnBadRequestWhenValidationFails() {
        FormDraftRequest request = buildFormDraftRequest(null, "{\"form\":\"data\"}");
        doThrow(new IllegalArgumentException("Patient UUID is required")).when(formDraftService).saveDraft(any(FormDraftRequest.class), any(String.class));

        ResponseEntity<?> response = controller.saveDraft(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void saveDraft_shouldReturnInternalServerErrorWhenServiceThrowsException() {
        FormDraftRequest request = buildFormDraftRequest(PATIENT_UUID, "{\"form\":\"data\"}");
        doThrow(new RuntimeException("Unexpected error")).when(formDraftService).saveDraft(any(FormDraftRequest.class), any(String.class));

        ResponseEntity<?> response = controller.saveDraft(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void markDraftAsSaved_shouldReturnOkOnSuccess() {
        ResponseEntity<Object> response = controller.markDraftAsSaved(PATIENT_UUID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(formDraftService).markDraftAsSaved(PATIENT_UUID, PROVIDER_UUID);
    }

    @Test
    public void getDraftsByProvider_shouldReturnInternalServerErrorWhenServiceThrows() {
        doThrow(new RuntimeException("Unexpected error")).when(formDraftService).getDraftsByProvider(PROVIDER_UUID);

        ResponseEntity<Object> response = controller.getDraftsByProvider();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void markDraftAsSaved_shouldReturnBadRequestWhenPatientUuidIsNull() {
        doThrow(new IllegalArgumentException("Patient UUID is required")).when(formDraftService)
                .markDraftAsSaved(null, PROVIDER_UUID);

        ResponseEntity<Object> response = controller.markDraftAsSaved(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void markDraftAsSaved_shouldReturnInternalServerErrorWhenServiceThrows() {
        doThrow(new RuntimeException("Service error")).when(formDraftService)
                .markDraftAsSaved(PATIENT_UUID, PROVIDER_UUID);

        ResponseEntity<Object> response = controller.markDraftAsSaved(PATIENT_UUID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
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

        ResponseEntity<Object> response = controller.getDraftsByProvider();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<?> body = (List<?>) response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
    }

    @Test
    public void getDraftsByProvider_returns200WithEmptyList() {
        when(formDraftService.getDraftsByProvider(PROVIDER_UUID)).thenReturn(Collections.emptyList());

        ResponseEntity<Object> response = controller.getDraftsByProvider();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<?> body = (List<?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.isEmpty());
    }

    @Test
    public void getDraftsByProvider_returns400_whenServiceThrowsIllegalArgument() {
        doThrow(new IllegalArgumentException("Provider UUID is required")).when(formDraftService)
                .getDraftsByProvider(PROVIDER_UUID);

        ResponseEntity<Object> response = controller.getDraftsByProvider();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void discardDraft_shouldReturnNoContentOnSuccess() {
        ResponseEntity<Object> response = controller.discardDraft(PATIENT_UUID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(formDraftService).discardDraft(PATIENT_UUID, PROVIDER_UUID);
    }

    @Test(expected = APIAuthenticationException.class)
    public void getDraftsByProvider_shouldThrowWhenAuthenticatedUserHasNoProvider() {
        when(providerService.getProvidersByPerson(authenticatedPerson, false)).thenReturn(Collections.emptyList());
        controller.getDraftsByProvider();
    }

    @Test(expected = APIAuthenticationException.class)
    public void saveDraft_shouldThrowWhenAuthenticatedUserHasNoProvider() {
        when(providerService.getProvidersByPerson(authenticatedPerson, false)).thenReturn(Collections.emptyList());
        controller.saveDraft(buildFormDraftRequest(PATIENT_UUID, "{\"form\":\"data\"}"));
    }

    @Test(expected = APIAuthenticationException.class)
    public void getDraft_shouldThrowWhenAuthenticatedUserHasNoProvider() {
        when(providerService.getProvidersByPerson(authenticatedPerson, false)).thenReturn(Collections.emptyList());
        controller.getDraft(PATIENT_UUID);
    }

    @Test(expected = APIAuthenticationException.class)
    public void markDraftAsSaved_shouldThrowWhenAuthenticatedUserHasNoProvider() {
        when(providerService.getProvidersByPerson(authenticatedPerson, false)).thenReturn(Collections.emptyList());
        controller.markDraftAsSaved(PATIENT_UUID);
    }

    @Test(expected = APIAuthenticationException.class)
    public void discardDraft_shouldThrowWhenAuthenticatedUserHasNoProvider() {
        when(providerService.getProvidersByPerson(authenticatedPerson, false)).thenReturn(Collections.emptyList());
        controller.discardDraft(PATIENT_UUID);
    }

    @Test(expected = APIAuthenticationException.class)
    public void getDraft_shouldThrowWhenAuthenticatedUserIsNull() {
        controller.setAuthenticatedUser(null);
        controller.getDraft(PATIENT_UUID);
    }

    @Test(expected = APIAuthenticationException.class)
    public void getDraft_shouldThrowWhenAuthenticatedUserHasNoPerson() {
        User userWithNoPerson = new User();
        userWithNoPerson.setPerson(null);
        controller.setAuthenticatedUser(userWithNoPerson);
        controller.getDraft(PATIENT_UUID);
    }

    @Test(expected = APIAuthenticationException.class)
    public void getDraftsByProvider_shouldThrowWhenAuthenticatedUserIsNull() {
        controller.setAuthenticatedUser(null);
        controller.getDraftsByProvider();
    }

    @Test(expected = APIAuthenticationException.class)
    public void saveDraft_shouldThrowWhenAuthenticatedUserIsNull() {
        controller.setAuthenticatedUser(null);
        controller.saveDraft(buildFormDraftRequest(PATIENT_UUID, "{\"form\":\"data\"}"));
    }

    @Test(expected = APIAuthenticationException.class)
    public void markDraftAsSaved_shouldThrowWhenAuthenticatedUserIsNull() {
        controller.setAuthenticatedUser(null);
        controller.markDraftAsSaved(PATIENT_UUID);
    }

    @Test(expected = APIAuthenticationException.class)
    public void discardDraft_shouldThrowWhenAuthenticatedUserIsNull() {
        controller.setAuthenticatedUser(null);
        controller.discardDraft(PATIENT_UUID);
    }

    @Test(expected = APIAuthenticationException.class)
    public void getDraftsByProvider_shouldThrowWhenProvidersListIsNull() {
        when(providerService.getProvidersByPerson(authenticatedPerson, false)).thenReturn(null);
        controller.getDraftsByProvider();
    }

    @Test
    public void handleAuthenticationException_shouldReturn403() {
        APIAuthenticationException exception = new APIAuthenticationException("No provider");
        ResponseEntity<Object> response = controller.handleAuthenticationException(exception);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void saveDraft_shouldReturnOkWithFormDraftResponse() {
        FormDraftRequest request = buildFormDraftRequest(PATIENT_UUID, "{\"form\":\"data\"}");
        FormDraft draft = buildFormDraft(DRAFT_UUID, FORM_DATA_PATH);
        draft.setMarkedAsSaved(false);

        when(formDraftService.saveDraft(any(FormDraftRequest.class), any(String.class))).thenReturn(draft);
        when(formDraftService.getFormData(FORM_DATA_PATH)).thenReturn("{\"form\":\"data\"}");

        ResponseEntity<?> response = controller.saveDraft(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof FormDraftResponse);
        FormDraftResponse body = (FormDraftResponse) response.getBody();
        assertEquals(DRAFT_UUID, body.getUuid());
        assertEquals("{\"form\":\"data\"}", body.getFormData());
    }

    @Test
    public void saveDraft_shouldUseChangedDateForTimestampWhenPresent() {
        FormDraftRequest request = buildFormDraftRequest(PATIENT_UUID, "{\"form\":\"data\"}");
        FormDraft draft = buildFormDraft(DRAFT_UUID, FORM_DATA_PATH);
        draft.setDateChanged(new Date(5000L));
        draft.setMarkedAsSaved(false);

        when(formDraftService.saveDraft(any(FormDraftRequest.class), any(String.class))).thenReturn(draft);
        when(formDraftService.getFormData(FORM_DATA_PATH)).thenReturn("{\"form\":\"data\"}");

        ResponseEntity<?> response = controller.saveDraft(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        FormDraftResponse body = (FormDraftResponse) response.getBody();
        assertEquals(Long.valueOf(5000L), body.getTimestamp());
    }

    @Test
    public void getDraft_shouldReturnOkWithPopulatedResponseWhenDraftExists() {
        FormDraft draft = buildFormDraft(DRAFT_UUID, FORM_DATA_PATH);
        draft.setMarkedAsSaved(true);

        when(formDraftService.getDraft(PATIENT_UUID, PROVIDER_UUID)).thenReturn(draft);
        when(formDraftService.getFormData(FORM_DATA_PATH)).thenReturn("{\"existing\":\"data\"}");

        ResponseEntity<?> response = controller.getDraft(PATIENT_UUID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof FormDraftResponse);
        FormDraftResponse body = (FormDraftResponse) response.getBody();
        assertEquals(DRAFT_UUID, body.getUuid());
        assertEquals("{\"existing\":\"data\"}", body.getFormData());
        assertTrue(body.getMarkedAsSaved());
    }

    @Test
    public void getDraft_shouldUseChangedDateForTimestampWhenPresent() {
        FormDraft draft = buildFormDraft(DRAFT_UUID, FORM_DATA_PATH);
        draft.setDateChanged(new Date(7000L));
        draft.setMarkedAsSaved(false);

        when(formDraftService.getDraft(PATIENT_UUID, PROVIDER_UUID)).thenReturn(draft);
        when(formDraftService.getFormData(FORM_DATA_PATH)).thenReturn("{\"data\":\"value\"}");

        ResponseEntity<?> response = controller.getDraft(PATIENT_UUID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        FormDraftResponse body = (FormDraftResponse) response.getBody();
        assertEquals(Long.valueOf(7000L), body.getTimestamp());
    }

    @Test
    public void getDraft_shouldReturnInternalServerErrorWhenServiceThrowsRuntimeException() {
        doThrow(new RuntimeException("Unexpected failure")).when(formDraftService).getDraft(PATIENT_UUID, PROVIDER_UUID);

        ResponseEntity<?> response = controller.getDraft(PATIENT_UUID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void discardDraft_shouldReturnBadRequestWhenServiceThrowsIllegalArgument() {
        doThrow(new IllegalArgumentException("Patient UUID is required")).when(formDraftService)
                .discardDraft(PATIENT_UUID, PROVIDER_UUID);

        ResponseEntity<Object> response = controller.discardDraft(PATIENT_UUID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void discardDraft_shouldReturnInternalServerErrorWhenServiceThrowsRuntimeException() {
        doThrow(new RuntimeException("Unexpected error")).when(formDraftService)
                .discardDraft(PATIENT_UUID, PROVIDER_UUID);

        ResponseEntity<Object> response = controller.discardDraft(PATIENT_UUID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test(expected = APIAuthenticationException.class)
    public void resolveAuthenticatedProviderUuid_shouldThrowWhenProviderIsNull() {
        Provider nullProvider = null;
        when(providerService.getProvidersByPerson(authenticatedPerson, false))
                .thenReturn(Collections.singletonList(nullProvider));
        controller.getDraft(PATIENT_UUID);
    }

    @Test(expected = APIAuthenticationException.class)
    public void resolveAuthenticatedProviderUuid_shouldThrowWhenProviderUuidIsNull() {
        Provider providerWithNullUuid = new Provider();
        providerWithNullUuid.setUuid(null);
        when(providerService.getProvidersByPerson(authenticatedPerson, false))
                .thenReturn(Collections.singletonList(providerWithNullUuid));
        controller.getDraft(PATIENT_UUID);
    }

    // --- Helpers ---

    private FormDraftRequest buildFormDraftRequest(String patientUuid, String formData) {
        FormDraftRequest request = new FormDraftRequest();
        request.setPatientUuid(patientUuid);
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
