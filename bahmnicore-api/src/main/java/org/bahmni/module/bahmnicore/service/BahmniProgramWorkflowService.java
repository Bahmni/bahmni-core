/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.service;

import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.ProgramWorkflowService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface BahmniProgramWorkflowService extends ProgramWorkflowService {

    @Transactional(readOnly = true)
    @Authorized({"View Patient Programs"})
    Collection<Encounter> getEncountersByPatientProgramUuid(String patientProgramUuid);

    @Transactional(readOnly = true)
    @Authorized({"View Patient Programs"})
    List<ProgramWorkflowState> getAllowedStatesForProgram(PatientProgram program);

}
