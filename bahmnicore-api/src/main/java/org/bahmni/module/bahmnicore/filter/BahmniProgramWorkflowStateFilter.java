package org.bahmni.module.bahmnicore.filter;

import org.openmrs.ProgramWorkflowState;

import java.util.List;

public interface BahmniProgramWorkflowStateFilter {
    List<ProgramWorkflowState> filterAllowedStates(List<ProgramWorkflowState> programStates);
}
