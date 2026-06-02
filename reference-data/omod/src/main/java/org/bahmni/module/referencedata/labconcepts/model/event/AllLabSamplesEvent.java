/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.module.referencedata.labconcepts.contract.AllSamples;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;

public class AllLabSamplesEvent extends ConceptOperationEvent {

    public AllLabSamplesEvent(String conceptUrl, String labCategory, String title) {
        super(conceptUrl, labCategory, title);
    }

    @Override
    public boolean isResourceConcept(Concept concept) {
        return isLabSamplesConcept(concept);
    }


    private boolean isLabSamplesConcept(Concept concept) {
        return concept.getName(Context.getLocale()) != null && concept.getName(Context.getLocale()).getName().equals(AllSamples.ALL_SAMPLES);
    }
}
