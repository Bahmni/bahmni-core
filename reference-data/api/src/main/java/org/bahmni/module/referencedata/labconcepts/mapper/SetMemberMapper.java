package org.bahmni.module.referencedata.labconcepts.mapper;

import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.api.context.Context;

import java.util.Collection;
import java.util.List;

public class SetMemberMapper {

    public Concept map(Concept concept, List<Concept> childConcepts) {
        removeAllSetMembers(concept);
        for (int i = 0; i < childConcepts.size(); i++) {
            addSetMember(concept, childConcepts.get(i), i);
        }
        return concept;
    }

    private void removeAllSetMembers(Concept concept) {
        Collection<ConceptSet> conceptSets = concept.getConceptSets();
        conceptSets.clear();
        concept.setConceptSets(conceptSets);
    }

    private org.openmrs.Concept addSetMember(Concept concept, Concept childConcept, Integer sortWeight) {
        if (ifChildExists(concept, childConcept)) return concept;
        concept.addSetMember(childConcept, sortWeight);
        return concept;
    }

    private boolean ifChildExists(Concept concept, Concept childConcept) {
        for (Concept child : concept.getSetMembers()) {
            if (child.getName(Context.getLocale()).getName().equals(childConcept.getName(Context.getLocale()).getName())) {
                return true;
            }
        }
        return false;
    }
}
