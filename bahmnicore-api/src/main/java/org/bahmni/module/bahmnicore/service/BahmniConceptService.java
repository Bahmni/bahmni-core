/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.service;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Collection;
import java.util.List;

public interface BahmniConceptService {
    EncounterTransaction.Concept getConceptByName(String conceptName);

    Collection<ConceptAnswer> searchByQuestion(String questionConcept, String query);
    Collection<Drug> getDrugsByConceptSetName(String conceptSetName, String searchTerm);

    Concept getConceptByFullySpecifiedName(String drug);

    List<Concept> getConceptsByFullySpecifiedName(List<String> conceptNames);
}
