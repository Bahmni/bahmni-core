/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

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