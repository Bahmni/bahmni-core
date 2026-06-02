/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

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
