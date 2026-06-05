package org.bahmni.module.referencedata.labconcepts.model.event;

import org.openmrs.Concept;

import static org.bahmni.module.referencedata.labconcepts.contract.RadiologyTest.RADIOLOGY_TEST_CONCEPT_CLASSES;
import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.isOfAnyConceptClass;

public class RadiologyTestEvent extends ConceptOperationEvent {

    public RadiologyTestEvent(String url, String category, String title) {
        super(url, category, title);
    }


    @Override
    public boolean isResourceConcept(Concept concept) {
        return isOfAnyConceptClass(concept, RADIOLOGY_TEST_CONCEPT_CLASSES);
    }


}
