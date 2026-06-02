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
import org.openmrs.Concept;

public class LabTestMapper extends ResourceMapper {
    public LabTestMapper() {
        super(AllTestsAndPanels.ALL_TESTS_AND_PANELS);
    }

    @Override
    public LabTest map(Concept testConcept) {
        LabTest test = new LabTest();
        test = mapResource(test, testConcept);
        test.setDescription(ConceptExtension.getDescriptionOrName(testConcept));
        test.setResultType(testConcept.getDatatype().getName());
        test.setTestUnitOfMeasure(ConceptExtension.getUnits(testConcept));
        test.setSortOrder(getSortWeight(testConcept));
        test.setCodedTestAnswer(testConcept.getAnswers());
        return test;
    }


}
