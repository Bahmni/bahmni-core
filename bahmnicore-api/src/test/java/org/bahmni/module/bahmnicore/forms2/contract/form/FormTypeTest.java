/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.forms2.contract.form;

import org.bahmni.module.bahmnicore.forms2.contract.FormType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FormTypeTest {

    @Test
    public void shouldReturnAllObservationTemplateFormsTypeAsV1() {
        assertEquals("FORMS1", FormType.FORMS1.toString());
        assertEquals(FormType.FORMS1, FormType.valueOfType("v1"));
    }

    @Test
    public void shouldReturnFormBuilderFormsTypeAsV2() {
        assertEquals("FORMS2", FormType.FORMS2.toString());
        assertEquals(FormType.FORMS2, FormType.valueOfType("v2"));

    }

    @Test
    public void shouldReturnFormTypeAsString() {
        assertEquals("v1", FormType.FORMS1.getType());
        assertEquals("v2", FormType.FORMS2.getType());

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldErrorOutForInvalidTypeString() {
        assertEquals(FormType.FORMS1, FormType.valueOfType("v0"));
    }
}