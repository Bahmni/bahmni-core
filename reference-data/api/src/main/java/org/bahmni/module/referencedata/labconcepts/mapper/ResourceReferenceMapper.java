/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.ResourceReference;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;

public class ResourceReferenceMapper {
    public ResourceReference map(Concept concept) {
        return new ResourceReference(concept.getUuid(), concept.getName(Context.getLocale()).getName());
    }
}
