package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.module.referencedata.labconcepts.contract.AllTestsAndPanels;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;

public class AllTestsPanelsConceptSetEvent extends ConceptOperationEvent {


    public AllTestsPanelsConceptSetEvent(String url, String category, String title) {
        super(url, category, title);
    }

    @Override
    public boolean isResourceConcept(Concept concept) {
        return isAllTestAndPanelConcept(concept);
    }

    private boolean isAllTestAndPanelConcept(Concept concept) {
        return concept.getName(Context.getLocale()) != null &&
                concept.getName(Context.getLocale()).getName().equals(AllTestsAndPanels.ALL_TESTS_AND_PANELS);
    }

}
