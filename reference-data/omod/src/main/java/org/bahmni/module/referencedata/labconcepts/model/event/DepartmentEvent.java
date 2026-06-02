/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.referencedata.labconcepts.model.event;

import org.openmrs.Concept;

import static org.bahmni.module.referencedata.labconcepts.contract.Department.DEPARTMENT_CONCEPT_CLASS;
import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.isOfConceptClass;

public class DepartmentEvent extends ConceptOperationEvent {

    public DepartmentEvent(String url, String category, String title) {
        super(url, category, title);
    }

    @Override
    public boolean isResourceConcept(Concept concept) {
        return isOfConceptClass(concept, DEPARTMENT_CONCEPT_CLASS);
    }

}
