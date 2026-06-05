package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PatientProgram;
import org.openmrs.api.context.Context;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class EpisodeServiceImplIT extends BaseIntegrationTest {
    @Autowired
    private EpisodeService episodeService;
    private BahmniProgramWorkflowService bahmniProgramWorkflowService;
    @Before
    public void setUp() throws Exception {
        bahmniProgramWorkflowService = Context.getService(BahmniProgramWorkflowService.class);
    }

    @Test
    public void shouldCreateANewEpisode() {
        Episode episode = createAnEpisode();
        assertThat(episode.getId(), is(notNullValue()));
        Episode savedEpisode = episodeService.get(episode.getId());
        assertThat(savedEpisode.getEncounters(), is(notNullValue()));
    }

    @Test
    public void shouldRetrieveEpisodeForAProgram() {
        createAnEpisode();
        PatientProgram patientProgram = testPatientProgram();

        Episode episodeForPatientProgram = episodeService.getEpisodeForPatientProgram(patientProgram);

        Set<PatientProgram> patientPrograms = episodeForPatientProgram.getPatientPrograms();
        assertThat(patientPrograms.size(), is(equalTo(1)));
        assertThat(patientPrograms.iterator().next().getUuid(), is(equalTo(patientProgram.getUuid())));
    }

    @Test
    public void shouldReturnNullIfPatientProgramIsNotLinkedToAnEpisode() {
        Episode episodeForPatientProgram = episodeService.getEpisodeForPatientProgram(testPatientProgram());

        assertThat(episodeForPatientProgram, is(nullValue()));
    }

    @Test
    public void shouldReturnNullEpisodeIfPatientProgramIsNull() {
        Episode episodeForPatientProgram = episodeService.getEpisodeForPatientProgram(null);

        assertThat(episodeForPatientProgram, is(nullValue()));
    }

    private Episode createAnEpisode() {
        Episode episode = new Episode();
        episode.addPatientProgram(testPatientProgram());
        episodeService.save(episode);
        return episode;
    }

    private PatientProgram testPatientProgram() {
        return bahmniProgramWorkflowService.getPatientProgram(1);
    }
}