package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.module.eventoutbox.EMREvent;
import org.bahmni.module.referencedata.helper.ConceptHelper;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;

import java.util.List;

import static org.bahmni.module.referencedata.labconcepts.contract.LabTest.LAB_TEST_CONCEPT_CLASSES;
import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.isOfAnyConceptClass;

public class LabTestEvent extends ConceptOperationEvent {

    public LabTestEvent(String url, String category, String title) {
        super(url, category, title);
    }

    public boolean isResourceConcept(Concept concept) {
        return isOfAnyConceptClass(concept, LAB_TEST_CONCEPT_CLASSES) || (getParentOfTypeLabTest(concept) != null);
    }

    private Concept getParentOfTypeLabTest(Concept concept) {
        ConceptHelper conceptHelper = new ConceptHelper(Context.getConceptService());
        List<Concept> parentConcepts = conceptHelper.getParentConcepts(concept);
        for (Concept parentConcept : parentConcepts) {
            if (isOfAnyConceptClass(parentConcept, LAB_TEST_CONCEPT_CLASSES)) {
                return parentConcept;
            }
        }
        return null;
    }

    @Override
    public EMREvent<Concept> asEMREvent(Object[] arguments) {
        Concept concept = (Concept) arguments[0];
        if (!isOfAnyConceptClass(concept, LAB_TEST_CONCEPT_CLASSES)) {
            concept = getParentOfTypeLabTest(concept);
        }
        String restUrl = String.format(this.url, title, concept.getUuid());
        return new EMREvent<>(concept, category, title, null, restUrl);
    }
}
