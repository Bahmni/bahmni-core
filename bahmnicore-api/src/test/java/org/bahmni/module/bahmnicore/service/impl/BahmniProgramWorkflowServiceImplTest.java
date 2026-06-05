package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.filter.BahmniProgramWorkflowStateFilter;
import org.openmrs.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.APIException;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class BahmniProgramWorkflowServiceImplTest {

    private BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Mock
    private ProgramWorkflowDAO bahmniProgramWorkflowDAO;

    @Mock
    private EpisodeService episodeService;

    @Mock
    private PatientProgram patientProgram;

    private Integer sampleId = 1234;
    private String sampleUuid = "a1b2c3";

    @Before
    public void before() {
        bahmniProgramWorkflowService = new BahmniProgramWorkflowServiceImpl(bahmniProgramWorkflowDAO, episodeService);
    }

    @Test
    public void testGetAllProgramAttributeTypes() throws Exception {
        bahmniProgramWorkflowService.getAllProgramAttributeTypes();
        verify(bahmniProgramWorkflowDAO).getAllProgramAttributeTypes();
    }

    @Test
    public void testGetProgramAttributeType() throws Exception {
        bahmniProgramWorkflowService.getProgramAttributeType(sampleId);
        verify(bahmniProgramWorkflowDAO).getProgramAttributeType(sampleId);
    }

    @Test
    public void testGetProgramAttributeTypeByUuid() throws Exception {
        bahmniProgramWorkflowService.getProgramAttributeTypeByUuid(sampleUuid);
        verify(bahmniProgramWorkflowDAO).getProgramAttributeTypeByUuid(sampleUuid);
    }

    @Test
    public void testSaveProgramAttributeType() throws Exception {
        ProgramAttributeType programAttributeType = new ProgramAttributeType();
        bahmniProgramWorkflowService.saveProgramAttributeType(programAttributeType);
        verify(bahmniProgramWorkflowDAO).saveProgramAttributeType(programAttributeType);
    }

    @Test
    public void testPurgeProgramAttributeType() throws Exception {
        ProgramAttributeType programAttributeType = new ProgramAttributeType();
        bahmniProgramWorkflowService.purgeProgramAttributeType(programAttributeType);
        verify(bahmniProgramWorkflowDAO).purgeProgramAttributeType(programAttributeType);
    }

    @Test
    public void testGetPatientProgramAttributeByUuid() throws Exception {
        bahmniProgramWorkflowService.getPatientProgramAttributeByUuid(sampleUuid);
        verify(bahmniProgramWorkflowDAO).getPatientProgramAttributeByUuid(sampleUuid);
    }

    @Test
    public void testSavePatientProgramShouldCreateEpisode() throws Exception {
        PatientProgram patientProgram = new PatientProgram();
        patientProgram.setPatient(new Patient());
        patientProgram.setProgram(new Program());
        when(bahmniProgramWorkflowDAO.savePatientProgram(patientProgram)).thenReturn(patientProgram);

        bahmniProgramWorkflowService.savePatientProgram(patientProgram);

        ArgumentCaptor<Episode> argumentCaptor = ArgumentCaptor.forClass(Episode.class);
        verify(episodeService).save(argumentCaptor.capture());
        verify(bahmniProgramWorkflowDAO).savePatientProgram(patientProgram);
        PatientProgram savedPatientProgram = argumentCaptor.getValue().getPatientPrograms().iterator().next();
        assertThat(savedPatientProgram.getUuid(), is(equalTo(patientProgram.getUuid())));
    }

    @Test
    public void testUpdatePatientProgramShouldNotCreateNewEpisode() throws Exception {
        Episode episode = new Episode();
        PatientProgram patientProgram = new PatientProgram();
        patientProgram.setPatient(new Patient());
        patientProgram.setProgram(new Program());
        when(bahmniProgramWorkflowDAO.savePatientProgram(patientProgram)).thenReturn(patientProgram);
        when(episodeService.getEpisodeForPatientProgram(patientProgram)).thenReturn(episode);

        bahmniProgramWorkflowService.savePatientProgram(patientProgram);

        verify(episodeService, times(0)).save(any(Episode.class));
        verify(bahmniProgramWorkflowDAO).savePatientProgram(patientProgram);
    }

    @Test
    public void testGetEncountersByPatientProgram() {
        Episode episode = new Episode();
        String patientProgramUuid = "patientProgramUuid";
        PatientProgram patientProgram = new PatientProgram();
        patientProgram.setUuid(patientProgramUuid);
        patientProgram.setPatient(new Patient());
        patientProgram.setProgram(new Program());

        when(bahmniProgramWorkflowDAO.getPatientProgramByUuid(patientProgramUuid)).thenReturn(patientProgram);
        when(episodeService.getEpisodeForPatientProgram(patientProgram)).thenReturn(episode);

        bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(patientProgramUuid);

        verify(bahmniProgramWorkflowDAO).getPatientProgramByUuid(patientProgramUuid);
        verify(episodeService).getEpisodeForPatientProgram(patientProgram);
    }

    @Test
    public void testNullEncountersByPatientProgramIfEpisodeCannotBeFound() {
        String patientProgramUuid = "patientProgramUuid";
        PatientProgram patientProgram = new PatientProgram();
        patientProgram.setUuid(patientProgramUuid);
        patientProgram.setPatient(new Patient());
        patientProgram.setProgram(new Program());

        when(bahmniProgramWorkflowDAO.getPatientProgramByUuid(patientProgramUuid)).thenReturn(patientProgram);
        when(episodeService.getEpisodeForPatientProgram(patientProgram)).thenReturn(null);

        bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(patientProgramUuid);

        verify(bahmniProgramWorkflowDAO).getPatientProgramByUuid(patientProgramUuid);
        verify(episodeService).getEpisodeForPatientProgram(patientProgram);
    }

    @Test
    public void shouldSetDateCompletedOfAProgramWhenItsOutcomeIsSetAndDateCompletedIsNull() {
        when(patientProgram.getPatient()).thenReturn(new Patient());
        when(patientProgram.getProgram()).thenReturn(new Program());
        when(patientProgram.getOutcome()).thenReturn(new Concept());
        when(patientProgram.getDateCompleted()).thenReturn(null);

        bahmniProgramWorkflowService.savePatientProgram(patientProgram);

        verify(patientProgram, times(1)).getOutcome();
        verify(patientProgram, times(1)).setDateCompleted(any(Date.class));
        verify(patientProgram, times(2)).getDateCompleted();
    }

    @Test(expected = APIException.class)
    public void getAllowedStatesForProgramShouldThrowExceptionWhenPatientProgramIsNull() {
        bahmniProgramWorkflowService.getAllowedStatesForProgram(null);
    }

    @Test(expected = APIException.class)
    public void getAllowedStatesForProgramShouldThrowExceptionWhenProgramIsNull() {
        PatientProgram patientProgram = new PatientProgram();
        bahmniProgramWorkflowService.getAllowedStatesForProgram(patientProgram);
    }

    @Test
    public void getAllowedStatesForProgramShouldReturnActiveStatesOnly() {
        PatientProgram patientProgram = new PatientProgram();
        Program program = new Program();
        patientProgram.setProgram(program);

        ProgramWorkflow workflow = new ProgramWorkflow();
        workflow.setProgram(program);

        ProgramWorkflowState activeState = new ProgramWorkflowState();
        activeState.setRetired(false);
        ProgramWorkflowState retiredState = new ProgramWorkflowState();
        retiredState.setRetired(true);

        Set<ProgramWorkflowState> states = new HashSet<>(Arrays.asList(activeState, retiredState));
        workflow.setStates(states);

        program.addWorkflow(workflow);

        List<ProgramWorkflowState> result = bahmniProgramWorkflowService.getAllowedStatesForProgram(patientProgram);

        assertThat(result, hasSize(1));
        assertTrue(result.contains(activeState));
    }

    @Test
    public void getAllowedStatesForProgramShouldReturnFilteredStatesWhenFilterIsConfigured() throws Exception {
        final ProgramWorkflowState filteredState1 = new ProgramWorkflowState();
        final ProgramWorkflowState filteredState2 = new ProgramWorkflowState();

        BahmniProgramWorkflowStateFilter filter = new BahmniProgramWorkflowStateFilter() {
            @Override
            public List<ProgramWorkflowState> filterAllowedStates(PatientProgram patientProgram) {
                return Arrays.asList(filteredState1, filteredState2);
            }
        };

        BahmniProgramWorkflowServiceImpl serviceImpl = (BahmniProgramWorkflowServiceImpl) bahmniProgramWorkflowService;
        Field filterField = BahmniProgramWorkflowServiceImpl.class.getDeclaredField("programWorkflowStateFilter");
        filterField.setAccessible(true);
        filterField.set(serviceImpl, filter);

        PatientProgram patientProgram = new PatientProgram();
        Program program = new Program();
        patientProgram.setProgram(program);

        ProgramWorkflow workflow = new ProgramWorkflow();
        workflow.setProgram(program);

        ProgramWorkflowState state1 = new ProgramWorkflowState();
        state1.setRetired(false);
        ProgramWorkflowState state2 = new ProgramWorkflowState();
        state2.setRetired(false);
        ProgramWorkflowState state3 = new ProgramWorkflowState();
        state3.setRetired(false);

        Set<ProgramWorkflowState> states = new HashSet<>(Arrays.asList(state1, state2, state3));
        workflow.setStates(states);

        program.addWorkflow(workflow);

        List<ProgramWorkflowState> result = bahmniProgramWorkflowService.getAllowedStatesForProgram(patientProgram);

        assertThat(result, hasSize(2));
        assertTrue(result.contains(filteredState1));
        assertTrue(result.contains(filteredState2));
    }
}
