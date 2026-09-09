package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.security.PrivilegeConstants;
import org.bahmni.module.bahmnicore.service.PatientDocumentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class BahmniPatientImageControllerTest {

    private BahmniPatientImageController bahmniPatientImageController;

    @Mock
    private PatientDocumentService patientDocumentService;

    @Mock
    private UserContext userContext;

    @Mock
    private PatientService patientService;

    @Before
    public void setUp() throws IOException {
        PowerMockito.mockStatic(Context.class);
        PowerMockito.when(Context.getUserContext()).thenReturn(userContext);
        PowerMockito.when(Context.getPatientService()).thenReturn(patientService);
        bahmniPatientImageController = new BahmniPatientImageController(patientDocumentService);
    }

    @Test
    public void shouldRespondWithFileNotFoundStatusCodeIfTheImageIsNotFound() throws Exception {
        String patientUuid = "patientUuid";
        Mockito.when(userContext.isAuthenticated()).thenReturn(true);
        Mockito.when(userContext.hasPrivilege(PrivilegeConstants.GET_PATIENT_PHOTO)).thenReturn(true);
        Mockito.when(patientService.getPatientByUuid(patientUuid)).thenReturn(new Patient());
        when(patientDocumentService.retriveImage(anyString())).thenReturn(new ResponseEntity<Object>(new Object(), HttpStatus.OK));

        ResponseEntity<Object> responseEntity = bahmniPatientImageController.getImage(patientUuid);

        verify(patientDocumentService).retriveImage(patientUuid);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void shouldRespondWithNotAuthorizeStatusCodeIfTheImageIsNotFound() throws Exception {
        Mockito.when(userContext.isAuthenticated()).thenReturn(false);
        when(patientDocumentService.retriveImage(anyString())).thenReturn(new ResponseEntity<Object>(new Object(), HttpStatus.OK));
        String patientUuid = "patientUuid";

        ResponseEntity<Object> responseEntity = bahmniPatientImageController.getImage(patientUuid);

        verify(patientDocumentService, never()).retriveImage(patientUuid);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void shouldRespondWithForbiddenWhenUserLacksGetPatientPhotoPrivilege() throws Exception {
        String patientUuid = "patientUuid";
        Mockito.when(userContext.isAuthenticated()).thenReturn(true);
        Mockito.when(userContext.hasPrivilege(PrivilegeConstants.GET_PATIENT_PHOTO)).thenReturn(false);

        ResponseEntity<Object> responseEntity = bahmniPatientImageController.getImage(patientUuid);

        verify(patientDocumentService, never()).retriveImage(patientUuid);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    public void shouldRespondWithNotFoundWhenPatientUuidDoesNotResolveToAPatient() throws Exception {
        String patientUuid = "../outside/secret";
        Mockito.when(userContext.isAuthenticated()).thenReturn(true);
        Mockito.when(userContext.hasPrivilege(PrivilegeConstants.GET_PATIENT_PHOTO)).thenReturn(true);
        Mockito.when(patientService.getPatientByUuid(patientUuid)).thenReturn(null);

        ResponseEntity<Object> responseEntity = bahmniPatientImageController.getImage(patientUuid);

        verify(patientDocumentService, never()).retriveImage(patientUuid);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }
}