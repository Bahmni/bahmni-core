package org.bahmni.module.bahmnicore.encounterTransaction.command;

import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPostSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EpisodeEncounterCreateCommand implements EncounterDataPostSaveCommand {

    private EpisodeService episodeService;
    private BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Autowired
    public EpisodeEncounterCreateCommand(EpisodeService episodeService, BahmniProgramWorkflowService bahmniProgramWorkflowService) {
        this.episodeService = episodeService;
        this.bahmniProgramWorkflowService = bahmniProgramWorkflowService;
    }

    @Override
    public EncounterTransaction save(BahmniEncounterTransaction bahmniEncounterTransaction, Encounter currentEncounter, EncounterTransaction updatedEncounterTransaction) {
        if (!bahmniEncounterTransaction.isAssociatedToPatientProgram()) return updatedEncounterTransaction;

        PatientProgram patientProgram = bahmniProgramWorkflowService.getPatientProgramByUuid(bahmniEncounterTransaction.getPatientProgramUuid());
        Episode episode = getOrCreateEpisodeForPatientProgram(patientProgram);
        episode.addEncounter(currentEncounter);
        episodeService.save(episode);
        return updatedEncounterTransaction;
    }

    private Episode getOrCreateEpisodeForPatientProgram(PatientProgram patientProgram) {
        Episode episode = episodeService.getEpisodeForPatientProgram(patientProgram);
        return episode != null ? episode : createEpisode(patientProgram);
    }

    private Episode createEpisode(PatientProgram patientProgram) {
        Episode episode;
        episode = new Episode();
        episode.addPatientProgram(patientProgram);
        return episode;
    }
}
