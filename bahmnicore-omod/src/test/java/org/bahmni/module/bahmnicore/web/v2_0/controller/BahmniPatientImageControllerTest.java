package org.bahmni.module.bahmnicore.web.v2_0.controller;

import org.bahmni.module.bahmnicore.service.PatientDocumentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class BahmniPatientImageControllerTest {

    @Mock
    private PatientDocumentService patientDocumentService;

    @Mock
    private UserContext userContext;

    private BahmniPatientImageController bahmniPatientImageController;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
        PowerMockito.when(Context.getUserContext()).thenReturn(userContext);

        bahmniPatientImageController = new BahmniPatientImageController(patientDocumentService);
    }

    @Test
    public void shouldRetrieveImageWithoutDefaultWhenUserIsAuthenticated() {
        Mockito.when(userContext.isAuthenticated()).thenReturn(true);
        when(patientDocumentService.retriveImageWithoutDefault(anyString())).thenReturn(new ResponseEntity<Object>(new Object(), HttpStatus.OK));
        String patientUuid = "patientUuid";

        ResponseEntity<Object> responseEntity = bahmniPatientImageController.getImage(patientUuid);

        verify(patientDocumentService).retriveImageWithoutDefault(patientUuid);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void shouldNotRetrieveImageWithoutDefaultWhenUserIsNotAuthenticated() {
        Mockito.when(userContext.isAuthenticated()).thenReturn(false);
        when(patientDocumentService.retriveImageWithoutDefault(anyString())).thenReturn(new ResponseEntity<Object>(new Object(), HttpStatus.OK));
        String patientUuid = "patientUuid";

        ResponseEntity<Object> responseEntity = bahmniPatientImageController.getImage(patientUuid);

        verify(patientDocumentService, never()).retriveImageWithoutDefault(patientUuid);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturnNotFoundWhenPatientImageDoesNotExist() {
        Mockito.when(userContext.isAuthenticated()).thenReturn(true);
        when(patientDocumentService.retriveImageWithoutDefault(anyString())).thenReturn(new ResponseEntity<Object>(new Object(), HttpStatus.NOT_FOUND));
        String patientUuid = "patientUuid";

        ResponseEntity<Object> responseEntity = bahmniPatientImageController.getImage(patientUuid);

        verify(patientDocumentService).retriveImageWithoutDefault(patientUuid);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }
}
