package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import org.openmrs.Concept;

import java.util.Collection;

public class ConceptSortWeightUtil {
    public static int getSortWeightFor(String conceptName, Collection<Concept> concepts) {
        return getSortWeightFor(conceptName, concepts, 0);
    }

    private static int getSortWeightFor(String conceptName, Collection<Concept> concepts, int startSortWeight) {
        for (Concept aConcept : concepts) {
            startSortWeight++;
            if (aConcept.getName().getName().equalsIgnoreCase(conceptName)) {
                return startSortWeight;
            } else if (aConcept.getSetMembers().size() > 0 && getSortWeightFor(conceptName, aConcept.getSetMembers(), startSortWeight) > 0) {
                return getSortWeightFor(conceptName, aConcept.getSetMembers(), startSortWeight);
            } else if (aConcept.getSetMembers().size() > 0) {
                startSortWeight += aConcept.getSetMembers().size();
            }
        }
        return 0;
    }
}