package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;
@Ignore
@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class BahmniProgramEnrollmentResourceTest {

    private BahmniProgramEnrollmentResource bahmniProgramEnrollmentResource;
    @Mock
    BahmniProgramWorkflowService bahmniProgramWorkflowService;
    @Mock
    RequestContext requestContext;
    @Mock
    HttpServletRequest httpServletRequest;
    @Mock
    private PatientService patientService;

    @Before
    public void before() throws Exception {
        initMocks(this);
        PowerMockito.mockStatic(Context.class);
        bahmniProgramEnrollmentResource = new BahmniProgramEnrollmentResource();
    }

    @Test
    public void shouldSearchProgramsByPatientUuid() throws Exception {
        String patientUuid = "patientUuid";
        when(requestContext.getRequest()).thenReturn(httpServletRequest);
        when(requestContext.getIncludeAll()).thenReturn(true);
        when(httpServletRequest.getParameter("patient")).thenReturn(patientUuid);
        when(Context.getPatientService()).thenReturn(patientService);
        when(Context.getService(BahmniProgramWorkflowService.class)).thenReturn(bahmniProgramWorkflowService);
        Patient patient = new Patient();
        when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        ArrayList<PatientProgram> expected = new ArrayList<>();
        when(bahmniProgramWorkflowService.getPatientPrograms(patient, null, null, null, null, null, true)).thenReturn(expected);

        PageableResult pageableResult = bahmniProgramEnrollmentResource.doSearch(requestContext);

        assertNotNull(pageableResult);
        assertThat("org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult", is(not(equalTo(pageableResult.getClass().getName()))));
        verify(requestContext, times(2)).getRequest();
        verify(requestContext, times(1)).getIncludeAll();
        verify(httpServletRequest, times(2)).getParameter(anyString());
        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(bahmniProgramWorkflowService, times(1)).getPatientPrograms(patient, null, null, null, null, null, true);
        verify(bahmniProgramWorkflowService, never()).getPatientProgramByUuid(anyString());
    }

    @Test
    public void shouldReturnEmptySearchResultIfThePatientIsNotExists() throws Exception {
        String patientUuid = "patientUuid";
        when(requestContext.getRequest()).thenReturn(httpServletRequest);
        when(requestContext.getIncludeAll()).thenReturn(true);
        when(httpServletRequest.getParameter("patient")).thenReturn(patientUuid);
        when(Context.getPatientService()).thenReturn(patientService);
        when(Context.getService(BahmniProgramWorkflowService.class)).thenReturn(bahmniProgramWorkflowService);
        when(patientService.getPatientByUuid(patientUuid)).thenReturn(null);
        ArrayList<PatientProgram> expected = new ArrayList<>();
        when(bahmniProgramWorkflowService.getPatientPrograms(any(Patient.class), any(Program.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), anyBoolean())).thenReturn(expected);

        PageableResult pageableResult = bahmniProgramEnrollmentResource.doSearch(requestContext);

        assertNotNull(pageableResult);
        assertEquals("org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult", pageableResult.getClass().getName());
        verify(requestContext, times(2)).getRequest();
        verify(httpServletRequest, times(2)).getParameter(anyString());
        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(requestContext, never()).getIncludeAll();
        verify(bahmniProgramWorkflowService, never()).getPatientPrograms(any(Patient.class), any(Program.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), anyBoolean());
        verify(bahmniProgramWorkflowService, never()).getPatientProgramByUuid(anyString());
    }

    @Test
    public void shouldSearchProgramByPatientProgramUuid() {
        String patientProgramUuid = "patientProgramUuid";
        when(requestContext.getRequest()).thenReturn(httpServletRequest);
        when(requestContext.getIncludeAll()).thenReturn(true);
        when(httpServletRequest.getParameter("patientProgramUuid")).thenReturn(patientProgramUuid);
        when(Context.getPatientService()).thenReturn(patientService);
        when(Context.getService(BahmniProgramWorkflowService.class)).thenReturn(bahmniProgramWorkflowService);
        PatientProgram patientProgram = new PatientProgram();
        when(bahmniProgramWorkflowService.getPatientProgramByUuid(patientProgramUuid)).thenReturn(patientProgram);

        PageableResult pageableResult = bahmniProgramEnrollmentResource.doSearch(requestContext);

        assertNotNull(pageableResult);
        assertThat("org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult", is(not(equalTo(pageableResult.getClass().getName()))));
        verify(requestContext, times(2)).getRequest();
        verify(httpServletRequest, times(2)).getParameter(anyString());
        verify(bahmniProgramWorkflowService, times(1)).getPatientProgramByUuid(anyString());
        verify(requestContext, never()).getIncludeAll();
        verify(patientService, never()).getPatientByUuid(anyString());
        verify(bahmniProgramWorkflowService, never()).getPatientPrograms(any(Patient.class), any(Program.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), anyBoolean());
    }

    @Test
    public void shouldReturnEmptySearchResultIfPatientProgramNotExists() {
        String patientProgramUuid = "patientProgramUuid";
        when(requestContext.getRequest()).thenReturn(httpServletRequest);
        when(requestContext.getIncludeAll()).thenReturn(true);
        when(httpServletRequest.getParameter("patientProgramUuid")).thenReturn(patientProgramUuid);
        when(Context.getPatientService()).thenReturn(patientService);
        when(Context.getService(BahmniProgramWorkflowService.class)).thenReturn(bahmniProgramWorkflowService);
        when(bahmniProgramWorkflowService.getPatientProgramByUuid(patientProgramUuid)).thenReturn(null);

        PageableResult pageableResult = bahmniProgramEnrollmentResource.doSearch(requestContext);

        assertNotNull(pageableResult);
        assertEquals("org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult", pageableResult.getClass().getName());
        verify(requestContext, times(2)).getRequest();
        verify(httpServletRequest, times(2)).getParameter(anyString());
        verify(bahmniProgramWorkflowService, times(1)).getPatientProgramByUuid(anyString());
        verify(requestContext, never()).getIncludeAll();
        verify(patientService, never()).getPatientByUuid(anyString());
        verify(bahmniProgramWorkflowService, never()).getPatientPrograms(any(Patient.class), any(Program.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), anyBoolean());
    }
}