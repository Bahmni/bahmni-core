/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.dao;


import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;

import java.util.Collection;
import java.util.List;

public interface BahmniConceptDao {
    Collection<ConceptAnswer> searchByQuestion(Concept questionConcept, String searchQuery);
    Concept getConceptByFullySpecifiedName(String fullySpecifiedConceptName);
    Collection<Drug> getDrugByListOfConcepts(Collection<Concept> conceptSet);
    List searchDrugsByDrugName(Integer conceptSetId, String searchTerm);

    List getConceptsByFullySpecifiedName(List<String> conceptNames);
}
