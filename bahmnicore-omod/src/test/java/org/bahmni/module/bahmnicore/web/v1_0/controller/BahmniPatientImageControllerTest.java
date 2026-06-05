package org.bahmni.module.bahmnicore.web.v1_0.controller;

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

    @Before
    public void setUp() throws IOException {
        PowerMockito.mockStatic(Context.class);
        PowerMockito.when(Context.getUserContext()).thenReturn(userContext);
        bahmniPatientImageController = new BahmniPatientImageController(patientDocumentService);
    }

    @Test
    public void shouldRespondWithFileNotFoundStatusCodeIfTheImageIsNotFound() throws Exception {
        Mockito.when(userContext.isAuthenticated()).thenReturn(true);
        when(patientDocumentService.retriveImage(anyString())).thenReturn(new ResponseEntity<Object>(new Object(), HttpStatus.OK));
        String patientUuid = "patientUuid";

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
}