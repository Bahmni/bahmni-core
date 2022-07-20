package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramServiceValidator;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.api.APIException;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.openmrs.api.impl.ProgramWorkflowServiceImpl;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Transactional
public class BahmniProgramWorkflowServiceImpl extends ProgramWorkflowServiceImpl implements BahmniProgramWorkflowService {

    @Autowired
    private EpisodeService episodeService;
    @Autowired
    private List<BahmniProgramServiceValidator> bahmniProgramServiceValidators;

    public BahmniProgramWorkflowServiceImpl(ProgramWorkflowDAO programWorkflowDAO, EpisodeService episodeService) {
        this.episodeService = episodeService;
        this.dao = programWorkflowDAO;
    }

    //Default constructor to satisfy Spring
    public BahmniProgramWorkflowServiceImpl() {
    }

    @Override
    public Collection<Encounter> getEncountersByPatientProgramUuid(String patientProgramUuid) {
        PatientProgram patientProgram = dao.getPatientProgramByUuid(patientProgramUuid);
        Episode episode = episodeService.getEpisodeForPatientProgram(patientProgram);
        return episode == null ? Collections.EMPTY_LIST : episode.getEncounters();
    }

    @Override
    public PatientProgram savePatientProgram(PatientProgram patientProgram) throws APIException {
        preSaveValidation(patientProgram);
        if (patientProgram.getOutcome() != null && patientProgram.getDateCompleted() == null) {
            patientProgram.setDateCompleted(new Date());
        }
        PatientProgram bahmniPatientProgram = super.savePatientProgram(patientProgram);
        createEpisodeIfRequired(bahmniPatientProgram);
        return bahmniPatientProgram;
    }

    private void preSaveValidation(PatientProgram patientProgram) {
        if(CollectionUtils.isNotEmpty(bahmniProgramServiceValidators)) {
            for (BahmniProgramServiceValidator bahmniProgramServiceValidator : bahmniProgramServiceValidators) {
                bahmniProgramServiceValidator.validate(patientProgram);
            }
        }
    }

    private void createEpisodeIfRequired(PatientProgram bahmniPatientProgram) {
        if (episodeService.getEpisodeForPatientProgram(bahmniPatientProgram) != null) return;
        Episode episode = new Episode();
        episode.addPatientProgram(bahmniPatientProgram);
        episodeService.save(episode);
    }
}
