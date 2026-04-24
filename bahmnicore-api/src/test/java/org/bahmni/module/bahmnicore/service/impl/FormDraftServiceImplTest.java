package org.bahmni.module.bahmnicore.service.impl;

import java.util.Collections;

import org.bahmni.module.bahmnicore.contract.FormDraftRequest;
import org.bahmni.module.bahmnicore.dao.FormDraftDAO;
import org.bahmni.module.bahmnicore.model.FormDraft;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class FormDraftServiceImplTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private FormDraftDAO formDraftDAO;

    @Mock
    private PatientService patientService;

    @Mock
    private UserService userService;

    @Mock
    private ProviderService providerService;

    @Mock
    private EncounterService encounterService;

    private FormDraftServiceImpl formDraftService;
    private Person person;

    private static final String PATIENT_UUID = "patient-uuid-123";
    private static final int PATIENT_ID = 1;
    private static final String PROVIDER_UUID = "provider-uuid-456";
    private static final int PROVIDER_ID = 2;
    private static final String ENCOUNTER_UUID = "encounter-uuid-789";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // Set OPENMRS_APPLICATION_DATA_DIRECTORY for test environment
        System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", temporaryFolder.getRoot().getAbsolutePath());

        formDraftService = new FormDraftServiceImpl();
        formDraftService.setFormDraftDAO(formDraftDAO);
        formDraftService.setPatientService(patientService);
        formDraftService.setUserService(userService);
        formDraftService.setProviderService(providerService);
        formDraftService.setEncounterService(encounterService);

        // Set authenticated user for testing
        User mockUser = new User();
        mockUser.setUuid("user-uuid");
        formDraftService.setAuthenticatedUser(mockUser);

        person = new Person();
    }

    @After
    public void tearDown() {
        // Clean up system property
        System.clearProperty("OPENMRS_APPLICATION_DATA_DIRECTORY");
    }

    @Test
    public void saveDraft_shouldCreateNewDraftWhenNoneExists() {
        FormDraftRequest request = buildRequest(PATIENT_UUID, PROVIDER_UUID, null, "{\"form\":\"data\"}");
        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);
        User user = buildUser(PROVIDER_UUID, PROVIDER_ID);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        mockProviderResolution(user);
        when(formDraftDAO.getLatestByPatientAndUser(PATIENT_ID, PROVIDER_ID)).thenReturn(null);
        when(formDraftDAO.saveOrUpdate(any(FormDraft.class))).thenAnswer(inv -> inv.getArguments()[0]);

        FormDraft result = formDraftService.saveDraft(request);

        assertNotNull(result);
        assertNotNull(result.getUuid());
        assertEquals(patient, result.getPatient());
        assertEquals(user, result.getUser());
        assertNotNull(result.getDateCreated());
        assertNull(result.getDateChanged());

        verify(formDraftDAO).saveOrUpdate(any(FormDraft.class));
    }

    @Test
    public void saveDraft_shouldUpdateExistingDraftForSamePatientProvider() {
        FormDraftRequest request = buildRequest(PATIENT_UUID, PROVIDER_UUID, null, "{\"updated\":\"data\"}");
        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);
        User user = buildUser(PROVIDER_UUID, PROVIDER_ID);

        FormDraft existingDraft = new FormDraft();
        existingDraft.setUuid("existing-uuid");
        existingDraft.setPatient(patient);
        existingDraft.setUser(user);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        mockProviderResolution(user);
        when(formDraftDAO.getLatestByPatientAndUser(PATIENT_ID, PROVIDER_ID)).thenReturn(existingDraft);
        when(formDraftDAO.saveOrUpdate(any(FormDraft.class))).thenAnswer(inv -> inv.getArguments()[0]);

        FormDraft result = formDraftService.saveDraft(request);

        assertEquals("existing-uuid", result.getUuid());
        assertNotNull(result.getDateChanged());
        verify(formDraftDAO).saveOrUpdate(existingDraft);
    }

    @Test
    public void saveDraft_shouldSetEncounterWhenEncounterUuidIsProvided() {
        FormDraftRequest request = buildRequest(PATIENT_UUID, PROVIDER_UUID, ENCOUNTER_UUID, "{\"form\":\"data\"}");
        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);
        User user = buildUser(PROVIDER_UUID, PROVIDER_ID);
        Encounter encounter = new Encounter();
        encounter.setUuid(ENCOUNTER_UUID);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        mockProviderResolution(user);
        when(formDraftDAO.getLatestByPatientAndUser(PATIENT_ID, PROVIDER_ID)).thenReturn(null);
        when(encounterService.getEncounterByUuid(ENCOUNTER_UUID)).thenReturn(encounter);
        when(formDraftDAO.saveOrUpdate(any(FormDraft.class))).thenAnswer(inv -> inv.getArguments()[0]);

        FormDraft result = formDraftService.saveDraft(request);

        assertEquals(encounter, result.getEncounter());
    }

    @Test
    public void saveDraft_shouldNotFailWhenEncounterUuidNotFound() {
        FormDraftRequest request = buildRequest(PATIENT_UUID, PROVIDER_UUID, "nonexistent-encounter", "{\"form\":\"data\"}");
        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);
        User user = buildUser(PROVIDER_UUID, PROVIDER_ID);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        mockProviderResolution(user);
        when(formDraftDAO.getLatestByPatientAndUser(PATIENT_ID, PROVIDER_ID)).thenReturn(null);
        when(encounterService.getEncounterByUuid("nonexistent-encounter")).thenReturn(null);
        when(formDraftDAO.saveOrUpdate(any(FormDraft.class))).thenAnswer(inv -> inv.getArguments()[0]);

        FormDraft result = formDraftService.saveDraft(request);

        assertNull(result.getEncounter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveDraft_shouldThrowWhenPatientUuidIsNull() {
        FormDraftRequest request = buildRequest(null, PROVIDER_UUID, null, "{\"form\":\"data\"}");
        formDraftService.saveDraft(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveDraft_shouldThrowWhenProviderUuidIsEmpty() {
        FormDraftRequest request = buildRequest(PATIENT_UUID, "", null, "{\"form\":\"data\"}");
        formDraftService.saveDraft(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveDraft_shouldThrowWhenFormDataIsNull() {
        FormDraftRequest request = buildRequest(PATIENT_UUID, PROVIDER_UUID, null, null);
        formDraftService.saveDraft(request);
    }

    @Test
    public void saveDraft_shouldPersistFormDataPath() {
        FormDraftRequest request = buildRequest(PATIENT_UUID, PROVIDER_UUID, null, "{\"form\":\"data\"}");
        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);
        User user = buildUser(PROVIDER_UUID, PROVIDER_ID);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        mockProviderResolution(user);
        when(formDraftDAO.getLatestByPatientAndUser(PATIENT_ID, PROVIDER_ID)).thenReturn(null);

        ArgumentCaptor<FormDraft> captor = ArgumentCaptor.forClass(FormDraft.class);
        when(formDraftDAO.saveOrUpdate(captor.capture())).thenAnswer(inv -> inv.getArguments()[0]);

        formDraftService.saveDraft(request);

        FormDraft saved = captor.getValue();
        assertNotNull(saved.getFormDataPath());
        assertTrue(saved.getFormDataPath().endsWith(".json"));
        assertTrue(saved.getFormDataPath().contains(saved.getUuid()));
    }

    @Test
    public void getDraft_shouldReturnDraftForValidPatientAndProvider() {
        FormDraft existingDraft = new FormDraft();
        existingDraft.setUuid("draft-uuid");
        existingDraft.setFormDataPath("/path/to/draft.json");

        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);
        User user = buildUser(PROVIDER_UUID, PROVIDER_ID);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        mockProviderResolution(user);
        when(formDraftDAO.getLatestByPatientAndUser(PATIENT_ID, PROVIDER_ID)).thenReturn(existingDraft);

        FormDraft result = formDraftService.getDraft(PATIENT_UUID, PROVIDER_UUID);

        assertNotNull(result);
        assertEquals("draft-uuid", result.getUuid());
    }

    @Test
    public void getDraft_shouldReturnNullWhenNoDraftExists() {
        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);
        User user = buildUser(PROVIDER_UUID, PROVIDER_ID);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        mockProviderResolution(user);
        when(formDraftDAO.getLatestByPatientAndUser(PATIENT_ID, PROVIDER_ID)).thenReturn(null);

        FormDraft result = formDraftService.getDraft(PATIENT_UUID, PROVIDER_UUID);

        assertNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDraft_shouldThrowWhenPatientUuidIsNull() {
        formDraftService.getDraft(null, PROVIDER_UUID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDraft_shouldThrowWhenProviderUuidIsEmpty() {
        formDraftService.getDraft(PATIENT_UUID, "");
    }

    @Test
    public void discardDraft_shouldCallDaoDeleteLatestDraft() {
        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);
        User user = buildUser(PROVIDER_UUID, PROVIDER_ID);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        mockProviderResolution(user);

        formDraftService.discardDraft(PATIENT_UUID, PROVIDER_UUID);

        verify(formDraftDAO).deleteLatestDraft(PATIENT_ID, PROVIDER_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void discardDraft_shouldThrowWhenPatientUuidIsNull() {
        formDraftService.discardDraft(null, PROVIDER_UUID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void discardDraft_shouldThrowWhenProviderUuidIsEmpty() {
        formDraftService.discardDraft(PATIENT_UUID, "");
    }

    @Test
    public void markDraftAsSaved_shouldUpdateDraftMarkedAsSavedFlag() {
        FormDraft existingDraft = new FormDraft();
        existingDraft.setUuid("draft-uuid");
        existingDraft.setMarkedAsSaved(false);

        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);
        User user = buildUser(PROVIDER_UUID, PROVIDER_ID);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        mockProviderResolution(user);
        when(formDraftDAO.getLatestByPatientAndUser(PATIENT_ID, PROVIDER_ID)).thenReturn(existingDraft);
        when(formDraftDAO.saveOrUpdate(any(FormDraft.class))).thenAnswer(inv -> inv.getArguments()[0]);

        formDraftService.markDraftAsSaved(PATIENT_UUID, PROVIDER_UUID);

        ArgumentCaptor<FormDraft> captor = ArgumentCaptor.forClass(FormDraft.class);
        verify(formDraftDAO).saveOrUpdate(captor.capture());

        FormDraft updatedDraft = captor.getValue();
        assertTrue(updatedDraft.getMarkedAsSaved());
        assertNotNull(updatedDraft.getDateChanged());
    }

    @Test
    public void markDraftAsSaved_shouldDoNothingWhenNoDraftExists() {
        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);
        User user = buildUser(PROVIDER_UUID, PROVIDER_ID);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        mockProviderResolution(user);
        when(formDraftDAO.getLatestByPatientAndUser(PATIENT_ID, PROVIDER_ID)).thenReturn(null);

        formDraftService.markDraftAsSaved(PATIENT_UUID, PROVIDER_UUID);

        verify(formDraftDAO, org.mockito.Mockito.never()).saveOrUpdate(any(FormDraft.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void markDraftAsSaved_shouldThrowWhenPatientUuidIsNull() {
        formDraftService.markDraftAsSaved(null, PROVIDER_UUID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void markDraftAsSaved_shouldThrowWhenPatientUuidIsEmpty() {
        formDraftService.markDraftAsSaved("", PROVIDER_UUID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void markDraftAsSaved_shouldThrowWhenProviderUuidIsNull() {
        formDraftService.markDraftAsSaved(PATIENT_UUID, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void markDraftAsSaved_shouldThrowWhenProviderUuidIsEmpty() {
        formDraftService.markDraftAsSaved(PATIENT_UUID, "");
    }

    @Test(expected = APIException.class)
    public void markDraftAsSaved_shouldThrowWhenPatientNotFound() {
        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(null);

        formDraftService.markDraftAsSaved(PATIENT_UUID, PROVIDER_UUID);
    }

    @Test(expected = APIException.class)
    public void markDraftAsSaved_shouldThrowWhenProviderNotFound() {
        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        when(providerService.getProviderByUuid(PROVIDER_UUID)).thenReturn(null);

        formDraftService.markDraftAsSaved(PATIENT_UUID, PROVIDER_UUID);
    }

    @Test
    public void saveDraft_shouldResolveUserViaProvider() {
        FormDraftRequest request = buildRequest(PATIENT_UUID, PROVIDER_UUID, null, "{\"form\":\"data\"}");
        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);
        User user = buildUser("user-uuid-999", PROVIDER_ID);

        mockProviderResolution(user);
        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        when(formDraftDAO.getLatestByPatientAndUser(PATIENT_ID, PROVIDER_ID)).thenReturn(null);
        when(formDraftDAO.saveOrUpdate(any(FormDraft.class))).thenAnswer(inv -> inv.getArguments()[0]);

        FormDraft result = formDraftService.saveDraft(request);

        assertNotNull(result);
        assertEquals(user, result.getUser());
    }

    @Test
    public void saveDraft_shouldCreateNewDraftWhenExistingDraftIsMarkedAsSaved() {
        FormDraftRequest request = buildRequest(PATIENT_UUID, PROVIDER_UUID, null, "{\"updated\":\"data\"}");
        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);
        User user = buildUser(PROVIDER_UUID, PROVIDER_ID);

        FormDraft markedDraft = new FormDraft();
        markedDraft.setUuid("marked-draft-uuid");
        markedDraft.setPatient(patient);
        markedDraft.setUser(user);
        markedDraft.setMarkedAsSaved(true);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        mockProviderResolution(user);
        when(formDraftDAO.getLatestByPatientAndUser(PATIENT_ID, PROVIDER_ID)).thenReturn(markedDraft);
        when(formDraftDAO.saveOrUpdate(any(FormDraft.class))).thenAnswer(inv -> inv.getArguments()[0]);

        FormDraft result = formDraftService.saveDraft(request);

        // Should create a new draft instead of updating the marked one
        assertNotNull(result.getUuid());
        assertNotEquals("marked-draft-uuid", result.getUuid());
        assertFalse(result.getMarkedAsSaved());
        verify(formDraftDAO).saveOrUpdate(any(FormDraft.class));
    }

    @Test
    public void saveDraft_shouldInitializeMarkedAsSavedAsFalseForNewDraft() {
        FormDraftRequest request = buildRequest(PATIENT_UUID, PROVIDER_UUID, null, "{\"form\":\"data\"}");
        Patient patient = buildPatient(PATIENT_UUID, PATIENT_ID);
        User user = buildUser(PROVIDER_UUID, PROVIDER_ID);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        mockProviderResolution(user);
        when(formDraftDAO.getLatestByPatientAndUser(PATIENT_ID, PROVIDER_ID)).thenReturn(null);

        ArgumentCaptor<FormDraft> captor = ArgumentCaptor.forClass(FormDraft.class);
        when(formDraftDAO.saveOrUpdate(captor.capture())).thenAnswer(inv -> inv.getArguments()[0]);

        formDraftService.saveDraft(request);

        FormDraft saved = captor.getValue();
        assertFalse(saved.getMarkedAsSaved());
    }

    // --- Helpers ---

    private void mockProviderResolution(User user) {
        Provider provider = new Provider();
        provider.setPerson(person);
        when(providerService.getProviderByUuid(PROVIDER_UUID)).thenReturn(provider);
        when(userService.getUsersByPerson(person, false)).thenReturn(Collections.singletonList(user));
    }

    private FormDraftRequest buildRequest(String patientUuid, String providerUuid, String encounterUuid, String formData) {
        FormDraftRequest request = new FormDraftRequest();
        request.setPatientUuid(patientUuid);
        request.setProviderUuid(providerUuid);
        request.setEncounterUuid(encounterUuid);
        request.setFormData(formData);
        return request;
    }

    private Patient buildPatient(String uuid, int patientId) {
        Patient patient = new Patient();
        patient.setUuid(uuid);
        patient.setPatientId(patientId);
        return patient;
    }

    private User buildUser(String uuid, int userId) {
        User user = new User();
        user.setUuid(uuid);
        user.setUserId(userId);
        return user;
    }
}
