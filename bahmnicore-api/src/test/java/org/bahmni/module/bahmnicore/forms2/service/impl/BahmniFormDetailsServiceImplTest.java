package org.bahmni.module.bahmnicore.forms2.service.impl;

import org.bahmni.module.bahmnicore.forms2.contract.FormType;
import org.bahmni.module.bahmnicore.forms2.contract.FormDetails;
import org.bahmni.module.bahmnicore.forms2.mapper.FormDetailsMapper;
import org.bahmni.module.bahmnicore.forms2.util.FormUtil;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.bahmni.module.bahmnicore.service.BahmniVisitService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.internal.verification.VerificationModeFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest({FormType.class, FormUtil.class, FormDetailsMapper.class})
@RunWith(PowerMockRunner.class)
public class BahmniFormDetailsServiceImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private BahmniVisitService bahmniVisitService = mock(BahmniVisitService.class);
    private FormDetails formDetails = mock(FormDetails.class);
    private FormDetails anotherFormDetails = mock(FormDetails.class);
    private VisitService visitService = mock(VisitService.class);
    private BahmniProgramWorkflowService bahmniProgramWorkflowService = mock(BahmniProgramWorkflowService.class);
    private PatientService patientService = mock(PatientService.class);
    private EncounterService encounterService = mock(EncounterService.class);
    private ObsService obsService = mock(ObsService.class);
    private Patient patient = mock(Patient.class);
    private Person person = mock(Person.class);
    private Visit visit = mock(Visit.class);
    private Encounter encounter = mock(Encounter.class);
    private Obs height = mock(Obs.class);
    private Obs weight = mock(Obs.class);
    private List<Obs> obs = Arrays.asList(height, weight);
    private BahmniFormDetailsServiceImpl bahmniFormDetailsService;
    private String patientUuid = "patient-uuid";
    private String patientProgramUuid = "patient-program-uuid";
    private String visitUuid = "visit-uuid";

    @Before
    public void setUp() {
        bahmniFormDetailsService = new BahmniFormDetailsServiceImpl(patientService, visitService,
                encounterService, obsService, bahmniVisitService, bahmniProgramWorkflowService);

        when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        when(visitService.getVisitsByPatient(patient)).thenReturn(singletonList(visit));
        List<Encounter> encounters = singletonList(encounter);
        when(encounterService.getEncounters(any(EncounterSearchCriteria.class))).thenReturn(encounters);
        when(patient.getPerson()).thenReturn(person);
        when(obsService.getObservations(anyListOf(Person.class), anyListOf(Encounter.class), eq(null), eq(null), eq(null), eq(null),
                eq(null), eq(null), eq(null), eq(null), eq(null), eq(false))).thenReturn(obs);
    }

    @Test
    public void shouldReturnInvalidParameterExceptionIfPatientDoesNotFound() {

        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(null);
        expectedException.expect(InvalidParameterException.class);
        expectedException.expectMessage("Patient does not exist");

        Collection<FormDetails> formDetailsCollection = bahmniFormDetailsService.getFormDetails("patient uuid", FormType.FORMS1, -1);

        assertEquals(0, formDetailsCollection.size());

    }

    @Test
    public void shouldReturnFormDetailsForGivenPatientUuidAndFormTypeIsV2() {
        mockFilterFormBuilderObs();
        mockStatic(FormDetailsMapper.class);
        List<FormDetails> expectedFormDetails = Arrays.asList(formDetails, anotherFormDetails);
        when(FormDetailsMapper.createFormDetails(anyListOf(Obs.class), any(FormType.class)))
                .thenReturn(expectedFormDetails);
        Collection<FormDetails> formBuilderFormDetails = bahmniFormDetailsService.getFormDetails("patient-uuid", FormType.FORMS2, -1);

        assertEquals(2, formBuilderFormDetails.size());
        containsInAnyOrder(expectedFormDetails, formBuilderFormDetails.toArray());

        verifyCommonMockCalls();
        verifyFilterFormBuilderObsMockCall(1);
        verifyCreateFormDetailsMockCall(1);
    }

    @Test
    public void shouldReturnFormDetailsOfTypeV2ForGivenPatientUuidAndNoFormTypeIsProvided() {
        mockFilterFormBuilderObs();

        mockStatic(FormDetailsMapper.class);
        List<FormDetails> expectedFormDetails = Arrays.asList(formDetails, anotherFormDetails);
        when(FormDetailsMapper.createFormDetails(anyListOf(Obs.class), any(FormType.class)))
                .thenReturn(expectedFormDetails);
        Collection<FormDetails> formBuilderFormDetails = bahmniFormDetailsService.getFormDetails("patient-uuid", null, -1);

        assertEquals(2, formBuilderFormDetails.size());
        containsInAnyOrder(expectedFormDetails, formBuilderFormDetails.toArray());

        verifyCommonMockCalls();
        verifyFilterFormBuilderObsMockCall(1);
        verifyCreateFormDetailsMockCall(1);
    }

    @Test
    public void shouldReturnEmptyCollectionOfFormDetailsIfFormTypeIsAvailableButNotV2() {
        Collection<FormDetails> formBuilderFormDetails = bahmniFormDetailsService.getFormDetails("patient-uuid", FormType.FORMS1, -1);

        assertEquals(0, formBuilderFormDetails.size());
    }

    @Test
    public void shouldReturnFormDetailsGivenPatientUuidFormTypeAsV2AndNumberOfVisitsAreOne() {
        Visit anotherVisit = mock(Visit.class);
        when(visitService.getVisitsByPatient(patient)).thenReturn(Arrays.asList(anotherVisit, visit));

        mockFilterFormBuilderObs();

        mockStatic(FormDetailsMapper.class);
        when(FormDetailsMapper.createFormDetails(anyListOf(Obs.class), any(FormType.class)))
                .thenReturn(singletonList(formDetails));
        Collection<FormDetails> formBuilderFormDetails = bahmniFormDetailsService.getFormDetails("patient-uuid", FormType.FORMS2, 1);

        assertEquals(1, formBuilderFormDetails.size());
        assertEquals(formDetails, formBuilderFormDetails.iterator().next());

        verifyCommonMockCalls();

        verifyFilterFormBuilderObsMockCall(1);

        verifyCreateFormDetailsMockCall(1);

    }

    @Test
    public void shouldReturnEmptyCollectionsOfFormDetailsIfPatientDoesNotHaveVisits() {
        when(visitService.getVisitsByPatient(patient)).thenReturn(Collections.emptyList());
        Collection<FormDetails> formDetailsCollection = bahmniFormDetailsService.getFormDetails(patientUuid, FormType.FORMS2, -1);

        assertEquals(0, formDetailsCollection.size());

        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(visitService, times(1)).getVisitsByPatient(patient);
        verify(encounterService, times(0)).getEncounters(any(EncounterSearchCriteria.class));

        verify(patient, times(0)).getPerson();
        verify(obsService, times(0)).getObservations(anyListOf(Person.class),
                anyListOf(Encounter.class), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(Boolean.class));

    }

    @Test
    public void shouldReturnEmptyCollectionsOfFormDetailsIfPatientDoesNotHaveEncounters() {
        when(encounterService.getEncounters(any(EncounterSearchCriteria.class))).thenReturn(Collections.emptyList());
        Collection<FormDetails> formDetailsCollection = bahmniFormDetailsService.getFormDetails(patientUuid, FormType.FORMS2, -1);

        assertEquals(0, formDetailsCollection.size());

        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(visitService, times(1)).getVisitsByPatient(patient);
        verify(encounterService, times(1)).getEncounters(any(EncounterSearchCriteria.class));

        verify(patient, times(0)).getPerson();
        verify(obsService, times(0)).getObservations(anyListOf(Person.class),
                anyListOf(Encounter.class), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(Boolean.class));
    }

    @Test
    public void shouldReturnFormDetailsGivenPatientUuidFormTypeAsV2AndVisitUuid() {
        when(bahmniVisitService.getVisitSummary(visitUuid)).thenReturn(visit);
        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(any(String.class)))
                .thenReturn(Collections.emptyList());

        mockFilterFormBuilderObs();

        mockStatic(FormDetailsMapper.class);
        when(FormDetailsMapper.createFormDetails(anyListOf(Obs.class), any(FormType.class)))
                .thenReturn(singletonList(formDetails));

        Collection<FormDetails> actualFormDetailsCollection = bahmniFormDetailsService.getFormDetails(patientUuid, FormType.FORMS2, visitUuid, null);

        assertEquals(1, actualFormDetailsCollection.size());
        assertEquals(formDetails, actualFormDetailsCollection.iterator().next());

        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(encounterService, times(1)).getEncounters(any(EncounterSearchCriteria.class));
        verify(patient, times(1)).getPerson();
        verify(obsService, times(1)).getObservations(anyListOf(Person.class),
                anyListOf(Encounter.class), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(Boolean.class));
        verify(bahmniVisitService, times(1)).getVisitSummary(visitUuid);
        verify(bahmniProgramWorkflowService, times(1)).getEncountersByPatientProgramUuid(null);

        verifyFilterFormBuilderObsMockCall(1);

        verifyCreateFormDetailsMockCall(1);
    }

    @Test
    public void shouldReturnFormDetailsGivenPatientUuidFormTypeAsV2AndPatientProgramUuid() {
        when(bahmniVisitService.getVisitSummary(null)).thenReturn(null);
        when(encounterService.getEncounters(any(EncounterSearchCriteria.class))).thenReturn(null);
        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(patientProgramUuid))
                .thenReturn(singletonList(encounter));

        mockFilterFormBuilderObs();

        mockStatic(FormDetailsMapper.class);
        when(FormDetailsMapper.createFormDetails(anyListOf(Obs.class), any(FormType.class)))
                .thenReturn(singletonList(formDetails));

        Collection<FormDetails> actualFormDetailsCollection = bahmniFormDetailsService.getFormDetails(patientUuid, FormType.FORMS2, null, patientProgramUuid);

        assertEquals(1, actualFormDetailsCollection.size());
        assertEquals(formDetails, actualFormDetailsCollection.iterator().next());

        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(encounterService, times(1)).getEncounters(any(EncounterSearchCriteria.class));
        verify(patient, times(1)).getPerson();
        verify(obsService, times(1)).getObservations(anyListOf(Person.class),
                anyListOf(Encounter.class), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(Boolean.class));
        verify(bahmniVisitService, times(1)).getVisitSummary(null);
        verify(bahmniProgramWorkflowService, times(1)).getEncountersByPatientProgramUuid(patientProgramUuid);

        verifyFilterFormBuilderObsMockCall(1);

        verifyCreateFormDetailsMockCall(1);
    }

    @Test
    public void shouldReturnFormDetailsGivenPatientUuidFormTypeAsV2VisitUuidAndPatientProgramUuid() {
        when(bahmniVisitService.getVisitSummary(visitUuid)).thenReturn(visit);
        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(patientProgramUuid))
                .thenReturn(singletonList(encounter));
        when(encounter.getVisit()).thenReturn(visit);

        mockFilterFormBuilderObs();

        mockStatic(FormDetailsMapper.class);
        when(FormDetailsMapper.createFormDetails(anyListOf(Obs.class), any(FormType.class)))
                .thenReturn(singletonList(formDetails));

        Collection<FormDetails> actualFormDetailsCollection = bahmniFormDetailsService.getFormDetails(patientUuid, FormType.FORMS2, visitUuid, patientProgramUuid);

        assertEquals(1, actualFormDetailsCollection.size());
        assertEquals(formDetails, actualFormDetailsCollection.iterator().next());

        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(encounterService, times(1)).getEncounters(any(EncounterSearchCriteria.class));
        verify(patient, times(1)).getPerson();
        verify(obsService, times(1)).getObservations(anyListOf(Person.class),
                anyListOf(Encounter.class), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(Boolean.class));
        verify(bahmniVisitService, times(1)).getVisitSummary(visitUuid);
        verify(bahmniProgramWorkflowService, times(1)).getEncountersByPatientProgramUuid(patientProgramUuid);

        verifyFilterFormBuilderObsMockCall(1);

        verifyCreateFormDetailsMockCall(1);
    }

    @Test
    public void shouldReturnEmptyCollectionOfFormDetailsGivenPatientUuidFormTypeAsV2InvalidVisitUuidAndInvalidPatientProgramUuid() {
        when(bahmniVisitService.getVisitSummary(visitUuid)).thenReturn(null);
        when(encounterService.getEncounters(any(EncounterSearchCriteria.class))).thenReturn(null);
        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(patientProgramUuid))
                .thenReturn(null);

        Collection<FormDetails> actualFormDetailsCollection = bahmniFormDetailsService
                .getFormDetails(patientUuid, FormType.FORMS2, visitUuid, patientProgramUuid);

        assertEquals(0, actualFormDetailsCollection.size());

        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(encounterService, times(1)).getEncounters(any(EncounterSearchCriteria.class));
        verify(bahmniVisitService, times(1)).getVisitSummary(visitUuid);
        verify(bahmniProgramWorkflowService, times(1)).getEncountersByPatientProgramUuid(patientProgramUuid);
    }

    public void verifyCreateFormDetailsMockCall(int wantedNumberOfInvocations) {
        verifyStatic(FormDetailsMapper.class, VerificationModeFactory.times(wantedNumberOfInvocations));
        FormDetailsMapper.createFormDetails(anyListOf(Obs.class), any(FormType.class));
    }

    private void verifyFilterFormBuilderObsMockCall(int wantedNumberOfInvocations) {
        verifyStatic(FormUtil.class, VerificationModeFactory.times(wantedNumberOfInvocations));
        FormUtil.filterFormBuilderObs(obs);
    }

    private void mockFilterFormBuilderObs() {
        mockStatic(FormUtil.class);
        when(FormUtil.filterFormBuilderObs(obs)).thenReturn(obs);
    }

    private void verifyCommonMockCalls() {
        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(visitService, times(1)).getVisitsByPatient(patient);
        verify(encounterService, times(1)).getEncounters(any(EncounterSearchCriteria.class));
        verify(patient, times(1)).getPerson();
        verify(obsService, times(1)).getObservations(anyListOf(Person.class),
                anyListOf(Encounter.class), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(Boolean.class));
    }

}
