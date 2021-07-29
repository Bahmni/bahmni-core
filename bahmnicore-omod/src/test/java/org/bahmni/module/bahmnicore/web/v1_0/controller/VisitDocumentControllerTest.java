package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.model.Document;
import org.bahmni.module.bahmnicore.security.PrivilegeConstants;
import org.bahmni.module.bahmnicore.service.PatientDocumentService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.bahmniemrapi.document.contract.VisitDocumentRequest;
import org.openmrs.module.bahmniemrapi.document.service.VisitDocumentService;
import org.openmrs.module.bahmniemrapi.visitlocation.BahmniVisitLocationService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class VisitDocumentControllerTest {
    @InjectMocks
    VisitDocumentController visitDocumentController;
    @Mock
    PatientService patientService;
    @Mock
    AdministrationService administrationService;
    @Mock
    PatientDocumentService patientDocumentService;
    @Mock
    VisitDocumentService visitDocumentService;
    @Mock
    BahmniVisitLocationService bahmniVisitLocationService;
    @Mock
    UserContext userContext;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldGetDefaultEncounterTypeIfNoEncounterTypeIsPassedInRequest() throws Exception {
        PowerMockito.mockStatic(Context.class);
        PowerMockito.when(Context.getPatientService()).thenReturn(patientService);
        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");
        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(patient);
        when(administrationService.getGlobalProperty("bahmni.encounterType.default")).thenReturn("consultation");

        Document document = new Document("abcd", "jpeg", null, "patient-uuid", "image");

        visitDocumentController.saveDocument(document);

        verify(patientDocumentService).saveDocument(1, "consultation", "abcd", "jpeg", document.getFileType());
        verify(administrationService).getGlobalProperty("bahmni.encounterType.default");
    }

    @Test
    public void shouldNotGetDefaultEncounterTypeIfEncounterTypeIsPassedInRequest() throws Exception {
        PowerMockito.mockStatic(Context.class);
        PowerMockito.when(Context.getPatientService()).thenReturn(patientService);
        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");
        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(patient);
        when(administrationService.getGlobalProperty("bahmni.encounterType.default")).thenReturn("consultation");

        Document document = new Document("abcd", "jpeg", "radiology", "patient-uuid", "image");

        visitDocumentController.saveDocument(document);

        verify(patientDocumentService).saveDocument(1, "radiology", "abcd", "jpeg", document.getFileType());
        verifyZeroInteractions(administrationService);
    }

    @Test
    public void shouldSetVisitLocationUuid() throws Exception {
        Visit visit = new Visit();
        visit.setUuid("visit-uuid");
        Encounter encounter = new Encounter();
        encounter.setUuid("encounterUuid");
        encounter.setVisit(visit);
        VisitDocumentRequest visitDocumentRequest = new VisitDocumentRequest("patient-uuid", "visit-uuid", "visit-type-uuid",
                null, null, "encounter-uuid", null, null, "provider-uuid", "location-uuid", null);

        when(visitDocumentService.upload(visitDocumentRequest)).thenReturn(encounter);

        when(bahmniVisitLocationService.getVisitLocationUuid("location-uuid")).thenReturn("VisitLocationuuid");
        visitDocumentController.save(visitDocumentRequest);

        verify(bahmniVisitLocationService).getVisitLocationUuid("location-uuid");
    }

    @Test
    public void shouldCallDeleteWithGivenFileNameIfUserIsAuthenticated() throws Exception {
        PowerMockito.mockStatic(Context.class);
        when(Context.getUserContext()).thenReturn(userContext);
        when(userContext.hasPrivilege(PrivilegeConstants.DELETE_PATIENT_DOCUMENT_PRIVILEGE)).thenReturn(true);
        visitDocumentController.deleteDocument("testFile.png");
        verify(patientDocumentService, times(1)).delete("testFile.png");
    }

    @Test
    public void shouldNotCallDeleteWithGivenFileNameIfUserIsNotAuthenticated() throws Exception {
        PowerMockito.mockStatic(Context.class);
        when(Context.getUserContext()).thenReturn(userContext);
        when(userContext.isAuthenticated()).thenReturn(false);
        visitDocumentController.deleteDocument("testFile.png");
        verifyZeroInteractions(patientDocumentService);
    }

    /**
     * Should be moved to PatientDocumentServiceImplIT
     * @throws Exception
     */
    @Test
    public void shouldReturnHttpBadRequestIfFileNameIsNull() throws Exception {
        PowerMockito.mockStatic(Context.class);
        when(Context.getUserContext()).thenReturn(userContext);
        when(userContext.hasPrivilege(PrivilegeConstants.DELETE_PATIENT_DOCUMENT_PRIVILEGE)).thenReturn(true);
        doThrow(RuntimeException.class).when(patientDocumentService).delete(any());
        ResponseEntity<Object> responseEntity = visitDocumentController.deleteDocument(null);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    /**
     * should be moved to PatientDocumentServiceImplIT
     * @throws Exception
     */
    @Test
    public void shouldNotCallDeleteWithGivenFileNameIfFileNameIsEmpty() throws Exception {
        PowerMockito.mockStatic(Context.class);
        when(Context.getUserContext()).thenReturn(userContext);
        when(userContext.hasPrivilege(PrivilegeConstants.DELETE_PATIENT_DOCUMENT_PRIVILEGE)).thenReturn(true);
        doThrow(RuntimeException.class).when(patientDocumentService).delete(any());
        ResponseEntity<Object> responseEntity = visitDocumentController.deleteDocument("");
        Assert.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
}
