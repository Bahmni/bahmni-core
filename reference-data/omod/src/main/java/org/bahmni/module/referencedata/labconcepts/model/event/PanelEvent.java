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
import org.openmrs.ConceptClass;

import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.isOfConceptClassByUUID;

public class PanelEvent extends ConceptOperationEvent {

    public PanelEvent(String url, String category, String title) {
        super(url, category, title);
    }

    @Override
    public boolean isResourceConcept(Concept concept) {
        return isOfConceptClassByUUID(concept, ConceptClass.LABSET_UUID);
    }

}
