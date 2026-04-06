package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.api.context.Context;

import java.util.List;

import static java.util.Arrays.asList;

public abstract class ConceptOperationEvent implements ConceptServiceOperationEvent {
    protected String url;
    protected String category;
    protected String title;

    public ConceptOperationEvent(String url, String category, String title) {
        this.url = url;
        this.category = category;
        this.title = title;
    }

    public ConceptOperationEvent() {
    }

    public abstract boolean isResourceConcept(Concept argument);

    @Override
    public Boolean isApplicable(String operation, Object[] arguments) {
        return this.operations().contains(operation) && isResourceConcept((Concept) arguments[0]);
    }

    private List<String> operations() {
        return asList("saveConcept", "updateConcept", "retireConcept", "purgeConcept");
    }

    @Override
    public EMREvent<Concept> asEMREvent(Object[] arguments) {
        Concept concept = (Concept) arguments[0];
        String restUrl = String.format(this.url, title, concept.getUuid());
        return new EMREvent<>(concept, category, title, null, restUrl);
    }

    public static boolean isChildOf(Concept concept, String parentConceptName) {
        List<ConceptSet> conceptSets = Context.getConceptService().getSetsContainingConcept(concept);
        if (conceptSets == null) return false;
        for (ConceptSet conceptSet : conceptSets) {
            if (conceptSet.getConceptSet().getName(Context.getLocale()).getName().equals(parentConceptName)) {
                return true;
            }
        }
        return false;
    }
}
