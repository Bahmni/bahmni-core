/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.web.v1_0;

import static org.bahmni.module.bahmnicore.web.v1_0.LocaleResolver.identifyLocale;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openmrs.util.LocaleUtility;

import java.util.Locale;

public class LocaleResolverTest {

    @Test
    public void shouldReturnDefaultLocaleIfNull() {
        Locale locale = identifyLocale(null);
        assertEquals(LocaleUtility.getDefaultLocale(), locale);
    }

    @Test
    public void shouldReturnDefaultLocaleIfEmpty() {
        Locale locale = identifyLocale("");
        assertEquals(LocaleUtility.getDefaultLocale(), locale);
    }

    @Test
    public void shouldReturnParsedLocaleIfValid() {
        Locale locale = identifyLocale("en_US");
        assertEquals(new Locale("en", "US"), locale);
    }

    @Test(expected = AssertionError.class)
    public void shouldThrowExceptionIfInvalidLocale() {
        identifyLocale("invalid");
        fail("Should have thrown exception");
    }

}