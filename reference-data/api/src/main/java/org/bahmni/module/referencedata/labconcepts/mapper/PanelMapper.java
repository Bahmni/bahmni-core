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
import org.bahmni.module.referencedata.labconcepts.contract.LabTest;
import org.bahmni.module.referencedata.labconcepts.contract.Panel;
import org.openmrs.Concept;

public class PanelMapper extends ResourceMapper {
    public PanelMapper() {
        super(AllTestsAndPanels.ALL_TESTS_AND_PANELS);
    }

    @Override
    public Panel map(Concept panelConcept) {
        Panel panel = new Panel();
        panel = mapResource(panel, panelConcept);
        panel.setTests(ConceptExtension.getResourceReferencesOfConceptClasses(panelConcept.getSetMembers(), LabTest.LAB_TEST_CONCEPT_CLASSES));
        panel.setSortOrder(getSortWeight(panelConcept));
        panel.setDescription(ConceptExtension.getDescriptionOrName(panelConcept));
        return panel;
    }
}
