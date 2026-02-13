package org.bahmni.module.bahmnicore.filter.impl;

import org.bahmni.module.bahmnicore.filter.BahmniProgramWorkflowStateFilter;
import org.openmrs.ProgramWorkflowState;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BahmniProgramWorkflowStateFilterImpl implements BahmniProgramWorkflowStateFilter {

    @Override
    public List<ProgramWorkflowState> filterAllowedStates(List<ProgramWorkflowState> programStates) {
        return programStates;
    }
}
