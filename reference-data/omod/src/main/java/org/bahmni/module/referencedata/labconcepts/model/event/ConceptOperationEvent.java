package org.bahmni.module.referencedata.labconcepts.model.event;

import org.ict4h.atomfeed.server.service.Event;
import java.time.LocalDateTime;
import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.api.context.Context;

import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

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
    public Event asAtomFeedEvent(Object[] arguments) throws URISyntaxException {
        Concept concept = (Concept) arguments[0];
        String url = String.format(this.url, title, concept.getUuid());
        return new Event(UUID.randomUUID().toString(), title, LocalDateTime.now(), url, url, category);
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
