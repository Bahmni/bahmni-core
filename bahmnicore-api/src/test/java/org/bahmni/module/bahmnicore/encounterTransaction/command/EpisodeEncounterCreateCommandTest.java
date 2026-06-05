package org.bahmni.module.bahmnicore.encounterTransaction.command;

import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EpisodeEncounterCreateCommandTest {

    @Mock
    private EpisodeService episodeService;

    @Mock
    BahmniProgramWorkflowService programWorkflowService;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void shouldAddEncounterToEpisode() {
        BahmniEncounterTransaction encounterTransaction = new BahmniEncounterTransaction();
        encounterTransaction.setPatientProgramUuid("foo");

        PatientProgram testPatientProgram = testPatientProgram();
        when(programWorkflowService.getPatientProgramByUuid("foo")).thenReturn(testPatientProgram);
        Episode testEpisode = testEpisode(testPatientProgram);
        when(episodeService.getEpisodeForPatientProgram(testPatientProgram)).thenReturn(testEpisode);

        Encounter currentEncounter = new Encounter();
        new EpisodeEncounterCreateCommand(episodeService, programWorkflowService).save(encounterTransaction, currentEncounter, null);

        verify(programWorkflowService).getPatientProgramByUuid("foo");
        verify(episodeService).getEpisodeForPatientProgram(testPatientProgram);
        verify(episodeService).save(testEpisode);
        assertTrue(testEpisode.getEncounters().contains(currentEncounter));
    }

    @Test
    public void shouldIgnoreIfEncounterHasNoPatientProgramAssociated() {
        BahmniEncounterTransaction encounterTransaction = new BahmniEncounterTransaction();

        PatientProgram testPatientProgram = testPatientProgram();
        when(programWorkflowService.getPatientProgramByUuid("foo")).thenReturn(testPatientProgram);
        Episode testEpisode = testEpisode(testPatientProgram);
        when(episodeService.getEpisodeForPatientProgram(testPatientProgram)).thenReturn(testEpisode);

        Encounter currentEncounter = new Encounter();
        new EpisodeEncounterCreateCommand(episodeService, programWorkflowService).save(encounterTransaction, currentEncounter, null);

        verify(programWorkflowService, times(0)).getPatientProgramByUuid("foo");
        verify(episodeService, times(0)).getEpisodeForPatientProgram(testPatientProgram);
        verify(episodeService, times(0)).save(testEpisode);
    }

    @Test
    public void shouldCreateEpisodeAndAssociatePatientProgramIfItDoesntExist() {
        BahmniEncounterTransaction encounterTransaction = new BahmniEncounterTransaction();
        encounterTransaction.setPatientProgramUuid("foo");

        PatientProgram testPatientProgram = testPatientProgram();
        when(programWorkflowService.getPatientProgramByUuid("foo")).thenReturn(testPatientProgram);

        when(episodeService.getEpisodeForPatientProgram(testPatientProgram)).thenReturn(null);

        Encounter currentEncounter = new Encounter();
        new EpisodeEncounterCreateCommand(episodeService, programWorkflowService).save(encounterTransaction, currentEncounter, null);

        verify(programWorkflowService).getPatientProgramByUuid("foo");
        verify(episodeService).getEpisodeForPatientProgram(testPatientProgram);
        ArgumentCaptor<Episode> episodeArgumentCaptor = ArgumentCaptor.forClass(Episode.class);
        verify(episodeService).save(episodeArgumentCaptor.capture());
        Episode episode = episodeArgumentCaptor.getValue();
        assertTrue(episode.getEncounters().contains(currentEncounter));
        assertTrue(episode.getPatientPrograms().contains(testPatientProgram));
    }


    private Episode testEpisode(PatientProgram testPatientProgram) {
        Episode episode = new Episode();
        episode.addPatientProgram(testPatientProgram);
        return episode;
    }

    private PatientProgram testPatientProgram() {
        PatientProgram bahmniPatientProgram = new PatientProgram();
        bahmniPatientProgram.setUuid("bar");
        return bahmniPatientProgram;
    }
}
