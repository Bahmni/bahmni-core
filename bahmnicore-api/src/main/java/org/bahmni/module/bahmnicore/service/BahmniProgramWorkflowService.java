package org.bahmni.module.bahmnicore.service;

import org.openmrs.Encounter;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.ProgramWorkflowService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public interface BahmniProgramWorkflowService extends ProgramWorkflowService {

    @Transactional(readOnly = true)
    @Authorized({"View Patient Programs"})
    Collection<Encounter> getEncountersByPatientProgramUuid(String patientProgramUuid);

}
