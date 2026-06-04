package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.module.referencedata.labconcepts.contract.AllSamples;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;

public class AllLabSamplesEvent extends ConceptOperationEvent {

    public AllLabSamplesEvent(String conceptUrl, String labCategory, String title) {
        super(conceptUrl, labCategory, title);
    }

    @Override
    public boolean isResourceConcept(Concept concept) {
        return isLabSamplesConcept(concept);
    }


    private boolean isLabSamplesConcept(Concept concept) {
        return concept.getName(Context.getLocale()) != null && concept.getName(Context.getLocale()).getName().equals(AllSamples.ALL_SAMPLES);
    }
}
