/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.AllTestsAndPanels;
import org.openmrs.Concept;

public class AllTestsAndPanelsMapper extends ResourceMapper {
    public AllTestsAndPanelsMapper() {
        super(null);
    }

    @Override
    public AllTestsAndPanels map(Concept testsAndPanelsConcept) {
        AllTestsAndPanels allTestsAndPanels = new AllTestsAndPanels();
        allTestsAndPanels = mapResource(allTestsAndPanels, testsAndPanelsConcept);
        allTestsAndPanels.setDescription(ConceptExtension.getDescription(testsAndPanelsConcept));
        allTestsAndPanels.setTestsAndPanels(new TestAndPanelMapper().map(testsAndPanelsConcept));
        return allTestsAndPanels;
    }
}
