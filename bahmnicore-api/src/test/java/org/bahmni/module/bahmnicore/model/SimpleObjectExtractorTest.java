/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.model;

import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;

import static org.junit.Assert.assertEquals;

public class SimpleObjectExtractorTest {
    @Test
    public void shouldReturnDefaultValueWhenKeyDoesNotExist() {
        SimpleObjectExtractor simpleObjectExtractor = new SimpleObjectExtractor(new SimpleObject().add("foo", 4));

        assertEquals(0, (int)simpleObjectExtractor.getValueOrDefault("bar", int.class));
    }
}
