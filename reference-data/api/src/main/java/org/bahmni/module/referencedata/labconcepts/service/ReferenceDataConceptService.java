/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.referencedata.labconcepts.service;

import org.bahmni.module.referencedata.labconcepts.contract.Concept;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptSet;

public interface ReferenceDataConceptService {
    public org.openmrs.Concept saveConcept(Concept concept);

    public org.openmrs.Concept saveConcept(ConceptSet conceptSet);

    public org.bahmni.module.referencedata.labconcepts.contract.Concepts getConcept(String conceptName);
}
