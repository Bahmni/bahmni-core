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
