/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.referencedata.labconcepts.contract;

import java.util.Arrays;
import java.util.List;

public class RadiologyTest extends Resource {
    public static final List<String> RADIOLOGY_TEST_CONCEPT_CLASSES = Arrays.asList("Radiology", "Radiology/Imaging Procedure");
    public static final String RADIOLOGY_TEST_PARENT_CONCEPT_NAME = "Radiology";
}
