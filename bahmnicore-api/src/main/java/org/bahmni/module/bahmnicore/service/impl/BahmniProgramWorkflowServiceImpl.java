/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.filter.BahmniProgramWorkflowStateFilter;
import org.bahmni.module.bahmnicore.service.BahmniProgramServiceValidator;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.APIException;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.openmrs.api.impl.ProgramWorkflowServiceImpl;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Transactional
public class BahmniProgramWorkflowServiceImpl extends ProgramWorkflowServiceImpl implements BahmniProgramWorkflowService {

    @Autowired
    private EpisodeService episodeService;
    @Autowired
    private List<BahmniProgramServiceValidator> bahmniProgramServiceValidators;
    @Autowired(required = false)
    private BahmniProgramWorkflowStateFilter programWorkflowStateFilter;
    private static final Logger logger =
            LogManager.getLogger(BahmniProgramWorkflowServiceImpl.class);

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
    public List<ProgramWorkflowState> getAllowedStatesForProgram(PatientProgram program) throws APIException {
        if (program == null || program.getProgram() == null) {
            logger.error("PatientProgram cannot be null when fetching allowed states");
            throw new APIException("PatientProgram cannot be null");
        }

        List<ProgramWorkflowState> states = new ArrayList<>();
        getAllActiveStates(program, states);

        if (programWorkflowStateFilter != null) {
            states = programWorkflowStateFilter.filterAllowedStates(program);
        } else {
            logger.debug("BahmniProgramWorkflowStateFilter not implemented, returning all states");
        }

        return states;
    }

    private void getAllActiveStates(PatientProgram program, List<ProgramWorkflowState> states) {
        Set<ProgramWorkflow> programWorkflows = program.getProgram().getAllWorkflows();
        if (CollectionUtils.isEmpty(programWorkflows)) {
            return;
        }

        for (ProgramWorkflow workflow : programWorkflows) {
            if (CollectionUtils.isEmpty(workflow.getStates())) {
                continue;
            }
            for (ProgramWorkflowState state : workflow.getStates()) {
                if (!state.getRetired()) {
                    states.add(state);
                }
            }
        }
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
