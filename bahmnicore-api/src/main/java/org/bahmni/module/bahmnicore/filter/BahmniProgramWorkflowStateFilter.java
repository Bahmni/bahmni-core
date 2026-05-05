package org.bahmni.module.bahmnicore.filter;

import org.openmrs.PatientProgram;
import org.openmrs.ProgramWorkflowState;

import java.util.List;

public interface BahmniProgramWorkflowStateFilter {
    List<ProgramWorkflowState> filterAllowedStates(PatientProgram patientProgram);
}
