/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.RadiologyTest;
import org.openmrs.Concept;

public class RadiologyTestMapper extends ResourceMapper {
    public RadiologyTestMapper() {
        super(RadiologyTest.RADIOLOGY_TEST_PARENT_CONCEPT_NAME);
    }

    @Override
    public RadiologyTest map(Concept testConcept) {
        return mapResource(new RadiologyTest(), testConcept);
    }


}
